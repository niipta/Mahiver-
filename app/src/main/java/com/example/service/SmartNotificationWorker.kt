package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@androidx.hilt.work.HiltWorker
class SmartNotificationWorker @dagger.assisted.AssistedInject constructor(
    @dagger.assisted.Assisted private val context: Context,
    @dagger.assisted.Assisted params: WorkerParameters,
    private val db: AppDatabase,
    private val settingsRepository: com.example.data.SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!settingsRepository.notificationsEnabled.value) {
            return Result.success()
        }
        
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Don't disturb between 11 PM and 9 AM except for the specific 11 PM planner alert
        if (currentHour in 0..8) {
            return Result.success()
        }

        
        // 1. Check for 11 PM Planner Reminder
        if (currentHour == 23) {
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val tomorrowStr = dateFormat.format(tomorrow.time)
            
            val planner = db.plannerDao().getPlan(tomorrowStr).first()
            if (planner == null || planner.plannedTopicIds.isBlank()) {
                showNotification(
                    id = 1100,
                    title = "Kal ka plan ready hai?",
                    text = "Kal ka planner abhi pending hai. Schedule your targets before sleep."
                )
                return Result.success()
            }
            return Result.success()
        }

        // 2. Daytime Smart Insights (randomized or prioritized)
        // Rate limiting: We only show max 1 insight per run (runs every 3-4 hours)
        
        // Check for Overdue Revisions first (High Priority)
        val allRevisions = db.revisionDao().getAllRevisions().first()
        val overdueRevisions = allRevisions.filter { !it.isCompleted && it.isActive && it.scheduledDateMillis < System.currentTimeMillis() }
        if (overdueRevisions.isNotEmpty()) {
            val sample = overdueRevisions.random()
            showNotification(
                id = 1101,
                title = "Revision Penalty \uD83D\uDEA8",
                text = "${sample.title} ka revision overdue hai. Clear your backlog."
            )
            return Result.success()
        }

        // Check for weak topics
        val subjectsWithTopics = db.syllabusDao().getAllSubjectsWithTopics().first()
        val weakTopics = subjectsWithTopics.flatMap { it.topics.map { t -> t.topic } }.filter { it.isWeak && !it.isCompleted }
        if (weakTopics.isNotEmpty() && currentHour in 14..18 && Math.random() > 0.5) {
            val sample = weakTopics.random()
            showNotification(
                id = 1102,
                title = "Focus Area \uD83D\uDD0D",
                text = "${sample.name} weak chal raha hai. Extra focus is needed here today."
            )
            return Result.success()
        }
        
        val sessions = db.focusDao().getAllSessions().first()

        // Check for Ignored Subjects (no activity for >= 2 days)
        val twoDaysAgo = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
        val activeRecentSubjectIds = sessions
            .filter { it.timestamp >= twoDaysAgo && it.subjectId != null }
            .map { it.subjectId!! }
            .toSet()
        val ignoredSubjects = subjectsWithTopics
            .filter { it.subject.id !in activeRecentSubjectIds && it.topics.any { t -> !t.isFullyCompleted } }
        if (ignoredSubjects.isNotEmpty() && currentHour in 10..20) {
            val sample = ignoredSubjects.random()
            showNotification(
                id = 1105,
                title = "Subject Alert \uD83D\uDCBB",
                text = "Last 2 din se ${sample.subject.name} par focus nahi hua hai. Don't let the backlog grow."
            )
            return Result.success()
        }

        // Focus Consistency Check (Compare yesterday and today)
        if (currentHour in 19..22) { // Evening analysis
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }.timeInMillis
            
            val yesterdayStart = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }.timeInMillis
            
            val sessions = db.focusDao().getAllSessions().first()
            
            val todaySessions = sessions.filter { it.timestamp >= todayStart }
            val yesterdaySessions = sessions.filter { it.timestamp in yesterdayStart until todayStart }
            
            val todayFocusMin = todaySessions.sumOf { it.actualDurationSeconds } / 60
            val yesterdayFocusMin = yesterdaySessions.sumOf { it.actualDurationSeconds } / 60
            
            if (yesterdayFocusMin > 0 && todayFocusMin < yesterdayFocusMin / 2) {
                showNotification(
                    id = 1103,
                    title = "Stats Check \uD83D\uDCC9",
                    text = "Aaj focus sessions weak rahe. Kal ${yesterdayFocusMin} mins the, aaj sirf ${todayFocusMin} mins. Let's push!"
                )
                return Result.success()
            } else if (todayFocusMin > 0 && todayFocusMin > yesterdayFocusMin) {
                showNotification(
                    id = 1104,
                    title = "Great Consistency \uD83D\uDCC8",
                    text = "Aaj consistency kaafi achi rahi! (${todayFocusMin} mins focused). Keep it up."
                )
                return Result.success()
            }
        }

        return Result.success()
    }

    private fun showNotification(id: Int, title: String, text: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "smart_insights_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Smart Insights",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "AI generated study insights and reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.drawable.ic_notification_timer)
            .setColorized(true)
            .setColor(android.graphics.Color.parseColor("#D4A853"))
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
