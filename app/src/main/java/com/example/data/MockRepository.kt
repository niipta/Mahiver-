package com.example.data

import androidx.room.withTransaction
import com.example.data.sync.SyncDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MockRepository(
    private val db: AppDatabase,
    private val syncDao: SyncDao
) {
    private val mockDao = db.mockDao()

    // All flows run their DB reads on IO so the UI thread never blocks.
    val allMockTests: Flow<List<MockTestWithQuestions>> =
        mockDao.getAllMockTests().flowOn(Dispatchers.IO)
    val mockTestCount: Flow<Int> =
        mockDao.getMockTestCount().flowOn(Dispatchers.IO)
    val totalQuestionCount: Flow<Int> =
        mockDao.getTotalQuestionCount().flowOn(Dispatchers.IO)
    val averagePercentage: Flow<Float?> =
        mockDao.getAveragePercentage().flowOn(Dispatchers.IO)
    val bestPercentage: Flow<Float?> =
        mockDao.getBestPercentage().flowOn(Dispatchers.IO)
    val averagePercentile: Flow<Float?> =
        mockDao.getAveragePercentile().flowOn(Dispatchers.IO)
    val bestPercentile: Flow<Float?> =
        mockDao.getBestPercentile().flowOn(Dispatchers.IO)
    val totalMockTimeSeconds: Flow<Int?> =
        mockDao.getTotalMockTimeSeconds().flowOn(Dispatchers.IO)
    val distinctSubjects: Flow<List<String>> =
        mockDao.getDistinctSubjects().flowOn(Dispatchers.IO)

    /**
     * Insert a mock test together with its per-question logs in a single transaction.
     * If the test insert fails the questions are not left orphaned.
     */
    suspend fun insertMockTestWithQuestions(
        mockTest: MockTestEntity,
        questions: List<MockQuestionLogEntity>
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            mockDao.insertMockTest(mockTest)
            if (questions.isNotEmpty()) {
                mockDao.insertQuestionLogs(questions)
            }
            syncDao.insertSyncTask(
                SyncQueueEntity(
                    operationType = "INSERT",
                    entityType = "MOCK_TEST",
                    entityId = mockTest.id
                )
            )
        }
    }

    suspend fun updateMockTest(mockTest: MockTestEntity) = withContext(Dispatchers.IO) {
        db.withTransaction {
            mockDao.updateMockTest(mockTest)
            syncDao.insertSyncTask(
                SyncQueueEntity(
                    operationType = "UPDATE",
                    entityType = "MOCK_TEST",
                    entityId = mockTest.id
                )
            )
        }
    }

    suspend fun deleteMockTest(mockTest: MockTestEntity) = withContext(Dispatchers.IO) {
        db.withTransaction {
            mockDao.deleteMockTest(mockTest)
            syncDao.insertSyncTask(
                SyncQueueEntity(
                    operationType = "DELETE",
                    entityType = "MOCK_TEST",
                    entityId = mockTest.id
                )
            )
        }
    }

    suspend fun getMockTestById(id: String): MockTestWithQuestions? = withContext(Dispatchers.IO) {
        mockDao.getMockTestById(id)
    }

    suspend fun getQuestionsForTestSync(mockTestId: String): List<MockQuestionLogEntity> =
        withContext(Dispatchers.IO) {
            mockDao.getQuestionsForTestSync(mockTestId)
        }

    fun getQuestionsForTest(mockTestId: String): Flow<List<MockQuestionLogEntity>> =
        mockDao.getQuestionsForTest(mockTestId).flowOn(Dispatchers.IO)

    // ============================================================
    // ANALYTICS — all run on IO because they do GROUP BY scans
    // ============================================================

    /** Error-category breakdown across every logged question. */
    suspend fun getErrorCategoryBreakdown(): List<ErrorCategoryBreakdown> =
        withContext(Dispatchers.IO) {
            val rows = mockDao.getErrorCategoryCountsSync()
            val total = rows.sumOf { it.count }.toFloat()
            if (total <= 0f) return@withContext emptyList()
            rows.map { row ->
                ErrorCategoryBreakdown(
                    category = row.errorCategory,
                    count = row.count,
                    percentage = row.count / total * 100f
                )
            }
        }

    /**
     * Per-(subject, topic) weightage. Flags weak topics (accuracy < 60% with
     * 3+ questions) and assigns a priority so the UI can sort weakest first.
     */
    suspend fun getTopicWeightage(): List<TopicWeightage> = withContext(Dispatchers.IO) {
        val rows = mockDao.getTopicAggregationSync()
        if (rows.isEmpty()) return@withContext emptyList()
        rows.map { row ->
            val attempted = row.correctCount + row.wrongCount
            val accuracy = if (attempted > 0) row.correctCount.toFloat() / attempted * 100f else 0f
            val isWeak = row.totalQuestions >= 3 && accuracy < 60f
            // Priority: more questions + lower accuracy => higher priority (lower number)
            val priority = row.totalQuestions * 100 - accuracy.toInt()
            TopicWeightage(
                subjectName = row.subjectName,
                topicName = row.topicName,
                totalQuestions = row.totalQuestions,
                correctCount = row.correctCount,
                wrongCount = row.wrongCount,
                unattemptedCount = row.unattemptedCount,
                accuracy = accuracy,
                isWeak = isWeak,
                priority = priority
            )
        }.sortedByDescending { it.priority }
    }

    /** Per-subject aggregated stats. */
    suspend fun getSubjectAnalytics(): List<SubjectAnalytics> = withContext(Dispatchers.IO) {
        val rows = mockDao.getSubjectAggregationSync()
        if (rows.isEmpty()) return@withContext emptyList()
        rows.map { row ->
            val attempted = row.correctCount + row.wrongCount
            val accuracy = if (attempted > 0) row.correctCount.toFloat() / attempted * 100f else 0f
            SubjectAnalytics(
                subjectName = row.subjectName,
                totalQuestions = row.totalQuestions,
                correctCount = row.correctCount,
                wrongCount = row.wrongCount,
                unattemptedCount = row.unattemptedCount,
                accuracy = accuracy,
                averagePercentage = accuracy, // close enough proxy when we don't have per-test split
                attemptCount = 0
            )
        }
    }
}
