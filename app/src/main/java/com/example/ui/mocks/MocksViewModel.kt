package com.example.ui.mocks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ErrorCategory
import com.example.data.ErrorCategoryBreakdown
import com.example.data.MockCategory
import com.example.data.MockQuestionLogEntity
import com.example.data.MockRepository
import com.example.data.MockTestEntity
import com.example.data.MockTestWithQuestions
import com.example.data.SmartRecommendation
import com.example.data.SubjectAnalytics
import com.example.data.SubjectWithTopics
import com.example.data.TopicWeightage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MocksUiState(
    val mockTests: List<MockTestWithQuestions> = emptyList(),
    val subjects: List<SubjectWithTopics> = emptyList(),
    val mockTestCount: Int = 0,
    val totalQuestionCount: Int = 0,
    val averagePercentage: Float = 0f,
    val bestPercentage: Float = 0f,
    val averagePercentile: Float = 0f,
    val bestPercentile: Float = 0f,
    val totalMockTimeSeconds: Int = 0,
    val loading: Boolean = true
)

/**
 * Analytics shown on the Mocks screen (error breakdown, topic weightage,
 * subject analytics, recommendations). Kept separate from [MocksUiState] so
 * the heavy GROUP BY queries only re-run when the mock list actually changes,
 * not on every keystroke in the search box.
 */
data class MocksAnalytics(
    val errorBreakdown: List<ErrorCategoryBreakdown> = emptyList(),
    val topicWeightage: List<TopicWeightage> = emptyList(),
    val subjectAnalytics: List<SubjectAnalytics> = emptyList(),
    val recommendations: List<SmartRecommendation> = emptyList(),
    val loading: Boolean = true
)

