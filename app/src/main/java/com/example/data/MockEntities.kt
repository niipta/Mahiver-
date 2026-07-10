package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Error categories used to classify each question in a mock test.
 * Each category is color-coded for the analysis UI.
 */
object ErrorCategory {
    const val CORRECT = "CORRECT"
    const val SILLY_MISTAKE = "SILLY_MISTAKE"
    const val CONCEPT_NOT_KNOWN = "CONCEPT_NOT_KNOWN"
    const val FORMULA_FORGOTTEN = "FORMULA_FORGOTTEN"
    const val TIME_PRESSURE = "TIME_PRESSURE"
    const val UNATTEMPTED = "UNATTEMPTED"

    val ALL = listOf(CORRECT, SILLY_MISTAKE, CONCEPT_NOT_KNOWN, FORMULA_FORGOTTEN, TIME_PRESSURE, UNATTEMPTED)

    /** Returns a stable color (ARGB Long) for the category for persistence/UI. */
    fun colorLong(category: String): Long = when (category) {
        CORRECT -> 0xFF0F9D58L          // green
        SILLY_MISTAKE -> 0xFFF59E0BL    // amber
        CONCEPT_NOT_KNOWN -> 0xFFD93025L // red
        FORMULA_FORGOTTEN -> 0xFF8E44ADL // purple
        TIME_PRESSURE -> 0xFF1A73E8L    // blue
        UNATTEMPTED -> 0xFF9E9E9EL      // grey
        else -> 0xFF9E9E9EL
    }

    /** Returns a Compose Color for the category. */
    fun color(category: String): Color = Color(colorLong(category).toInt())

    /** Human-readable label for display. */
    fun label(category: String): String = when (category) {
        CORRECT -> "Correct"
        SILLY_MISTAKE -> "Silly Mistake"
        CONCEPT_NOT_KNOWN -> "Concept Not Known"
        FORMULA_FORGOTTEN -> "Formula Forgotten"
        TIME_PRESSURE -> "Time Pressure"
        UNATTEMPTED -> "Unattempted"
        else -> category
    }

    /** Short label for compact chips. */
    fun shortLabel(category: String): String = when (category) {
        CORRECT -> "Correct"
        SILLY_MISTAKE -> "Silly"
        CONCEPT_NOT_KNOWN -> "Concept"
        FORMULA_FORGOTTEN -> "Formula"
        TIME_PRESSURE -> "Time"
        UNATTEMPTED -> "Skip"
        else -> category
    }
}

/**
 * Mock test categories. Drives the filter chips on the mocks screen.
 */
object MockCategory {
    const val FULL = "FULL"
    const val SECTIONAL = "SECTIONAL"
    const val PYQ = "PYQ"
    const val TOPIC = "TOPIC"
    const val CUSTOM = "CUSTOM"

    val ALL = listOf(FULL, SECTIONAL, PYQ, TOPIC, CUSTOM)

    fun label(category: String): String = when (category) {
        FULL -> "Full"
        SECTIONAL -> "Sectional"
        PYQ -> "PYQ"
        TOPIC -> "Topic"
        CUSTOM -> "Custom"
        else -> category
    }
}

/**
 * A mock test attempt. Captures the full result of one mock test the user took,
 * including score breakdown, percentile, rank and timing.
 *
 * Schema v11 — replaces the old mock_tests + mock_attempts split with a single
 * flat table that stores everything we need for analytics.
 */
@Entity(
    tableName = "mock_tests",
    indices = [
        Index("category"),
        Index("attemptedAt"),
        Index("subjectName")
    ]
)
data class MockTestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String = MockCategory.CUSTOM,
    val subjectId: String? = null,
    val subjectName: String,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val totalMarks: Int,
    val positiveMark: Float = 1f,
    val negativeMark: Float = 0f,
    val marksObtained: Float = 0f,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val unattemptedCount: Int = 0,
    val actualDurationSeconds: Int = 0,
    val percentile: Float = 0f,
    val rank: Int = 0,
    val totalCandidates: Int = 0,
    val attemptedAt: Long = System.currentTimeMillis(),
    val description: String = "",
    val tags: String = ""
) {
    val percentage: Float
        get() = if (totalMarks > 0f) (marksObtained / totalMarks) * 100f else 0f

    val accuracy: Float
        get() = if (correctCount + wrongCount > 0) correctCount.toFloat() / (correctCount + wrongCount) * 100f else 0f
}

/**
 * Per-question log for a mock test. Lets the user tag each question with the
 * subject, topic and the error category (silly / concept / formula / time / skip).
 *
 * This is what powers the Error Pattern Analysis and Topic Weightage sections
 * on the Mocks screen.
 */
@Entity(
    tableName = "mock_questions",
    foreignKeys = [
        ForeignKey(
            entity = MockTestEntity::class,
            parentColumns = ["id"],
            childColumns = ["mockTestId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("mockTestId"),
        Index("subjectName"),
        Index("topicName"),
        Index("errorCategory")
    ]
)
data class MockQuestionLogEntity(
    @PrimaryKey val id: String,
    val mockTestId: String,
    val questionNumber: Int,
    val subjectName: String,
    val topicName: String,
    val errorCategory: String = ErrorCategory.UNATTEMPTED,
    val isCorrect: Boolean = false,
    val timeSpentSeconds: Int = 0,
    val notes: String = ""
)

/**
 * Relation wrapper: a mock test + all its per-question logs.
 */
data class MockTestWithQuestions(
    @Embedded val mockTest: MockTestEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "mockTestId"
    )
    val questions: List<MockQuestionLogEntity>
) {
    val questionCount: Int get() = questions.size
}

// ============================================================
// ANALYTICS MODELS
// ============================================================

/** Count + percentage for one error category across all logged questions. */
data class ErrorCategoryBreakdown(
    val category: String,
    val count: Int,
    val percentage: Float
)

/**
 * Aggregated stats for one (subject, topic) pair across all mock questions.
 * `isWeak` is true when accuracy < 60% with at least 3 questions — used to flag
 * weak topics in the UI and recommendations.
 */
data class TopicWeightage(
    val subjectName: String,
    val topicName: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val accuracy: Float,
    val isWeak: Boolean,
    val priority: Int // 1 = highest priority (most questions, lowest accuracy)
)

/** Aggregated stats for one subject across all mock questions. */
data class SubjectAnalytics(
    val subjectName: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val accuracy: Float,
    val averagePercentage: Float,
    val attemptCount: Int
)

/**
 * One actionable recommendation shown on the Mocks screen.
 * Priority: 1 = highest. Drives the sort order and icon color.
 */
data class SmartRecommendation(
    val title: String,
    val description: String,
    val priority: Int,
    val iconName: String, // identifier the UI maps to an ImageVector
    val colorLong: Long
)
