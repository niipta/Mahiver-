package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker

/**
 * SyncWorker does a FULL sync — pushes ALL local data to Firestore.
 * This ensures that restored data (from JSON backup) also gets synced.
 *
 * Also processes the pending sync queue for incremental updates.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDb: AppDatabase,
    private val syncDao: SyncDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "SyncWorker started")

        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        if (network == null) {
            Log.w(TAG, "No network — retrying")
            return@withContext Result.retry()
        }

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Log.w(TAG, "No user logged in — skipping sync")
            return@withContext Result.success()
        }
        val userId = auth.currentUser?.uid ?: return@withContext Result.retry()
        val userName = auth.currentUser?.displayName ?: "User"
        val userEmail = auth.currentUser?.email ?: ""

        val db = FirebaseFirestore.getInstance()
        val database = AppDatabase.getDatabase(applicationContext)

        try {
            // === FULL SYNC — push everything to Firestore ===
            Log.d(TAG, "Starting full sync for user: $userId")

            // 1. Sync subjects
            val subjects = database.syllabusDao().getAllSubjectsSync()
            subjects.forEach { subject ->
                try {
                    db.collection("users").document(userId).collection("subjects").document(subject.id).set(subject).await()
                } catch (e: Exception) { Log.e(TAG, "Failed to sync subject ${subject.id}", e) }
            }
            Log.d(TAG, "Synced ${subjects.size} subjects")

            // 2. Sync topics
            val topics = database.syllabusDao().getAllTopicsSync()
            topics.forEach { topic ->
                try {
                    db.collection("users").document(userId).collection("topics").document(topic.id).set(topic).await()
                } catch (e: Exception) { Log.e(TAG, "Failed to sync topic ${topic.id}", e) }
            }
            Log.d(TAG, "Synced ${topics.size} topics")

            // 3. Sync subtopics
            val subtopics = database.syllabusDao().getAllSubtopicsSync()
            subtopics.forEach { subtopic ->
                try {
                    db.collection("users").document(userId).collection("subtopics").document(subtopic.id).set(subtopic).await()
                } catch (e: Exception) { Log.e(TAG, "Failed to sync subtopic ${subtopic.id}", e) }
            }
            Log.d(TAG, "Synced ${subtopics.size} subtopics")

            // 4. Sync revisions
            val revisions = database.revisionDao().getAllRevisionsSync()
            revisions.forEach { revision ->
                try {
                    db.collection("users").document(userId).collection("revisions").document(revision.id).set(revision).await()
                } catch (e: Exception) { Log.e(TAG, "Failed to sync revision ${revision.id}", e) }
            }
            Log.d(TAG, "Synced ${revisions.size} revisions")

            // 5. Sync focus sessions
            val sessions = database.focusDao().getAllSessionsSync()
            sessions.forEach { session ->
                try {
                    db.collection("users").document(userId).collection("focus_sessions").document(session.id).set(session).await()
                } catch (e: Exception) { Log.e(TAG, "Failed to sync session ${session.id}", e) }
            }
            Log.d(TAG, "Synced ${sessions.size} focus sessions")

            // 6. Sync exams
            val exams = database.examDao().getAllExamsSync()
            exams.forEach { exam ->
                try {
                    db.collection("users").document(userId).collection("exams").document(exam.id).set(exam).await()
                } catch (e: Exception) { Log.e(TAG, "Failed to sync exam ${exam.id}", e) }
            }
            Log.d(TAG, "Synced ${exams.size} exams")

            // 7. Sync daily plans
            val plans = database.plannerDao().getAllDailyPlansSync()
            plans.forEach { plan ->
                try {
                    db.collection("users").document(userId).collection("planner").document(plan.dateString).set(plan).await()
                } catch (e: Exception) { Log.e(TAG, "Failed to sync plan ${plan.dateString}", e) }
            }
            Log.d(TAG, "Synced ${plans.size} daily plans")

            // 8. Sync mock tests
            try {
                val mockTests = database.mockDao().getAllMockTestsSync()
                mockTests.forEach { test ->
                    try {
                        db.collection("users").document(userId).collection("mock_tests").document(test.id).set(test).await()
                    } catch (e: Exception) { Log.e(TAG, "Failed to sync mock test ${test.id}", e) }
                }
                Log.d(TAG, "Synced ${mockTests.size} mock tests")
            } catch (e: Exception) { Log.e(TAG, "Mock tests sync failed", e) }

            // 9. Sync mock questions
            try {
                val mockQuestions = database.mockDao().getAllMockQuestionsSync()
                mockQuestions.forEach { question ->
                    try {
                        db.collection("users").document(userId).collection("mock_questions").document(question.id).set(question).await()
                    } catch (e: Exception) { Log.e(TAG, "Failed to sync mock question ${question.id}", e) }
                }
                Log.d(TAG, "Synced ${mockQuestions.size} mock questions")
            } catch (e: Exception) { Log.e(TAG, "Mock questions sync failed", e) }

            // 10. Sync user profile (for leaderboard)
            val settingsRepo = com.example.data.SettingsRepository.getInstance(applicationContext)
            val lifetimeFocusMinutes = sessions.filter { it.sessionType in listOf("Focus", "Study") }.sumOf { it.actualDurationSeconds / 60 }
            val topicsCompleted = subjects.sumOf { it.completedTopics }
            val revisionsDone = revisions.count { it.isCompleted }
            val currentStreak = settingsRepo.currentStreak.value
            val totalPoints = lifetimeFocusMinutes + (topicsCompleted * 50L) + (currentStreak * 10L) + (revisionsDone * 20L)

            try {
                val profileData = mapOf(
                    "uid" to userId,
                    "name" to (settingsRepo.userName.value.ifBlank { userName }),
                    "email" to userEmail,
                    "points" to totalPoints,
                    "streak" to currentStreak,
                    "isSubscribed" to false,
                    "subscriptionExpiry" to 0L,
                    "isBlocked" to false,
                    "createdAt" to System.currentTimeMillis(),
                    "lastSyncAt" to System.currentTimeMillis()
                )
                db.collection("users").document(userId).collection("profile").document("profile").set(profileData).await()
                Log.d(TAG, "Synced user profile: points=$totalPoints, streak=$currentStreak")
            } catch (e: Exception) { Log.e(TAG, "Failed to sync user profile", e) }

            // 11. Process pending sync queue (for deletes/updates)
            val pendingTasks = syncDao.getPendingSyncQueueSync()
            for (task in pendingTasks) {
                try {
                    processTask(task, database, db, userId)
                    syncDao.updateSyncTask(task.copy(syncStatus = "COMPLETED"))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process sync task ${task.entityType}/${task.entityId}", e)
                    val retryCount = task.retryCount + 1
                    if (retryCount >= 5) {
                        syncDao.updateSyncTask(task.copy(syncStatus = "FAILED", retryCount = retryCount))
                    } else {
                        syncDao.updateSyncTask(task.copy(retryCount = retryCount))
                    }
                }
            }
            syncDao.clearCompletedTasks()

            Log.d(TAG, "Full sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed", e)
            Result.retry()
        }
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
        "MOCK_TEST" -> "mock_tests"
        "MOCK_QUESTION" -> "mock_questions"
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
