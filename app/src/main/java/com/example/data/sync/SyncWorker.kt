package com.example.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDb: AppDatabase,
    private val syncDao: SyncDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        if (network == null) {
            return@withContext Result.retry()
        }

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            // No user logged in — skip sync (user needs to sign in first)
            return@withContext Result.success()
        }
        val userId = auth.currentUser?.uid ?: return@withContext Result.retry()
        val db = FirebaseFirestore.getInstance()
        val database = AppDatabase.getDatabase(applicationContext)
        val syncDao = database.syncDao()
        
        val pendingTasks = syncDao.getPendingSyncQueueSync()
        if (pendingTasks.isEmpty()) {
            return@withContext Result.success()
        }

        var allSuccess = true

        for (task in pendingTasks) {
            try {
                processTask(task, database, db, userId)
                syncDao.updateSyncTask(task.copy(syncStatus = "COMPLETED"))
            } catch (e: Exception) {
                e.printStackTrace()
                val retryCount = task.retryCount + 1
                if (retryCount >= 5) {
                    syncDao.updateSyncTask(task.copy(syncStatus = "FAILED", retryCount = retryCount))
                } else {
                    syncDao.updateSyncTask(task.copy(retryCount = retryCount))
                }
                allSuccess = false
            }
        }

        syncDao.clearCompletedTasks()

        if (allSuccess) Result.success() else Result.retry()
    }

    private suspend fun processTask(task: SyncQueueEntity, localDb: AppDatabase, firestore: FirebaseFirestore, userId: String) {
        val collectionName = getCollectionNameForTask(task.entityType)
        if (collectionName.isEmpty()) throw IllegalArgumentException("Unknown entity type: ${task.entityType}")

        val docRef = firestore.collection("users").document(userId).collection(collectionName).document(task.entityId)

        if (task.operationType == "DELETE") {
            docRef.delete().await()
        } else {
            val entityData = getEntityData(localDb, task)
            if (entityData != null) {
                docRef.set(entityData).await()
            }
        }
    }

    private fun getCollectionNameForTask(entityType: String): String = when (entityType) {
        "SUBJECT" -> "subjects"
        "TOPIC" -> "topics"
        "SUBTOPIC" -> "subtopics"
        "REVISION" -> "revisions"
        "FOCUS_SESSION" -> "focus_sessions"
        "EXAM" -> "exams"
        "PLANNER" -> "planner"
        else -> ""
    }

    private suspend fun getEntityData(localDb: AppDatabase, task: SyncQueueEntity): Any? {
        return when (task.entityType) {
            "SUBJECT" -> localDb.syllabusDao().getSubjectById(task.entityId)
            "TOPIC" -> localDb.syllabusDao().getTopicById(task.entityId)
            "SUBTOPIC" -> localDb.syllabusDao().getSubtopicById(task.entityId)
            "REVISION" -> localDb.revisionDao().getRevisionById(task.entityId)
            "FOCUS_SESSION" -> localDb.focusDao().getSessionById(task.entityId)
            "EXAM" -> localDb.examDao().getExamById(task.entityId)
            "PLANNER" -> localDb.plannerDao().getPlanSync(task.entityId)
            else -> null
        }
    }
}
