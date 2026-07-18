package com.example.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import androidx.room.withTransaction
import kotlinx.coroutines.withContext

class RestoreWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
            } catch (e: Exception) {
                return@withContext Result.retry()
            }
        }
        
        val userId = auth.currentUser?.uid ?: return@withContext Result.retry()
        val db = FirebaseFirestore.getInstance()
        val localDb = AppDatabase.getDatabase(applicationContext)
        
        try {
            // 1. Fetch ALL cloud collections into memory first
            val subjectsSnapshot = db.collection("users").document(userId).collection("subjects").get().await()
            val topicsSnapshot = db.collection("users").document(userId).collection("topics").get().await()
            val subtopicsSnapshot = db.collection("users").document(userId).collection("subtopics").get().await()
            val revisionsSnapshot = db.collection("users").document(userId).collection("revisions").get().await()
            val focusSnapshot = db.collection("users").document(userId).collection("focus_sessions").get().await()
            val plannerSnapshot = db.collection("users").document(userId).collection("planner").get().await()
            val examsSnapshot = db.collection("users").document(userId).collection("exams").get().await()
            val userDoc = db.collection("users").document(userId).get().await()

            // 2. Only if fetch succeeded, upsert into local DB
            localDb.withTransaction {
                
                userDoc.get("unlockedAchievements")?.let { list ->
                    if (list is List<*>) {
                        val set = list.filterIsInstance<String>().toSet()
                        val prefs = applicationContext.getSharedPreferences("mahirverse_settings", Context.MODE_PRIVATE)
                        val current = prefs.getStringSet("unlocked_achievements", emptySet())?.toSet() ?: emptySet()
                        prefs.edit().putStringSet("unlocked_achievements", current + set).apply()
                        // SettingsRepository will reload this on next startup or we can update it if it's already instantiated
                    }
                }

                for (doc in subjectsSnapshot.documents) {
                    val entity = doc.toObject(SubjectEntity::class.java)
                    if (entity != null) localDb.syllabusDao().insertSubject(entity)
                }
                
                for (doc in topicsSnapshot.documents) {
                    val entity = doc.toObject(TopicEntity::class.java)
                    if (entity != null) localDb.syllabusDao().insertTopic(entity)
                }
                
                for (doc in subtopicsSnapshot.documents) {
                    val entity = doc.toObject(SubtopicEntity::class.java)
                    if (entity != null) localDb.syllabusDao().insertSubtopic(entity)
                }
                
                for (doc in revisionsSnapshot.documents) {
                    val entity = doc.toObject(RevisionEntity::class.java)
                    if (entity != null) localDb.revisionDao().insertRevision(entity)
                }
                
                for (doc in focusSnapshot.documents) {
                    val entity = doc.toObject(FocusSessionEntity::class.java)
                    if (entity != null) localDb.focusDao().insertSession(entity)
                }
                
                for (doc in plannerSnapshot.documents) {
                    val entity = doc.toObject(DailyPlanEntity::class.java)
                    if (entity != null) localDb.plannerDao().insertPlan(entity)
                }
                
                for (doc in examsSnapshot.documents) {
                    val entity = doc.toObject(ExamEntity::class.java)
                    if (entity != null) localDb.examDao().insertExam(entity)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("RestoreWorker", "Restore failed", e)
            Result.retry() // Instead of failure, let's retry later if there is network error
        }
    }
}
