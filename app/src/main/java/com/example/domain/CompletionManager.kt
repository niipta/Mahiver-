package com.example.domain

import com.example.data.*
import java.util.UUID

/**
 * Centralised completion-toggle logic for syllabus topics & subtopics.
 *
 * When a topic/subtopic is marked complete:
 *   - the [SyllabusViewModel] will first prompt the user whether to add to revision
 *   - only when the user accepts does this manager schedule the revision
 * When unmarked: pause any active pending revisions for that item.
 */
class CompletionManager(
    private val syllabusDao: SyllabusDao,
    private val revisionDao: RevisionDao,
    private val syncDao: com.example.data.sync.SyncDao
) {
    /**
     * Toggles the completion flag of [topic]. Does NOT create any revision
     * unless [addToRevision] is true.
     */
    suspend fun toggleTopicCompletion(
        topic: TopicEntity,
        subjectName: String,
        isCompleted: Boolean,
        addToRevision: Boolean = false
    ) {
        val updated = topic.copy(isCompleted = isCompleted)
        syllabusDao.updateTopic(updated)
        syncDao.insertSyncTask(
            SyncQueueEntity(
                operationType = "UPDATE",
                entityType = "TOPIC",
                entityId = topic.id
            )
        )

        if (isCompleted && addToRevision) {
            ensureRevision(topic.id, subjectName, topic.name, "Topic")
        } else if (!isCompleted) {
            pauseActiveRevisions(topic.id)
        }
    }

    suspend fun toggleSubtopicCompletion(
        subtopic: SubtopicEntity,
        subjectName: String,
        isCompleted: Boolean,
        addToRevision: Boolean = false
    ) {
        val updated = subtopic.copy(isCompleted = isCompleted)
        syllabusDao.updateSubtopic(updated)
        syncDao.insertSyncTask(
            SyncQueueEntity(
                operationType = "UPDATE",
                entityType = "SUBTOPIC",
                entityId = subtopic.id
            )
        )

        if (isCompleted && addToRevision) {
            ensureRevision(subtopic.id, subjectName, subtopic.name, "Subtopic")
        } else if (!isCompleted) {
            pauseActiveRevisions(subtopic.id)
        }
    }

    /**
     * Idempotent: if a revision for [relatedId] already exists, activate the
     * highest-level one and prune duplicates. Otherwise create a new one.
     */
    private suspend fun ensureRevision(
        relatedId: String,
        subjectName: String,
        title: String,
        type: String
    ) {
        val existing = revisionDao.getRevisionsByRelatedId(relatedId)
        if (existing.isEmpty()) {
            val newRevision = RevisionEntity(
                id = UUID.randomUUID().toString(),
                relatedId = relatedId,
                subjectName = subjectName,
                title = title,
                type = type,
                priority = "Medium",
                estimatedMinutes = if (type == "Topic") 15 else 10,
                scheduledDateMillis = System.currentTimeMillis() + 86_400_000L,
                repetitionLevel = 1,
                isCompleted = false,
                isActive = true
            )
            revisionDao.insertRevision(newRevision)
            syncDao.insertSyncTask(
                SyncQueueEntity(
                    operationType = "INSERT",
                    entityType = "REVISION",
                    entityId = newRevision.id
                )
            )
        } else {
            val sorted = existing.sortedWith(
                compareByDescending<RevisionEntity> { it.repetitionLevel }
                    .thenByDescending { it.scheduledDateMillis }
            )
            val primary = sorted.first()
            if (!primary.isActive) {
                val activated = primary.copy(
                    isActive = true,
                    scheduledDateMillis = System.currentTimeMillis() + 86_400_000L
                )
                revisionDao.updateRevision(activated)
                syncDao.insertSyncTask(
                    SyncQueueEntity(
                        operationType = "UPDATE",
                        entityType = "REVISION",
                        entityId = primary.id
                    )
                )
            }
            // Prune duplicates
            sorted.drop(1).forEach { dup ->
                revisionDao.deleteRevision(dup)
                syncDao.insertSyncTask(
                    SyncQueueEntity(
                        operationType = "DELETE",
                        entityType = "REVISION",
                        entityId = dup.id
                    )
                )
            }
        }
    }

    private suspend fun pauseActiveRevisions(relatedId: String) {
        val existing = revisionDao.getRevisionsByRelatedId(relatedId)
        for (rev in existing) {
            if (!rev.isCompleted && rev.isActive) {
                val paused = rev.copy(isActive = false)
                revisionDao.updateRevision(paused)
                syncDao.insertSyncTask(
                    SyncQueueEntity(
                        operationType = "UPDATE",
                        entityType = "REVISION",
                        entityId = paused.id
                    )
                )
            }
        }
    }
}
