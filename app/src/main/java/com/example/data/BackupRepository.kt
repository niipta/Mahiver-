package com.example.data

import android.content.Context
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Backup & restore repository.
 *
 * Stores backups as plain JSON (version 2). The old v1 approach used
 * AndroidKeystore encryption which was device-locked — uninstalling the app
 * deleted the key, making all backups unreadable. Plain JSON is portable
 * across devices and survives reinstalls, which is what users actually need.
 *
 * The backup file is saved with a `.json` extension and can be inspected in
 * any text editor. This is a deliberate trade-off: no encryption, but the file
 * works after uninstall/reinstall and across devices.
 */
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
                    exams = db.examDao().getAllExamsSync(),
                    mockTests = db.mockDao().getAllMockTestsSync(),
                    mockQuestions = db.mockDao().getAllMockQuestionsSync()
                )

                // Pretty-printed JSON so users can inspect the file if needed.
                val jsonStr = backupAdapter.indent("  ").toJson(backupData)

                context.contentResolver.openOutputStream(uri)?.use { it.write(jsonStr.toByteArray(Charsets.UTF_8)) }
                    ?: return@withContext Result.failure(IllegalStateException("Could not write to file"))

                val summary = "${backupData.subjects.size} subjects, ${backupData.focusSessions.size} sessions, ${backupData.mockTests.size} mocks"
                Result.success(summary)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    suspend fun restoreBackup(uri: android.net.Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)

                // Read the file as text
                val fileContent = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: return@withContext Result.failure(IllegalArgumentException("Could not read file"))

                if (fileContent.isBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("File is empty"))
                }

                // Parse JSON
                val backupData = try {
                    backupAdapter.fromJson(fileContent)
                } catch (e: Exception) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid backup format: not a valid MahirVerse backup file"))
                } ?: return@withContext Result.failure(IllegalArgumentException("Invalid backup format: empty or corrupt file"))

                // Validate it has the required fields
                if (backupData.subjects == null) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid backup: missing subjects data"))
                }

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
                    // Restore mock data (v2 backups only; v1 backups have empty lists)
                    backupData.mockTests.forEach { db.mockDao().insertMockTest(it) }
                    backupData.mockQuestions.forEach { db.mockDao().insertQuestionLog(it) }
                }

                val summary = "${backupData.subjects.size} subjects, ${backupData.focusSessions.size} sessions, ${backupData.mockTests.size} mocks"
                Result.success(summary)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}
