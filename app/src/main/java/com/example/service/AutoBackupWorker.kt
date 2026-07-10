package com.example.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.BackupData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@androidx.hilt.work.HiltWorker
class AutoBackupWorker @dagger.assisted.AssistedInject constructor(
    @dagger.assisted.Assisted private val appContext: Context,
    @dagger.assisted.Assisted workerParams: WorkerParameters,
    private val backupRepository: com.example.data.BackupRepository,
    private val settingsRepository: com.example.data.SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)

            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val backupAdapter = moshi.adapter(BackupData::class.java)

            // Plain JSON backup (no encryption — portable across reinstalls)
            val backupData = BackupData(
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

            val jsonStr = backupAdapter.indent("  ").toJson(backupData)

            // Save to app's external files dir (Documents or backups folder)
            val backupDir = File(applicationContext.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
            val fileName = "AutoBackup_${dateFormat.format(Date())}.json"

            val file = File(backupDir, fileName)
            file.writeText(jsonStr)

            // Keep only the last 5 backups to avoid using too much storage
            val oldBackups = backupDir.listFiles()?.filter { it.name.startsWith("AutoBackup_") }
            if (oldBackups != null && oldBackups.size > 5) {
                oldBackups.sortedBy { it.lastModified() }
                    .take(oldBackups.size - 5)
                    .forEach { it.delete() }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
