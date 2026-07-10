package com.example.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker
import com.example.data.SettingsRepository
import com.example.data.FocusDao

@HiltWorker
class StreakWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val focusDao: FocusDao,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationsEnabled = settingsRepository.notificationsEnabled.value
        val goalMetDates = settingsRepository.goalMetDates.value
        val allSessions = focusDao.getAllSessionsSync()
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayStr = sdf.format(java.util.Date())
        
        val streakResult = com.example.domain.StreakCalculator.compute(
            sessions = allSessions,
            goalMetDates = goalMetDates,
            todayStr = todayStr,
            dailyGoalMinutes = settingsRepository.dailyGoalMinutes.value
        )

        val computedStreak = streakResult.currentStreak
        val computedLongestStreak = streakResult.longestStreak
        val streakDropped = streakResult.missedYesterday

        if (streakDropped && notificationsEnabled) {
            showNotification(
                "Streak Reset",
                "You missed your study goal yesterday. Your streak has reset. Let's bounce back today!"
            )
        }

        // Persist computed streak
        settingsRepository.updateStreak(computedStreak)
        if (computedLongestStreak > settingsRepository.longestStreak.value) {
            settingsRepository.updateLongestStreak(computedLongestStreak)
        }
        return Result.success()
    }
    
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "mahirverse_channel",
                "Study Streak",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Streak reminders and updates"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        createNotificationChannel()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "mahirverse_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(com.example.R.drawable.ic_notification_streak)
            .setColorized(true)
            .setColor(android.graphics.Color.parseColor("#D4A853"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, builder.build())
    }
}
