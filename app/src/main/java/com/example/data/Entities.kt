package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dateMillis: Long
)

@Entity(
    tableName = "sync_queue",
    indices = [androidx.room.Index(value = ["syncStatus", "queuedAt"])]
)
data class SyncQueueEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val operationType: String, // INSERT, UPDATE, DELETE
    val entityType: String,
    val entityId: String,
    val syncStatus: String = "PENDING",
    val retryCount: Int = 0,
    val queuedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val color: Long // color in ARGB representation
)

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class TopicEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val name: String,
    val isPriority: Boolean, // priority system
    val isWeak: Boolean, // weak topic marker
    val estimatedMinutes: Int, // estimated study time
    val isCompleted: Boolean // If NO subtopics exist, user can manually toggle this, else derived.
)

@Entity(
    tableName = "subtopics",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class SubtopicEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val name: String,
    val isCompleted: Boolean
)

@Entity(
    tableName = "revisions",
    indices = [
        androidx.room.Index(value = ["relatedId"]),
        androidx.room.Index(value = ["scheduledDateMillis"])
    ]
)
data class RevisionEntity(
    @PrimaryKey val id: String,
    val relatedId: String,
    val subjectName: String,
    val title: String,
    val type: String, // "Topic" or "Subtopic"
    val priority: String, // "High", "Medium", "Low"
    val estimatedMinutes: Int,
    val scheduledDateMillis: Long,
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val confidence: Int = 100,
    val repetitionLevel: Int = 1
)

@Entity(
    tableName = "focus_sessions",
    indices = [androidx.room.Index(value = ["timestamp"])]
)
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val subjectId: String?,
    val topicId: String?,
    val subtopicId: String?,
    val subjectName: String,
    val topicName: String?,
    val durationMinutes: Int, // Represents planned or total minutes recorded
    val actualDurationSeconds: Int = 0,
    val interruptions: Int = 0,
    val sessionType: String, // "Study" or "Revision"
    val isDeepFocus: Boolean = false,
    val timestamp: Long
)

@Entity(tableName = "daily_plans")
data class DailyPlanEntity(
    @PrimaryKey val dateString: String, // e.g. "2026-06-03"
    val dateMillis: Long,
    val plannedTopicIds: String, // comma separated
    val plannedSubtopicIds: String = "", // comma separated
    val plannedRevisionIds: String, // comma separated
    val isCompleted: Boolean = false
)

data class TopicWithSubtopics(
    @Embedded val topic: TopicEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "topicId"
    )
    val subtopics: List<SubtopicEntity>
) {
    val progress: Float
        get() = if (subtopics.isEmpty()) {
            if (topic.isCompleted) 1f else 0f
        } else {
            val completed = subtopics.count { it.isCompleted }
            completed.toFloat() / subtopics.size
        }
        
    val isFullyCompleted: Boolean
        get() = if (subtopics.isEmpty()) topic.isCompleted else subtopics.all { it.isCompleted }
}

data class SubjectWithTopics(
    @Embedded val subject: SubjectEntity,
    @Relation(
        entity = TopicEntity::class,
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val topics: List<TopicWithSubtopics>
) {
    val totalTopics: Int get() = topics.size
    val completedTopics: Int get() = topics.count { it.isFullyCompleted }
    val progress: Float get() = if (topics.isEmpty()) 0f else completedTopics.toFloat() / topics.size
}
