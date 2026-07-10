package com.example.data

import com.example.data.sync.SyncDao
import kotlinx.coroutines.flow.Flow

import androidx.room.withTransaction
class RevisionRepository(private val db: AppDatabase, private val syncDao: SyncDao) {
    private val revisionDao = db.revisionDao()
    val allRevisions: Flow<List<RevisionEntity>> = revisionDao.getAllRevisions()

    suspend fun getRevisionsByRelatedId(relatedId: String): List<RevisionEntity> {
        return revisionDao.getRevisionsByRelatedId(relatedId)
    }

    suspend fun insertRevision(revision: RevisionEntity) {
        db.withTransaction {
        revisionDao.insertRevision(revision)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "INSERT", entityType = "REVISION", entityId = revision.id))
    }
    }
    
    suspend fun updateRevision(revision: RevisionEntity) {
        db.withTransaction {
        revisionDao.updateRevision(revision)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "UPDATE", entityType = "REVISION", entityId = revision.id))
    }
    }
    
    suspend fun deleteRevision(revision: RevisionEntity) {
        db.withTransaction {
        revisionDao.deleteRevision(revision)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "DELETE", entityType = "REVISION", entityId = revision.id))
    }
    }
}
