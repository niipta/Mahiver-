package com.example.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R
import com.example.service.TimerManager
import com.example.service.TimerState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MahirWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = com.example.data.AppDatabase.getDatabase(context)
        val todayStr = com.example.data.PlannerRepository.getDateString()
        val plan = db.plannerDao().getPlanSync(todayStr)
        var topicsCompleted = 0
        var topicsTotal = 0
        if (plan != null) {
            val topicIds = plan.plannedTopicIds.split(",").filter { it.isNotEmpty() }
            topicsTotal = topicIds.size
            if (topicIds.isNotEmpty()) {
                val topics = db.syllabusDao().getTopicsByIds(topicIds)
                topicsCompleted = topics.count { it.isCompleted }
            }
        }

        provideContent {
            GlanceTheme {
                WidgetContent(topicsCompleted, topicsTotal)
            }
        }
    }

    fun updateAll(context: Context) {
        GlobalScope.launch {
            this@MahirWidget.updateAll(context)
        }
    }
}

@Composable
fun WidgetContent(topicsCompleted: Int, topicsTotal: Int) {
    val context = LocalContext.current
    
    // In Glance, we can't easily collect flow natively so we will suppress this warning
    // Alternatively we can just read the current value since widget only updates on actions
    @Suppress("StateFlowValueCalledInComposition")
    val timeRemaining = TimerManager.timeRemaining.value
    @Suppress("StateFlowValueCalledInComposition")
    val timerState = TimerManager.timerState.value
    @Suppress("StateFlowValueCalledInComposition")
    val sessionType = TimerManager.sessionType.value
    
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateString = dateFormat.format(Date())

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF161924)) // Dark card color #161924
            .padding(16.dp)
            .clickable(
                onClick = actionStartActivity<MainActivity>()
            ),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image(provider = ImageProvider(R.mipmap.ic_launcher), contentDescription = "Logo", modifier = GlanceModifier.size(24.dp))
                Text(
                    text = "MahirVerse",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = dateString,
                    style = TextStyle(
                        color = ColorProvider(Color.Gray),
                        fontSize = 14.sp
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.height(16.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 Streak: ${context.getSharedPreferences("mahirverse_settings", Context.MODE_PRIVATE).getInt("streak_days", 0)} Days",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFFC107)), // Gold
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.defaultWeight())
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Today: $topicsCompleted/$topicsTotal topics",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Focus: $timeString",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFC107)), // Gold
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                // Play/Pause button
                val actionIntent = Intent(context, MahirWidgetReceiver::class.java).apply {
                    action = "com.example.widget.ACTION_PLAY_PAUSE"
                    putExtra("CURRENT_STATE", timerState.name)
                }
                val playPauseText = if (timerState == TimerState.RUNNING) "⏸ PAUSE" else "▶ PLAY"
                
                Box(
                    modifier = GlanceModifier
                        .background(Color(0x33FFC107))
                        .padding(8.dp)
                        .clickable(actionSendBroadcast(actionIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playPauseText,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFC107)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
