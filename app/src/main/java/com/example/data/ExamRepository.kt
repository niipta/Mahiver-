package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

import androidx.room.withTransaction
class ExamRepository(
    private val db: AppDatabase,
    private val syncDao: com.example.data.sync.SyncDao
) {
    private val examDao = db.examDao()
    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()

    suspend fun insertExam(exam: ExamEntity) {
        db.withTransaction {
        examDao.insertExam(exam)
        syncDao.insertSyncTask(SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            entityType = "exams", 
            entityId = exam.id,
            operationType = "INSERT", 
            syncStatus = "PENDING",
            queuedAt = System.currentTimeMillis()
        ))
    }
    }

    suspend fun deleteExam(exam: ExamEntity) {
        db.withTransaction {
        examDao.deleteExam(exam)
        syncDao.insertSyncTask(SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            entityType = "exams", 
            entityId = exam.id,
            operationType = "DELETE", 
            syncStatus = "PENDING",
            queuedAt = System.currentTimeMillis()
        ))
    }
    }

    suspend fun updateExam(exam: ExamEntity) {
        db.withTransaction {
            examDao.updateExam(exam)
            syncDao.insertSyncTask(SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "exams", 
                entityId = exam.id,
                operationType = "UPDATE", 
                syncStatus = "PENDING",
                queuedAt = System.currentTimeMillis()
            ))
        }
    }
}
