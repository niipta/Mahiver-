package com.example.data

import android.content.Context
import androidx.room.withTransaction
import com.example.util.SecurityUtil
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupRepository(private val context: Context) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val backupAdapter = moshi.adapter(BackupData::class.java)

    suspend fun createBackup(uri: android.net.Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val settingsRepo = SettingsRepository.getInstance(context)

                val backupData = BackupData(
                    settings = BackupSettings(
                        userName = settingsRepo.userName.value,
                        soundEnabled = settingsRepo.soundEnabled.value
                    ),
                    subjects = db.syllabusDao().getAllSubjectsSync(),
                    topics = db.syllabusDao().getAllTopicsSync(),
                    subtopics = db.syllabusDao().getAllSubtopicsSync(),
                    revisions = db.revisionDao().getAllRevisionsSync(),
                    focusSessions = db.focusDao().getAllSessionsSync(),
                    dailyPlans = db.plannerDao().getAllDailyPlansSync(),
                    exams = db.examDao().getAllExamsSync()
                )

                val jsonStr = backupAdapter.toJson(backupData)
                val encryptedBlob = SecurityUtil.encryptData(jsonStr)

                context.contentResolver.openOutputStream(uri)?.use { it.write(encryptedBlob.toByteArray(Charsets.UTF_8)) }
                Result.success(encryptedBlob)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    suspend fun restoreBackup(uri: android.net.Uri): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)

                val jsonStr = try {
                    val encryptedBlob = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                    SecurityUtil.decryptData(encryptedBlob)
                } catch (e: IllegalArgumentException) {
                    return@withContext Result.failure(e)
                } catch (e: Exception) {
                    return@withContext Result.failure(IllegalArgumentException("Wrong key or corrupted file"))
                }

                val backupData = backupAdapter.fromJson(jsonStr)
                    ?: return@withContext Result.failure(IllegalArgumentException("Invalid backup format"))

                db.withTransaction {
                    backupData.settings?.let { backupSettings ->
                        val settingsRepo = SettingsRepository.getInstance(context)
                        settingsRepo.updateUserName(backupSettings.userName)
                        settingsRepo.updateSoundEnabled(backupSettings.soundEnabled)
                    }

                    db.clearAllTables()

                    backupData.subjects.forEach { db.syllabusDao().insertSubject(it) }
                    backupData.topics.forEach { db.syllabusDao().insertTopic(it) }
                    backupData.subtopics.forEach { db.syllabusDao().insertSubtopic(it) }
                    backupData.revisions.forEach { db.revisionDao().insertRevision(it) }
                    backupData.focusSessions.forEach { db.focusDao().insertSession(it) }
                    backupData.dailyPlans.forEach { db.plannerDao().insertPlan(it) }
                    backupData.exams.forEach { db.examDao().insertExam(it) }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}
