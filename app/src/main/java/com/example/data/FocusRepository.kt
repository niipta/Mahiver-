package com.example.data

import com.example.data.sync.SyncDao
import kotlinx.coroutines.flow.Flow

import androidx.room.withTransaction
class FocusRepository(private val db: AppDatabase, private val syncDao: SyncDao) {
    private val focusDao = db.focusDao()
    val allSessions: Flow<List<FocusSessionEntity>> = focusDao.getAllSessions()

    suspend fun insertSession(session: FocusSessionEntity) {
        db.withTransaction {
        focusDao.insertSession(session)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "INSERT", entityType = "FOCUS_SESSION", entityId = session.id))
    }
    }

    suspend fun updateSession(session: FocusSessionEntity) {
        db.withTransaction {
            focusDao.updateSession(session)
            syncDao.insertSyncTask(SyncQueueEntity(
                id = java.util.UUID.randomUUID().toString(),
                entityType = "focus_sessions", 
                entityId = session.id,
                operationType = "UPDATE", 
                syncStatus = "PENDING",
                queuedAt = System.currentTimeMillis()
            ))
        }
    }

    suspend fun deleteSession(session: FocusSessionEntity) {
        db.withTransaction {
            focusDao.deleteSession(session)
            syncDao.insertSyncTask(SyncQueueEntity(
                id = java.util.UUID.randomUUID().toString(),
                entityType = "focus_sessions", 
                entityId = session.id,
                operationType = "DELETE", 
                syncStatus = "PENDING",
                queuedAt = System.currentTimeMillis()
            ))
        }
    }
}
