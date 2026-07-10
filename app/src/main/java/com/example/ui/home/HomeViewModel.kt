package com.example.ui.home

import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.data.SubjectEntity
import com.example.data.ai.AiPlannerEngine
import com.example.data.ai.DailyPlanResponse
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.example.data.SyllabusDao
import com.example.data.RevisionDao
import com.example.data.FocusDao
import com.example.data.ExamDao
import com.example.data.PlannerDao
import com.example.data.sync.SyncDao

data class DatabaseData(
    val subjectsWithTopics: List<com.example.data.SubjectWithTopics>,
    val revisions: List<com.example.data.RevisionEntity>,
    val focusSessions: List<com.example.data.FocusSessionEntity>,
    val examsList: List<com.example.data.ExamEntity>,
    val plannerPlan: com.example.data.DailyPlanEntity? = null
)

data class TimerData(
    val timeRemaining: Long,
    val timerState: com.example.service.TimerState
)

private data class Stage1Data(
    val db: DatabaseData,
    val timer: TimerData,
    val userName: String,
    val goalMetDates: Set<String>
)

data class HomeUiState(
    val userName: String = "MAHIR",
    val todayTopicsTarget: Int = 0,
    val todayTopicsCompleted: Int = 0,
    val upcomingRevisions: List<com.example.data.RevisionEntity> = emptyList(),
    val focusSessionMinutes: Int = 25,
    val focusSessionSeconds: Int = 0,
    val isFocusing: Boolean = false,
    val studyHoursOverview: String = "0h 0m",
    val revisionHoursOverview: String = "0h 0m",
    val topicsCompletedOverview: Int = 0,
    val revisionsDoneOverview: Int = 0,
    val subjects: List<SubjectEntity> = emptyList(),
    val exams: List<com.example.data.ExamEntity> = emptyList(),
    val weakTopics: List<String> = emptyList(),
    val hasActiveSession: Boolean = false,
    val suggestedTopics: List<String> = emptyList(),
    val priorities: String = "Review your pending tasks",
    val aiPlanLoading: Boolean = false,
    val currentStreak: Int = 0,
    val missedYesterday: Boolean = false,
    val lifetimeFocusMinutes: Int = 0,
    val dailyGoalMinutes: Int = 120,
    val todayFocusMinutes: Int = 0,
    val error: String? = null,
    val newUnlockedAchievement: com.example.domain.Achievement? = null,
    // AI personal analysis
    val analysisSummary: String? = null,
    val analysisStrengths: List<String> = emptyList(),
    val analysisImprovements: List<String> = emptyList(),
    val tonightTask: String? = null,
    val motivationalMessage: String? = null,
    val analysisLoading: Boolean = false,
    // Daily motivational quote (local, no API)
    val dailyQuote: String = com.example.data.ai.MotivationalQuotes.daily()
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syllabusDao: SyllabusDao,
    private val revisionDao: RevisionDao,
    private val focusDao: FocusDao,
    private val examDao: ExamDao,
    private val plannerDao: PlannerDao,
    private val syncDao: SyncDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val dbDataState = combine(
        syllabusDao.getAllSubjectsWithTopics(),
        revisionDao.getAllRevisions(),
        focusDao.getAllSessions(),
        examDao.getAllExams(),
        plannerDao.getPlan(com.example.data.PlannerRepository.getDateString())
    ) { subjectsWithTopics, revisions, focusSessions, examsList, plannerPlan ->
        DatabaseData(subjectsWithTopics, revisions, focusSessions, examsList, plannerPlan)
    }
    
    private val timerDataState = combine(
        com.example.service.TimerManager.timeRemaining,
        com.example.service.TimerManager.timerState
    ) { timeRemaining, timerState ->
        TimerData(timeRemaining, timerState)
    }

    private val stage1Flow = combine(
        dbDataState,
        timerDataState,
        settingsRepository.userName,
        settingsRepository.goalMetDates
    ) { db, timer, userName, goalMetDates ->
        Stage1Data(db, timer, userName, goalMetDates)
    }

    private val _dbAndTimerState = combine(
        stage1Flow,
        settingsRepository.dailyGoalMinutes,
        settingsRepository.dailyGoalTopics
    ) { stage1, dailyGoalMinutes, dailyGoalTopics ->
        val db = stage1.db
        val timer = stage1.timer
        val userName = stage1.userName
        val goalMetDates = stage1.goalMetDates

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayStr = sdf.format(java.util.Date())

        val todaysFocusSessions = db.focusSessions.filter { session ->
            val sessionDate = sdf.format(java.util.Date(session.timestamp))
            sessionDate == todayStr && session.actualDurationSeconds > 0
        }

        val totalFocusSeconds = todaysFocusSessions.filter { it.sessionType != "Revision" }.sumOf { it.actualDurationSeconds }
        val hours = totalFocusSeconds / 3600
        val mins = (totalFocusSeconds % 3600) / 60
        val studyHoursString = "${hours}h ${mins}m"
        val todayFocusMinutes = (totalFocusSeconds / 60).toInt()

        val totalRevisionSeconds = todaysFocusSessions.filter { it.sessionType == "Revision" }.sumOf { it.actualDurationSeconds }
        val revHours = totalRevisionSeconds / 3600
        val revMins = (totalRevisionSeconds % 3600) / 60
        val revisionHoursString = "${revHours}h ${revMins}m"

        val plannedTopics = db.plannerPlan?.plannedTopicIds?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        val plannedSubtopics = db.plannerPlan?.plannedSubtopicIds?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        val plannedRevisions = db.plannerPlan?.plannedRevisionIds?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

        var todayTopicsTargetCount = plannedTopics.size + plannedSubtopics.size
        var todayTopicsCompletedCount = 0
        var totalTopicsCompleted = 0

        db.subjectsWithTopics.forEach { subject ->
            subject.topics.forEach { topic ->
                if (topic.isFullyCompleted) {
                    totalTopicsCompleted++
                }

                if (topic.topic.isCompleted && plannedTopics.contains(topic.topic.id)) {
                    todayTopicsCompletedCount++
                }

                topic.subtopics.forEach { subtopic ->
                    if (subtopic.isCompleted && plannedSubtopics.contains(subtopic.id)) {
                        todayTopicsCompletedCount++
                    }
                }
            }
        }

        val todaysRevisions = db.revisions.filter { plannedRevisions.contains(it.id) && !it.isCompleted && it.isActive }.take(3)
        val completedRevisionsCount = db.revisions.count { plannedRevisions.contains(it.id) && it.isCompleted }

        val todayGoalMetByTopics = todayTopicsCompletedCount >= dailyGoalTopics
        val todayGoalMetByMinutes = todayFocusMinutes >= dailyGoalMinutes

        val isRunning = timer.timerState == com.example.service.TimerState.RUNNING
        val isPaused = timer.timerState == com.example.service.TimerState.PAUSED
        val hasActive = isRunning || (isPaused && timer.timeRemaining > 0)

        val combinedGoalMetDates = goalMetDates.toMutableSet()
        if (todayGoalMetByTopics || todayGoalMetByMinutes) combinedGoalMetDates.add(todayStr)

        val streakResult = com.example.domain.StreakCalculator.compute(
            sessions = db.focusSessions,
            goalMetDates = combinedGoalMetDates,
            todayStr = todayStr,
            dailyGoalMinutes = dailyGoalMinutes
        )
        val computedStreak = streakResult.currentStreak
        val wasMissedYesterday = streakResult.missedYesterday

        val lifetimeFocusMinutes = db.focusSessions.sumOf { it.actualDurationSeconds } / 60

        HomeUiState(
            userName = userName,
            todayTopicsTarget = todayTopicsTargetCount,
            todayTopicsCompleted = todayTopicsCompletedCount,
            upcomingRevisions = todaysRevisions,
            studyHoursOverview = studyHoursString,
            revisionHoursOverview = revisionHoursString,
            topicsCompletedOverview = totalTopicsCompleted,
            revisionsDoneOverview = completedRevisionsCount,
            subjects = db.subjectsWithTopics.map { it.subject },
            exams = db.examsList.filter { it.dateMillis >= System.currentTimeMillis() }.sortedBy { it.dateMillis },
            hasActiveSession = hasActive,
            focusSessionMinutes = (timer.timeRemaining / 60).toInt(),
            focusSessionSeconds = (timer.timeRemaining % 60).toInt(),
            isFocusing = isRunning,
            currentStreak = computedStreak,
            missedYesterday = wasMissedYesterday,
            lifetimeFocusMinutes = lifetimeFocusMinutes,
            dailyGoalMinutes = dailyGoalMinutes,
            todayFocusMinutes = todayFocusMinutes
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)


    private val _aiPlanState = kotlinx.coroutines.flow.MutableStateFlow<DailyPlanResponse?>(null)
    private val _aiLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val _aiError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val _unlockedAchievementState = kotlinx.coroutines.flow.MutableStateFlow<com.example.domain.Achievement?>(null)

    // AI personal analysis state
    private val _analysisState = kotlinx.coroutines.flow.MutableStateFlow<com.example.data.ai.PersonalAnalysis?>(null)
    private val _analysisLoading = kotlinx.coroutines.flow.MutableStateFlow(false)

    // Stage 1: combine DB+timer state with AI plan state (5 flows)
    private data class StageAiPlan(
        val dbState: HomeUiState,
        val plan: DailyPlanResponse?,
        val loading: Boolean,
        val err: String?,
        val newAchievement: com.example.domain.Achievement?
    )

    private val stageAiPlan = combine(
        _dbAndTimerState,
        _aiPlanState,
        _aiLoading,
        _aiError,
        _unlockedAchievementState
    ) { dbState, plan, loading, err, newAchievement ->
        StageAiPlan(dbState, plan, loading, err, newAchievement)
    }

    // Stage 2: combine stage1 with analysis state (3 flows)
    val fullUiState: StateFlow<HomeUiState> = combine(
        stageAiPlan,
        _analysisState,
        _analysisLoading
    ) { stage, analysis, analysisLoading ->
        stage.dbState.copy(
            suggestedTopics = stage.plan?.suggestedTopics ?: emptyList(),
            weakTopics = stage.plan?.weakTopicsDetected ?: emptyList(),
            priorities = stage.plan?.priorities ?: "Review your pending tasks",
            aiPlanLoading = stage.loading,
            error = stage.err,
            newUnlockedAchievement = stage.newAchievement,
            analysisSummary = analysis?.summary,
            analysisStrengths = analysis?.strengths ?: emptyList(),
            analysisImprovements = analysis?.improvements ?: emptyList(),
            tonightTask = analysis?.tonightTask,
            motivationalMessage = analysis?.motivationalMessage,
            analysisLoading = analysisLoading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
    
    val uiState = fullUiState

    init {
        viewModelScope.launch {
            // FIX: debounce achievement-check to ~2s to avoid running 4×/sec
            combine(
                _dbAndTimerState,
                dbDataState,
                settingsRepository.dailyGoalTopics
            ) { state, db, dailyGoalTopics ->
                Triple(state, db, dailyGoalTopics)
            }
            .debounce(2000)
            .distinctUntilChanged()
            .collect { (state, db, dailyGoalTopics) ->
                val todayGoalMetByTopics = state.todayTopicsCompleted >= dailyGoalTopics
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val todayStr = sdf.format(java.util.Date())
                
                if (settingsRepository.goalMetDates.value.contains(todayStr) != todayGoalMetByTopics) {
                    settingsRepository.setGoalMet(todayStr, todayGoalMetByTopics)
                }

                // Check achievements
                val focusMinutes = (db.focusSessions.sumOf { it.actualDurationSeconds } / 60)
                var topicsCompleted = 0
                db.subjectsWithTopics.forEach { subj ->
                    subj.topics.forEach {
                        if (it.topic.isCompleted) topicsCompleted++
                        it.subtopics.forEach { sub -> if (sub.isCompleted) topicsCompleted++ }
                    }
                }
                val revisionsCompleted = db.revisions.count { it.isCompleted }
                val lifetimeStats = com.example.domain.LifetimeStats(
                    streak = state.currentStreak,
                    focusMinutes = focusMinutes,
                    topicsCompleted = topicsCompleted,
                    revisionsCompleted = revisionsCompleted
                )
                val unlockedIds = settingsRepository.unlockedAchievements.value
                val newlyUnlocked = com.example.domain.AchievementChecker.check(lifetimeStats, unlockedIds)
                // Queue multiple unlocks instead of dropping all but first
                if (newlyUnlocked.isNotEmpty() && _unlockedAchievementState.value == null) {
                    // Persist all newly unlocked, queue the first for display
                    newlyUnlocked.forEach { settingsRepository.unlockAchievement(it.id) }
                    _unlockedAchievementState.value = newlyUnlocked.first()
                }
            }
        }
    }

    fun dismissAchievement() {
        val ach = _unlockedAchievementState.value
        if (ach != null) {
            settingsRepository.unlockAchievement(ach.id)
            _unlockedAchievementState.value = null
        }
    }

    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    fun generateSmartPlan() {
        if (_aiLoading.value) return
        _aiLoading.value = true
        _aiError.value = null
        
        viewModelScope.launch {
            val dbData = dbDataState.firstOrNull() ?: return@launch
            
            val planner = com.example.data.ai.AiPlannerEngine {
                settingsRepository.geminiApiKey.value
            }
            
            // FIX: compute actual study hours from recent sessions instead of hardcoded "2"
            val recentMinutes = dbData.focusSessions
                .filter { System.currentTimeMillis() - it.timestamp < 7L * 86_400_000L }
                .sumOf { it.actualDurationSeconds } / 60
            val studyHoursStr = "${recentMinutes / 60}h ${recentMinutes % 60}m"
            
            val result = planner.generateDailyPlan(
                subjects = dbData.subjectsWithTopics,
                revisions = dbData.revisions,
                recentSessions = dbData.focusSessions,
                studyHours = studyHoursStr,
                examsCount = dbData.examsList.size
            )
            
            when (result) {
                is com.example.data.ai.AiPlanResult.Success -> {
                    _aiPlanState.value = result.plan
                }
                is com.example.data.ai.AiPlanResult.Error -> {
                    _aiError.value = result.message
                }
                is com.example.data.ai.AiPlanResult.NoApiKey -> {
                    _aiError.value = "No API key configured"
                }
            }
            _aiLoading.value = false
        }
    }

    /**
     * Generates a personalized AI analysis of the user's study habits.
     * Includes strengths, improvements, tonight's task, and a motivational
     * message. At night (after 8 PM), the tone shifts to a gentle guilt trip
     * if the user hasn't met their daily goal.
     */
    fun generateAnalysis() {
        if (_analysisLoading.value) return
        _analysisLoading.value = true
        _aiError.value = null

        viewModelScope.launch {
            val dbData = dbDataState.firstOrNull() ?: run {
                _analysisLoading.value = false
                return@launch
            }

            val engine = com.example.data.ai.AiAnalysisEngine {
                settingsRepository.geminiApiKey.value
            }

            val result = engine.generateAnalysis(
                subjects = dbData.subjectsWithTopics,
                revisions = dbData.revisions,
                recentSessions = dbData.focusSessions,
                currentStreak = settingsRepository.currentStreak.value,
                dailyGoalMinutes = settingsRepository.dailyGoalMinutes.value
            )

            when (result) {
                is com.example.data.ai.AiAnalysisResult.Success -> {
                    _analysisState.value = result.analysis
                }
                is com.example.data.ai.AiAnalysisResult.Error -> {
                    _aiError.value = result.message
                }
                is com.example.data.ai.AiAnalysisResult.NoApiKey -> {
                    _aiError.value = "No API key configured. Add your Gemini key in Settings."
                }
            }
            _analysisLoading.value = false
        }
    }

    fun addExam(name: String, dateMillis: Long) {
        viewModelScope.launch {
            val examId = java.util.UUID.randomUUID().toString()
            examDao.insertExam(com.example.data.ExamEntity(
                id = examId,
                name = name,
                dateMillis = dateMillis
            ))
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "EXAM", entityId = examId))
        }
    }
}
