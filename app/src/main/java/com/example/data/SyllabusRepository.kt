package com.example.data

import com.example.data.sync.SyncDao
import kotlinx.coroutines.flow.Flow

import androidx.room.withTransaction
class SyllabusRepository(private val db: AppDatabase, private val syncDao: SyncDao) {
    private val syllabusDao = db.syllabusDao()
    val allSubjects: Flow<List<SubjectWithTopics>> = syllabusDao.getAllSubjectsWithTopics()

    suspend fun insertSubject(subject: SubjectEntity) {
        db.withTransaction {
        syllabusDao.insertSubject(subject)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "INSERT", entityType = "SUBJECT", entityId = subject.id))
    }
    }
    suspend fun updateSubject(subject: SubjectEntity) {
        db.withTransaction {
        syllabusDao.updateSubject(subject)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "UPDATE", entityType = "SUBJECT", entityId = subject.id))
    }
    }
    suspend fun deleteSubject(subject: SubjectEntity) {
        db.withTransaction {
        syllabusDao.deleteSubject(subject)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "DELETE", entityType = "SUBJECT", entityId = subject.id))
    }
    }

    suspend fun insertTopic(topic: TopicEntity) {
        db.withTransaction {
        syllabusDao.insertTopic(topic)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "INSERT", entityType = "TOPIC", entityId = topic.id))
    }
    }
    suspend fun updateTopic(topic: TopicEntity) {
        db.withTransaction {
        syllabusDao.updateTopic(topic)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "UPDATE", entityType = "TOPIC", entityId = topic.id))
    }
    }
    suspend fun deleteTopic(topic: TopicEntity) {
        db.withTransaction {
        syllabusDao.deleteTopic(topic)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "DELETE", entityType = "TOPIC", entityId = topic.id))
    }
    }

    suspend fun insertSubtopic(subtopic: SubtopicEntity) {
        db.withTransaction {
        syllabusDao.insertSubtopic(subtopic)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "INSERT", entityType = "SUBTOPIC", entityId = subtopic.id))
    }
    }
    suspend fun updateSubtopic(subtopic: SubtopicEntity) {
        db.withTransaction {
        syllabusDao.updateSubtopic(subtopic)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "UPDATE", entityType = "SUBTOPIC", entityId = subtopic.id))
    }
    }
    suspend fun deleteSubtopic(subtopic: SubtopicEntity) {
        db.withTransaction {
        syllabusDao.deleteSubtopic(subtopic)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "DELETE", entityType = "SUBTOPIC", entityId = subtopic.id))
    }
    }
}
