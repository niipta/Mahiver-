package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.service.FocusService

class MahirWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MahirWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.widget.ACTION_PLAY_PAUSE") {
            val serviceIntent = Intent(context, FocusService::class.java)
            val currentState = intent.getStringExtra("CURRENT_STATE")
            if (currentState == "RUNNING") {
                serviceIntent.action = FocusService.ACTION_PAUSE
            } else {
                serviceIntent.action = FocusService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            // Trigger a widget update
            GlobalScope.launch { glanceAppWidget.updateAll(context) }
        } else if (intent.action == "com.example.widget.ACTION_TIMER_TICK") {
            GlobalScope.launch { glanceAppWidget.updateAll(context) }
        }
    }
}
