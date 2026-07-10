package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MockDao {

    // ----- Mock Tests (v11 schema) -----

    @Transaction
    @Query("SELECT * FROM mock_tests ORDER BY attemptedAt DESC")
    fun getAllMockTests(): Flow<List<MockTestWithQuestions>>

    @Transaction
    @Query("SELECT * FROM mock_tests WHERE id = :id")
    suspend fun getMockTestById(id: String): MockTestWithQuestions?

    @Query("SELECT * FROM mock_tests WHERE id = :id")
    suspend fun getRawMockTest(id: String): MockTestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(mockTest: MockTestEntity)

    @Update
    suspend fun updateMockTest(mockTest: MockTestEntity)

    @Delete
    suspend fun deleteMockTest(mockTest: MockTestEntity)

    @Query("DELETE FROM mock_tests WHERE id = :id")
    suspend fun deleteMockTestById(id: String)

    @Query("SELECT COUNT(*) FROM mock_tests")
    fun getMockTestCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mock_questions")
    fun getTotalQuestionCount(): Flow<Int>

    // ----- Question logs -----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionLog(question: MockQuestionLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionLogs(questions: List<MockQuestionLogEntity>)

    @Query("SELECT * FROM mock_questions WHERE mockTestId = :mockTestId ORDER BY questionNumber ASC")
    suspend fun getQuestionsForTestSync(mockTestId: String): List<MockQuestionLogEntity>

    @Query("SELECT * FROM mock_questions WHERE mockTestId = :mockTestId ORDER BY questionNumber ASC")
    fun getQuestionsForTest(mockTestId: String): Flow<List<MockQuestionLogEntity>>

    @Delete
    suspend fun deleteQuestionLog(question: MockQuestionLogEntity)

    @Query("DELETE FROM mock_questions WHERE mockTestId = :mockTestId")
    suspend fun deleteQuestionsForTest(mockTestId: String)

    // ----- Aggregations (sync, used by repository for analytics) -----

    /** Counts per errorCategory across ALL logged questions. */
    @Query("""
        SELECT errorCategory, COUNT(*) as count
        FROM mock_questions
        GROUP BY errorCategory
    """)
    suspend fun getErrorCategoryCountsSync(): List<CategoryCount>

    /** Counts + accuracy per (subjectName, topicName) across ALL logged questions. */
    @Query("""
        SELECT subjectName, topicName,
               COUNT(*) as totalQuestions,
               SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correctCount,
               SUM(CASE WHEN errorCategory = 'WRONG' OR (isCorrect = 0 AND errorCategory != 'UNATTEMPTED') THEN 1 ELSE 0 END) as wrongCount,
               SUM(CASE WHEN errorCategory = 'UNATTEMPTED' THEN 1 ELSE 0 END) as unattemptedCount
        FROM mock_questions
        WHERE topicName != ''
        GROUP BY subjectName, topicName
        ORDER BY totalQuestions DESC
    """)
    suspend fun getTopicAggregationSync(): List<TopicAggregationRow>

    /** Counts + accuracy per subjectName across ALL logged questions. */
    @Query("""
        SELECT subjectName,
               COUNT(*) as totalQuestions,
               SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correctCount,
               SUM(CASE WHEN isCorrect = 0 AND errorCategory != 'UNATTEMPTED' THEN 1 ELSE 0 END) as wrongCount,
               SUM(CASE WHEN errorCategory = 'UNATTEMPTED' THEN 1 ELSE 0 END) as unattemptedCount
        FROM mock_questions
        WHERE subjectName != ''
        GROUP BY subjectName
        ORDER BY totalQuestions DESC
    """)
    suspend fun getSubjectAggregationSync(): List<SubjectAggregationRow>

    // ----- Stats (Flow, for the KPI grid) -----

    /** Average percentage across all mock tests. */
    @Query("""
        SELECT AVG(CASE WHEN totalMarks > 0 THEN (marksObtained * 100.0 / totalMarks) ELSE 0 END)
        FROM mock_tests
    """)
    fun getAveragePercentage(): Flow<Float?>

    /** Best percentage across all mock tests. */
    @Query("""
        SELECT MAX(CASE WHEN totalMarks > 0 THEN (marksObtained * 100.0 / totalMarks) ELSE 0 END)
        FROM mock_tests
    """)
    fun getBestPercentage(): Flow<Float?>

    /** Average percentile across all mock tests. */
    @Query("SELECT AVG(percentile) FROM mock_tests WHERE percentile > 0")
    fun getAveragePercentile(): Flow<Float?>

    /** Best percentile across all mock tests. */
    @Query("SELECT MAX(percentile) FROM mock_tests WHERE percentile > 0")
    fun getBestPercentile(): Flow<Float?>

    /** Total time spent on mock tests in seconds. */
    @Query("SELECT SUM(actualDurationSeconds) FROM mock_tests")
    fun getTotalMockTimeSeconds(): Flow<Int?>

    // ----- Distinct subjects (for the subject analytics list) -----

    @Query("SELECT DISTINCT subjectName FROM mock_tests WHERE subjectName != '' ORDER BY subjectName ASC")
    fun getDistinctSubjects(): Flow<List<String>>
}

// ============================================================
// Aggregation result rows
// ============================================================

data class CategoryCount(
    val errorCategory: String,
    val count: Int
)

data class TopicAggregationRow(
    val subjectName: String,
    val topicName: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int
)

data class SubjectAggregationRow(
    val subjectName: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int
)
