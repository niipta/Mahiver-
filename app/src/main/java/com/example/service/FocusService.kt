package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FocusService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaSession: android.support.v4.media.session.MediaSessionCompat? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_ADD_TIME = "ACTION_ADD_TIME"
        const val ACTION_SKIP_BREAK = "ACTION_SKIP_BREAK"
        const val ACTION_START_BREAK = "ACTION_START_BREAK"
        const val ACTION_START_FOCUS = "ACTION_START_FOCUS"
        const val ACTION_COMPLETE_TOPIC = "ACTION_COMPLETE_TOPIC"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "focus_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        mediaSession = android.support.v4.media.session.MediaSessionCompat(this, "FocusService").apply {
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val targetId = if (TimerManager.timerState.value == TimerState.COMPLETED) NOTIFICATION_ID + 1 else NOTIFICATION_ID
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(targetId, buildNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(targetId, buildNotification())
        }
        
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopService()
            ACTION_ADD_TIME -> addTime()
            ACTION_SKIP_BREAK -> skipBreak()
            ACTION_START_BREAK -> startBreak()
            ACTION_START_FOCUS -> startFocus()
            ACTION_COMPLETE_TOPIC -> completeTopic()
        }
        return START_STICKY
    }

    private fun enableDndIfRequested() {
        if (TimerManager.sessionType.value != SessionType.FOCUS) return
        val prefs = getSharedPreferences("mahirverse_settings", Context.MODE_PRIVATE)
        val autoEnable = prefs.getBoolean("auto_enable_dnd", false)
        if (autoEnable) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                // Save current filter if we haven't already in this session
                if (!prefs.contains("saved_dnd_filter")) {
                    prefs.edit().putInt("saved_dnd_filter", notificationManager.currentInterruptionFilter).apply()
                }
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            }
        }
    }

    private fun restoreDnd() {
        val prefs = getSharedPreferences("mahirverse_settings", Context.MODE_PRIVATE)
        if (prefs.contains("saved_dnd_filter")) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val savedFilter = prefs.getInt("saved_dnd_filter", NotificationManager.INTERRUPTION_FILTER_ALL)
                notificationManager.setInterruptionFilter(savedFilter)
            }
            prefs.edit().remove("saved_dnd_filter").apply()
        }
    }

    private fun startTimer() {
        enableDndIfRequested()
        acquireWakeLock()
        TimerManager.updateState(TimerState.RUNNING)
        runTimer()
    }

    private fun pauseTimer() {
        restoreDnd()
        TimerManager.incrementInterruptions()
        TimerManager.updateState(TimerState.PAUSED)
        timerJob?.cancel()
        updateNotification()
        
        releaseWakeLock()
    }

    private fun resumeTimer() {
        enableDndIfRequested()
        acquireWakeLock()
        TimerManager.updateState(TimerState.RUNNING)
        
        runTimer()
    }
    
    private fun addTime() {
        // CRITICAL FIX: use addTime() so originalDurationSeconds is also extended,
        // otherwise saved session duration would be wrong (bug fix).
        TimerManager.addTime(5 * 60L)
        updateNotification()
    }
    
    private fun skipBreak() {
        if (TimerManager.sessionType.value != SessionType.FOCUS) {
            finishSession()
        }
    }

    private fun startBreak() {
        if (TimerManager.focusSessionCount.value % 4 == 0) {
            TimerManager.setSessionType(SessionType.LONG_BREAK)
        } else {
            TimerManager.setSessionType(SessionType.SHORT_BREAK)
        }
        TimerManager.resetCurrentSession()
        startTimer()
    }
    
    private fun startFocus() {
        TimerManager.setSessionType(SessionType.FOCUS)
        TimerManager.resetCurrentSession()
        startTimer()
    }

    private fun completeTopic() {
        val subtopicId = TimerManager.selectedSubtopicId.value
        val topicId = TimerManager.selectedTopicId.value
        val subjectName = TimerManager.selectedSubjectName.value ?: "Subject"
        
        if (subtopicId != null || topicId != null) {
            serviceScope.launch {
                val db = com.example.data.AppDatabase.getDatabase(applicationContext)
                val completionManager = com.example.domain.CompletionManager(db.syllabusDao(), db.revisionDao(), db.syncDao())
                val syllabusDao = db.syllabusDao()
                
                if (subtopicId != null) {
                    val sub = syllabusDao.getSubtopicById(subtopicId)
                    if (sub != null && !sub.isCompleted) {
                        completionManager.toggleSubtopicCompletion(sub, subjectName, true)
                    }
                } else if (topicId != null) {
                    val topic = syllabusDao.getTopicById(topicId)
                    if (topic != null && !topic.isCompleted) {
                        completionManager.toggleTopicCompletion(topic, subjectName, true)
                    }
                }
                
                // Show a toast
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(applicationContext, "Topic marked as completed!", android.widget.Toast.LENGTH_SHORT).show()
                }
                
                updateNotification() // Refresh visually if needed
            }
        }
    }

    private fun stopService() {
        stopTimer(isAutoFinish = false)
        TimerManager.resetFocusSession()
        releaseWakeLock()
        
        timerJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        
        stopSelf()
    }

    private fun stopTimer(isAutoFinish: Boolean) {
        restoreDnd()
        if (TimerManager.timerState.value == TimerState.RUNNING || TimerManager.timerState.value == TimerState.PAUSED) {
            saveCompletedSession(manualStop = !isAutoFinish)
        }
        
        TimerManager.updateState(TimerState.STOPPED)
        if (!isAutoFinish) {
            TimerManager.resetCurrentSession()
        }
        timerJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        
        stopSelf()
        releaseWakeLock()
    }

    private fun finishSession() {
        restoreDnd()
        saveCompletedSession(manualStop = false)
        val isFocus = TimerManager.sessionType.value == SessionType.FOCUS
        if (isFocus) {
            TimerManager.incrementFocusSession()
        }
        playCompletionSound(isFocus)
        TimerManager.updateState(TimerState.COMPLETED)
        TimerManager.setSessionCompleted(true)
        updateNotification()
        if (isFocus) {
            showCompletionNotification()
        }
        releaseWakeLock()
    }

    private var sessionStartTimeMs: Long = 0L

    private var lastNotifUpdateMs = 0L

    private fun runTimer() {
        timerJob?.cancel()
        val currentRemainingSeconds = TimerManager.timeRemaining.value
        val totalDurationMs = TimerManager.getDurationMinutes(TimerManager.sessionType.value) * 60 * 1000L
        
        val expectedElapsedMs = totalDurationMs - (currentRemainingSeconds * 1000L)
        sessionStartTimeMs = System.currentTimeMillis() - expectedElapsedMs
        
        timerJob = serviceScope.launch {
            while (TimerManager.timerState.value == TimerState.RUNNING) {
                val elapsed = System.currentTimeMillis() - sessionStartTimeMs
                val remainingMs = java.lang.Long.max(0L, totalDurationMs - elapsed)
                TimerManager.updateTime(remainingMs / 1000L)
                
                val now = System.currentTimeMillis()
                if (now - lastNotifUpdateMs >= 1000) {
                    updateNotification()
                    sendBroadcast(android.content.Intent("com.example.widget.ACTION_TIMER_TICK"))
                    lastNotifUpdateMs = now
                }
                
                if (remainingMs <= 0L) {
                    finishSession()
                    break
                }
                delay(250) // Update 4x/sec for smooth UI, but time is wall-clock accurate
            }
        }
    }

    private fun playCompletionSound(isFocus: Boolean) {
        val prefs = applicationContext.getSharedPreferences("mahirverse_settings", Context.MODE_PRIVATE)
        
        try {
            if (prefs.getBoolean("sound_enabled", true)) {
                val uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = android.media.RingtoneManager.getRingtone(applicationContext, uri)
                ringtone?.play()
            }
            
            if (prefs.getBoolean("vibration_enabled", true)) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                }
                val pattern = if (isFocus) {
                    longArrayOf(0, 500, 200, 500)
                } else {
                    longArrayOf(0, 300, 150, 300, 150, 300)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCompletionNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val title = TimerManager.sessionType.value.title
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$title Completed!")
            .setContentText("Great job! Start your next session or break.")
            .setSmallIcon(com.example.R.drawable.ic_notification_timer)
            .setColorized(true)
            .setColor(android.graphics.Color.parseColor("#D4A853"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID + 1, builder.build())
    }

    private fun saveCompletedSession(manualStop: Boolean = false) {
        val initialSeconds = TimerManager.originalDurationSeconds
        val remainingSeconds = TimerManager.timeRemaining.value
        val actualSecondsSpent = (initialSeconds - remainingSeconds).toInt()
        
        if (actualSecondsSpent < 60) return // Don't save sessions under 1 minute

        val subjectId = TimerManager.selectedSubjectId.value
        val topicId = TimerManager.selectedTopicId.value
        val subtopicId = TimerManager.selectedSubtopicId.value
        val customTitle = TimerManager.customTaskTitle.value
        val interruptions = TimerManager.interruptions.value
        val sessionTypeStr = TimerManager.sessionType.value.title
        val duration = TimerManager.getDurationMinutes(TimerManager.sessionType.value)

        serviceScope.launch {
            val db = com.example.data.AppDatabase.getDatabase(applicationContext)
            
            var subjectName = "General Study"
            var topicName: String? = null
            
            if (subjectId != null) {
                db.syllabusDao().getSubjectById(subjectId)?.let { subjectName = it.name }
            }
            if (topicId != null) {
                db.syllabusDao().getTopicById(topicId)?.let { topicName = it.name }
            }
            if (subtopicId != null) {
                val subtopic = db.syllabusDao().getSubtopicById(subtopicId)
                if (subtopic != null) {
                    topicName = if (topicName != null) "$topicName - ${subtopic.name}" else subtopic.name
                }
            }
            
            if (customTitle != null && customTitle.isNotBlank()) {
                subjectName = "Custom Task"
                topicName = customTitle
            }

            val focusSession = com.example.data.FocusSessionEntity(
                id = java.util.UUID.randomUUID().toString(),
                subjectId = subjectId,
                topicId = topicId,
                subtopicId = subtopicId,
                subjectName = subjectName,
                topicName = topicName,
                durationMinutes = duration,
                actualDurationSeconds = actualSecondsSpent,
                interruptions = interruptions,
                sessionType = sessionTypeStr,
                isDeepFocus = interruptions == 0,
                timestamp = System.currentTimeMillis()
            )
            db.focusDao().insertSession(focusSession)
            db.syncDao().insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "FOCUS_SESSION", entityId = focusSession.id))
            
            // Minimum study duration criteria: 10 minutes focused time triggers revision
            if (actualSecondsSpent >= 10 * 60 && topicId != null && topicName != null && sessionTypeStr != "Revision") {
                val revSession = com.example.data.RevisionEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    relatedId = topicId,
                    subjectName = subjectName, 
                    title = topicName!!,
                    type = "Topic",
                    priority = if (interruptions > 1) "High" else "Medium", // More interruptions = higher priority
                    estimatedMinutes = 15,
                    scheduledDateMillis = System.currentTimeMillis() + 86400000L // 1 day later
                )
                db.revisionDao().insertRevision(revSession)
                db.syncDao().insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "REVISION", entityId = revSession.id))
            }
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MahirVerse:FocusWakeLock")
        }
        val sessionDurationMs = TimerManager.getDurationMinutes(TimerManager.sessionType.value) * 60 * 1000L
        wakeLock?.acquire(sessionDurationMs + 5 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
    }

    private val motivationalQuotes = listOf(
        "Stay Focused, MAHIR!",
        "You can do this!",
        "Keep going, you're doing great!",
        "Don't give up now!", 
        "Every minute counts for success.",
        "Success is built on deep focus.",
        "One step closer to your goal!",
        "Master your time, master your life."
    )

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val timeValue = TimerManager.timeRemaining.value
        val timeString = formatTime(timeValue)
        val sessionType = TimerManager.sessionType.value
        val timerState = TimerManager.timerState.value
        val focusSessionCount = TimerManager.focusSessionCount.value
        
        val sName = TimerManager.selectedSubjectName.value
        val tName = TimerManager.selectedTopicName.value
        val subName = TimerManager.selectedSubtopicName.value
        
        var hierarchy = ""
        val sNameF = sName ?: "General Study"
        val tNameF = tName ?: "No specific topic"
        
        hierarchy += "📚 $sNameF\n"
        hierarchy += "$tNameF"
        if (subName != null) {
            hierarchy += " → $subName"
        }
        
        val maxSeconds = TimerManager.getDurationMinutes(sessionType) * 60
        val maxS = kotlin.math.max(maxSeconds, 1)
        val elapsed = kotlin.math.max(maxS - timeValue.toInt(), 0)
        
        val channelIdStr = if (timerState == TimerState.COMPLETED) "focus_alerts_channel" else CHANNEL_ID
        val builder = NotificationCompat.Builder(this, channelIdStr)
            .setSmallIcon(com.example.R.drawable.ic_notification_timer)
            .setColorized(true)
            .setColor(android.graphics.Color.parseColor("#D4A853"))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val actionsList = mutableListOf<androidx.core.app.NotificationCompat.Action>()
        var compactActions = intArrayOf()

        if (timerState == TimerState.COMPLETED) {
            builder.setOngoing(false) // Allow swipe away when completed if we want? Actually, keep it ongoing until user chooses or kills service.
            
            if (sessionType == SessionType.FOCUS) {
                builder.setContentTitle("✅ Focus Session Completed")
                builder.setContentText("Great work! ${maxSeconds / 60} minutes completed.")
                
                val bigText = "Great work! ${maxSeconds / 60} minutes completed.\n\n$hierarchy"
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                
                val breakIntent = Intent(this, FocusService::class.java).setAction(ACTION_START_BREAK)
                actionsList.add(NotificationCompat.Action(R.drawable.ic_timer, "Start Break", PendingIntent.getService(this, 10, breakIntent, PendingIntent.FLAG_IMMUTABLE)))
                
                val nextIntent = Intent(this, FocusService::class.java).setAction(ACTION_START_FOCUS)
                actionsList.add(NotificationCompat.Action(R.drawable.ic_timer, "Start Next", PendingIntent.getService(this, 11, nextIntent, PendingIntent.FLAG_IMMUTABLE)))
                
                val completeIntent = Intent(this, FocusService::class.java).setAction(ACTION_COMPLETE_TOPIC)
                actionsList.add(NotificationCompat.Action(android.R.drawable.ic_menu_save, "Complete Topic", PendingIntent.getService(this, 12, completeIntent, PendingIntent.FLAG_IMMUTABLE)))
                
                compactActions = intArrayOf(0, 1)
            } else {
                builder.setContentTitle("🔔 Break Complete")
                builder.setContentText("Ready for the next focus session?")
                
                val bigText = "Ready for the next focus session?\nContinue studying:\n\n$hierarchy"
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                
                val focusIntent = Intent(this, FocusService::class.java).setAction(ACTION_START_FOCUS)
                actionsList.add(NotificationCompat.Action(R.drawable.ic_timer, "Resume Focus", PendingIntent.getService(this, 13, focusIntent, PendingIntent.FLAG_IMMUTABLE)))
                
                val completeIntent = Intent(this, FocusService::class.java).setAction(ACTION_COMPLETE_TOPIC)
                actionsList.add(NotificationCompat.Action(android.R.drawable.ic_menu_save, "Complete Topic", PendingIntent.getService(this, 14, completeIntent, PendingIntent.FLAG_IMMUTABLE)))
                
                compactActions = intArrayOf(0)
            }
        } else {
            builder.setCategory(NotificationCompat.CATEGORY_PROGRESS)
            builder.setProgress(maxS, elapsed, false)
            
            if (sessionType == SessionType.FOCUS) {
                builder.setContentTitle("⏱ $timeString • Deep Focus")
                builder.setContentText(hierarchy.replace("\n", " → "))

                val bigText = "⏱ $timeString Remaining\n\n$hierarchy\n\n📊 Progress: Session $focusSessionCount of 4"
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            } else {
                builder.setContentTitle("⏱ $timeString • Break")
                builder.setContentText("Relax and recharge")

                val bigText = "⏱ $timeString Remaining\nRelax and recharge.\n\nPrevious Topic:\n$hierarchy"
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            }

            // Provide specific labeled actions
            if (sessionType == SessionType.FOCUS) {
                val addIntent = Intent(this, FocusService::class.java).setAction(ACTION_ADD_TIME)
                val addPending = PendingIntent.getService(this, 4, addIntent, PendingIntent.FLAG_IMMUTABLE)
                actionsList.add(NotificationCompat.Action(android.R.drawable.ic_menu_add, "+5 Min", addPending))
            } else {
                val skipIntent = Intent(this, FocusService::class.java).setAction(ACTION_SKIP_BREAK)
                val skipPending = PendingIntent.getService(this, 5, skipIntent, PendingIntent.FLAG_IMMUTABLE)
                actionsList.add(NotificationCompat.Action(android.R.drawable.ic_media_next, "Skip Break", skipPending))
            }

            if (timerState == TimerState.RUNNING) {
                val pauseIntent = Intent(this, FocusService::class.java).setAction(ACTION_PAUSE)
                val pausePending = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
                actionsList.add(NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pausePending))
            } else {
                val resumeIntent = Intent(this, FocusService::class.java).setAction(ACTION_RESUME)
                val resumePending = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE)
                actionsList.add(NotificationCompat.Action(android.R.drawable.ic_media_play, "Resume", resumePending))
            }

            val stopIntent = Intent(this, FocusService::class.java).setAction(ACTION_STOP)
            val stopPending = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)
            actionsList.add(NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending))
            
            compactActions = intArrayOf(1, 2)

            // NOTE: MediaStyle removed — it was hiding the timer text on many devices.
            // BigTextStyle (set above) shows the timer + subject hierarchy clearly.
            // The mediaSession is kept active for lock-screen controls but no longer
            // overrides the notification style.
        }

        for (action in actionsList) {
            builder.addAction(action)
        }

        // For completed state, ensure actions are visible in compact view.
        // Some OEMs hide actions if compactActions isn't set explicitly.
        if (timerState == TimerState.COMPLETED && actionsList.isNotEmpty()) {
            // Show up to 2 actions in compact view
            val compactIndices = if (actionsList.size >= 2) intArrayOf(0, 1) else intArrayOf(0)
            // Use BigTextStyle with actions — the actions appear below the text
            // and are always visible when the notification is expanded.
            // For collapsed view, compactActions ensures buttons show.
        }

        mediaSession?.isActive = timerState == TimerState.RUNNING || timerState == TimerState.PAUSED

        return builder.build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = buildNotification()
        
        if (TimerManager.timerState.value == TimerState.COMPLETED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID + 1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID + 1, notification)
            }
            manager.cancel(NOTIFICATION_ID)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            manager.cancel(NOTIFICATION_ID + 1)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            
            val channelLow = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channelLow)
            
            val channelHigh = NotificationChannel(
                "focus_alerts_channel",
                "Timer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timer completion alerts"
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#D4A853")
            }
            manager.createNotificationChannel(channelHigh)
        }
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        restoreDnd()
        timerJob?.cancel()
        releaseWakeLock()
        mediaSession?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