@HiltViewModel
class MocksViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val repository: MockRepository
) : ViewModel() {

    private val syllabusDao = AppDatabase.getDatabase(application).syllabusDao()

    /**
     * Stage 1 — combine the 9 sources the UI needs:
     *   1. mockTests          (Flow<List<MockTestWithQuestions>>)
     *   2. mockTestCount      (Flow<Int>)
     *   3. totalQuestionCount (Flow<Int>)
     *   4. averagePercentage  (Flow<Float?>)
     *   5. bestPercentage     (Flow<Float?>)
     *   6. averagePercentile  (Flow<Float?>)
     *   7. bestPercentile     (Flow<Float?>)
     *   8. totalMockTimeSeconds (Flow<Int?>)
     *   9. subjects           (Flow<List<SubjectWithTopics>>)
     *
     * Room's combine() supports up to 5 flows; we nest two combines to stay
     * within that limit and keep a single StateFlow as the source of truth.
     */
    private data class Stats1(
        val mockTests: List<MockTestWithQuestions>,
        val subjects: List<SubjectWithTopics>,
        val mockTestCount: Int
    )

    private data class Stats2(
        val stats1: Stats1,
        val totalQuestionCount: Int,
        val averagePercentage: Float,
        val bestPercentage: Float
    )

    private data class Stats3(
        val stats2: Stats2,
        val averagePercentile: Float,
        val bestPercentile: Float,
        val totalMockTimeSeconds: Int
    )

    private val stats1Flow = combine(
        repository.allMockTests,
        syllabusDao.getAllSubjectsWithTopics(),
        repository.mockTestCount
    ) { mockTests, subjects, mockTestCount ->
        Stats1(mockTests, subjects, mockTestCount)
    }

    private val stats2Flow = combine(
        stats1Flow,
        repository.totalQuestionCount,
        repository.averagePercentage,
        repository.bestPercentage
    ) { s1, totalQ, avgPct, bestPct ->
        Stats2(s1, totalQ, avgPct ?: 0f, bestPct ?: 0f)
    }

    private val stats3Flow = combine(
        stats2Flow,
        repository.averagePercentile,
        repository.bestPercentile,
        repository.totalMockTimeSeconds
    ) { s2, avgPerc, bestPerc, totalSec ->
        Stats3(s2, avgPerc ?: 0f, bestPerc ?: 0f, totalSec ?: 0)
    }

    val uiState: StateFlow<MocksUiState> = stats3Flow
        .map { s3 ->
            MocksUiState(
                mockTests = s3.stats2.stats1.mockTests,
                subjects = s3.stats2.stats1.subjects,
                mockTestCount = s3.stats2.stats1.mockTestCount,
                totalQuestionCount = s3.stats2.totalQuestionCount,
                averagePercentage = s3.stats2.averagePercentage,
                bestPercentage = s3.stats2.bestPercentage,
                averagePercentile = s3.averagePercentile,
                bestPercentile = s3.bestPercentile,
                totalMockTimeSeconds = s3.totalMockTimeSeconds,
                loading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MocksUiState()
        )

    // ============================================================
    // ANALYTICS — separate StateFlow, recomputed only when mocks change
    // ============================================================

    private val _analytics = MutableStateFlow(MocksAnalytics())
    val analytics: StateFlow<MocksAnalytics> = _analytics.asStateFlow()

    init {
        // Recompute analytics whenever the mock list changes. We watch uiState
        // (not the raw flow) so we get the deduped, throttled value.
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.loading) return@collect
                recomputeAnalytics(state.mockTests)
            }
        }
    }

    private suspend fun recomputeAnalytics(mockTests: List<MockTestWithQuestions>) {
        val errorBreakdown = repository.getErrorCategoryBreakdown()
        val topicWeightage = repository.getTopicWeightage()
        val subjectAnalytics = repository.getSubjectAnalytics()
        val recommendations = generateRecommendations(errorBreakdown, topicWeightage, subjectAnalytics)
        _analytics.value = MocksAnalytics(
            errorBreakdown = errorBreakdown,
            topicWeightage = topicWeightage,
            subjectAnalytics = subjectAnalytics,
            recommendations = recommendations,
            loading = false
        )
    }

    /**
     * Generates actionable recommendations from the analytics. 7 patterns, each
     * with a priority (1 = highest) so the UI can sort and color-code them.
     */
    private fun generateRecommendations(
        errorBreakdown: List<ErrorCategoryBreakdown>,
        topicWeightage: List<TopicWeightage>,
        subjectAnalytics: List<SubjectAnalytics>
    ): List<SmartRecommendation> {
        val recs = mutableListOf<SmartRecommendation>()

        fun countFor(category: String): Int =
            errorBreakdown.firstOrNull { it.category == category }?.count ?: 0

        val totalQuestions = errorBreakdown.sumOf { it.count }.toFloat()

        // 1. Silly mistakes > 30% of all errors → "Slow down"
        val sillyCount = countFor(ErrorCategory.SILLY_MISTAKE)
        if (totalQuestions > 0 && sillyCount / totalQuestions > 0.30f) {
            recs += SmartRecommendation(
                title = "Slow down on calculations",
                description = "Silly mistakes are ${"%d".format(sillyCount / totalQuestions * 100)}%% of your errors. Double-check each step before moving on.",
                priority = 1,
                iconName = "warning",
                colorLong = 0xFFF59E0BL
            )
        }

        // 2. Formula forgotten >= 3 → "Revise formulas daily"
        val formulaCount = countFor(ErrorCategory.FORMULA_FORGOTTEN)
        if (formulaCount >= 3) {
            recs += SmartRecommendation(
                title = "Revise formulas daily",
                description = "You forgot formulas in $formulaCount questions. Add a 10-minute formula revision to your morning routine.",
                priority = 2,
                iconName = "menu_book",
                colorLong = 0xFF8E44ADL
            )
        }

        // 3. Concept not known >= 3 → "Cover untouched concepts"
        val conceptCount = countFor(ErrorCategory.CONCEPT_NOT_KNOWN)
        if (conceptCount >= 3) {
            recs += SmartRecommendation(
                title = "Cover untouched concepts",
                description = "$conceptCount questions were wrong because the concept wasn't known. Schedule dedicated concept-building sessions.",
                priority = 3,
                iconName = "lightbulb",
                colorLong = 0xFFD93025L
            )
        }

        // 4. Time pressure >= 5 → "Work on speed"
        val timeCount = countFor(ErrorCategory.TIME_PRESSURE)
        if (timeCount >= 5) {
            recs += SmartRecommendation(
                title = "Work on speed",
                description = "$timeCount questions were missed due to time pressure. Practice with a strict timer to build pace.",
                priority = 4,
                iconName = "timer",
                colorLong = 0xFF1A73E8L
            )
        }

        // 5. Weakest topic (isWeak, highest priority) → "Revise: <topic>"
        val weakestTopic = topicWeightage.firstOrNull { it.isWeak }
        if (weakestTopic != null) {
            recs += SmartRecommendation(
                title = "Revise: ${weakestTopic.topicName}",
                description = "Accuracy is only ${"%.0f".format(weakestTopic.accuracy)}%% across ${weakestTopic.totalQuestions} questions in ${weakestTopic.subjectName}.",
                priority = 5,
                iconName = "target",
                colorLong = 0xFFEF4444L
            )
        }

        // 6. Weakest subject (accuracy < 50%, 5+ Q) → "Strengthen <subject>"
        val weakestSubject = subjectAnalytics.firstOrNull { it.accuracy < 50f && it.totalQuestions >= 5 }
        if (weakestSubject != null) {
            recs += SmartRecommendation(
                title = "Strengthen ${weakestSubject.subjectName}",
                description = "Overall accuracy in ${weakestSubject.subjectName} is ${"%.0f".format(weakestSubject.accuracy)}%%. Allocate more study time here.",
                priority = 6,
                iconName = "school",
                colorLong = 0xFFD93025L
            )
        }

        // 7. Positive reinforcement when nothing is weak
        if (recs.isEmpty() && totalQuestions > 0) {
            recs += SmartRecommendation(
                title = "You're on track!",
                description = "No critical weak areas detected. Keep practicing and logging your mocks to surface deeper insights.",
                priority = 7,
                iconName = "check_circle",
                colorLong = 0xFF0F9D58L
            )
        }

        return recs.sortedBy { it.priority }
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    /**
     * Adds a mock test together with its per-question logs.
     * `questions` is a list of (subject, topic, errorCategory) tuples from the
     * Question Log Editor — `questionNumber` is assigned 1..N here.
     */
    fun addMockTest(
        title: String,
        category: String,
        subjectId: String?,
        subjectName: String,
        totalQuestions: Int,
        durationMinutes: Int,
        totalMarks: Int,
        positiveMark: Float,
        negativeMark: Float,
        marksObtained: Float,
        correctCount: Int,
        wrongCount: Int,
        unattemptedCount: Int,
        actualDurationSeconds: Int,
        percentile: Float,
        rank: Int,
        totalCandidates: Int,
        attemptedAt: Long,
        description: String,
        tags: List<String>,
        questions: List<QuestionLogInput>
    ) {
        viewModelScope.launch {
            val mockTestId = UUID.randomUUID().toString()
            val mockTest = MockTestEntity(
                id = mockTestId,
                title = title,
                category = category,
                subjectId = subjectId,
                subjectName = subjectName,
                totalQuestions = totalQuestions,
                durationMinutes = durationMinutes,
                totalMarks = totalMarks,
                positiveMark = positiveMark,
                negativeMark = negativeMark,
                marksObtained = marksObtained,
                correctCount = correctCount,
                wrongCount = wrongCount,
                unattemptedCount = unattemptedCount,
                actualDurationSeconds = actualDurationSeconds,
                percentile = percentile,
                rank = rank,
                totalCandidates = totalCandidates,
                attemptedAt = attemptedAt,
                description = description,
                tags = tags.joinToString(",")
            )
            val questionEntities = questions.mapIndexed { idx, q ->
                MockQuestionLogEntity(
                    id = UUID.randomUUID().toString(),
                    mockTestId = mockTestId,
                    questionNumber = idx + 1,
                    subjectName = q.subjectName,
                    topicName = q.topicName,
                    errorCategory = q.errorCategory,
                    isCorrect = q.errorCategory == ErrorCategory.CORRECT,
                    timeSpentSeconds = q.timeSpentSeconds,
                    notes = q.notes
                )
            }
            repository.insertMockTestWithQuestions(mockTest, questionEntities)
        }
    }

    fun deleteMockTest(mockTest: MockTestEntity) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteMockTest(mockTest) }
    }
}

/**
 * Input model for the Question Log Editor in the Add Mock dialog. The ViewModel
 * converts these to [MockQuestionLogEntity]s with generated ids and question numbers.
 */
data class QuestionLogInput(
    val subjectName: String,
    val topicName: String,
    val errorCategory: String,
    val timeSpentSeconds: Int = 0,
    val notes: String = ""
)
