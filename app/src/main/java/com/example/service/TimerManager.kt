package com.example.service

import android.content.Context
import android.content.SharedPreferences

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TimerState { STOPPED, RUNNING, PAUSED, COMPLETED }
enum class SessionType(val title: String) {
    FOCUS("Focus"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

object TimerManager {
    private var prefs: SharedPreferences? = null
    
    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

        val savedTime = prefs?.getLong("time_remaining", -1L) ?: -1L
        SessionType.entries.forEach { type ->
            val saved = prefs?.getInt("duration_${type.name}", -1) ?: -1
            if (saved > 0) durations[type] = saved
        }

        if (savedTime != -1L) {
            _timeRemaining.value = savedTime

            val savedState = prefs?.getString("timer_state", TimerState.STOPPED.name)
            _timerState.value = try { TimerState.valueOf(savedState ?: TimerState.STOPPED.name) } catch (e: Exception) { TimerState.STOPPED }

            val savedSessionType = prefs?.getString("session_type", SessionType.FOCUS.name)
            _sessionType.value = try { SessionType.valueOf(savedSessionType ?: SessionType.FOCUS.name) } catch (e: Exception) { SessionType.FOCUS }

            _selectedSubjectId.value = prefs?.getString("subject_id", null)
            _selectedTopicId.value = prefs?.getString("topic_id", null)
            _selectedSubtopicId.value = prefs?.getString("subtopic_id", null)
            _customTaskTitle.value = prefs?.getString("custom_title", null)
            _selectedSubjectName.value = prefs?.getString("subject_name", null)
            _selectedTopicName.value = prefs?.getString("topic_name", null)
            _selectedSubtopicName.value = prefs?.getString("subtopic_name", null)
            _interruptions.value = prefs?.getInt("interruptions", 0) ?: 0
            _focusSessionCount.value = prefs?.getInt("session_count", 1) ?: 1

            val lastUpdate = prefs?.getLong("last_update_time", 0L) ?: 0L
            if (_timerState.value == TimerState.RUNNING && lastUpdate > 0) {
                val now = System.currentTimeMillis()
                val elapsed = (now - lastUpdate) / 1000
                val newTime = maxOf(0L, _timeRemaining.value - elapsed)
                _timeRemaining.value = newTime
                if (newTime == 0L) {
                    _timerState.value = TimerState.COMPLETED
                }
            }
            // Sync originalDurationSeconds so saved sessions compute correct actual time
            originalDurationSeconds = _timeRemaining.value + 0L
        } else {
            // No active session — sync _timeRemaining with restored custom durations
            _timeRemaining.value = getDurationMinutes(SessionType.FOCUS) * 60L
            originalDurationSeconds = _timeRemaining.value
        }
    }
    
    private fun saveState() {
        prefs?.edit()?.apply {
            putLong("time_remaining", _timeRemaining.value)
            putString("timer_state", _timerState.value.name)
            putString("session_type", _sessionType.value.name)
            putString("subject_id", _selectedSubjectId.value)
            putString("topic_id", _selectedTopicId.value)
            putString("subtopic_id", _selectedSubtopicId.value)
            putString("custom_title", _customTaskTitle.value)
            putString("subject_name", _selectedSubjectName.value)
            putString("topic_name", _selectedTopicName.value)
            putString("subtopic_name", _selectedSubtopicName.value)
            putInt("interruptions", _interruptions.value)
            putInt("session_count", _focusSessionCount.value)
            putLong("last_update_time", System.currentTimeMillis())
            apply()
        }
    }

    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _sessionType = MutableStateFlow(SessionType.FOCUS)
    val sessionType: StateFlow<SessionType> = _sessionType.asStateFlow()

    private val durations = mutableMapOf(
        SessionType.FOCUS to 25,
        SessionType.SHORT_BREAK to 5,
        SessionType.LONG_BREAK to 15
    )

    fun getDurationMinutes(type: SessionType): Int = durations[type] ?: 25
    
    fun setDuration(type: SessionType, minutes: Int) {
        durations[type] = minutes
        prefs?.edit()?.putInt("duration_${type.name}", minutes)?.apply()

        if (_sessionType.value == type && _timerState.value == TimerState.STOPPED) {
            _timeRemaining.value = minutes * 60L
            originalDurationSeconds = minutes * 60L
        }
    }

    /**
     * Adds extra seconds to the running timer (e.g. "+5 min" notification action).
     * CRITICAL: also extends originalDurationSeconds so that the saved session
     * duration reflects the actual studied time.
     */
    fun addTime(seconds: Long) {
        _timeRemaining.value = (_timeRemaining.value + seconds).coerceAtLeast(0L)
        originalDurationSeconds += seconds
        saveState()
    }

    private val _timeRemaining = MutableStateFlow(getDurationMinutes(SessionType.FOCUS) * 60L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()
    var originalDurationSeconds = getDurationMinutes(SessionType.FOCUS) * 60L
    
    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    val selectedSubjectId: StateFlow<String?> = _selectedSubjectId.asStateFlow()
    
    private val _selectedTopicId = MutableStateFlow<String?>(null)
    val selectedTopicId: StateFlow<String?> = _selectedTopicId.asStateFlow()
    
    private val _selectedSubtopicId = MutableStateFlow<String?>(null)
    val selectedSubtopicId: StateFlow<String?> = _selectedSubtopicId.asStateFlow()
    
    private val _selectedSubjectName = MutableStateFlow<String?>(null)
    val selectedSubjectName: StateFlow<String?> = _selectedSubjectName.asStateFlow()
    
    private val _selectedTopicName = MutableStateFlow<String?>(null)
    val selectedTopicName: StateFlow<String?> = _selectedTopicName.asStateFlow()
    
    private val _selectedSubtopicName = MutableStateFlow<String?>(null)
    val selectedSubtopicName: StateFlow<String?> = _selectedSubtopicName.asStateFlow()
    
    private val _customTaskTitle = MutableStateFlow<String?>(null)
    val customTaskTitle: StateFlow<String?> = _customTaskTitle.asStateFlow()
    
    private val _interruptions = MutableStateFlow(0)
    val interruptions: StateFlow<Int> = _interruptions.asStateFlow()
    
    private val _focusSessionCount = MutableStateFlow(1)
    val focusSessionCount: StateFlow<Int> = _focusSessionCount.asStateFlow()
    
    fun incrementFocusSession() { _focusSessionCount.value += 1 }
    fun resetFocusSession() { _focusSessionCount.value = 1 }
    
    private val _isSessionCompleted = MutableStateFlow(false)
    val isSessionCompleted: StateFlow<Boolean> = _isSessionCompleted.asStateFlow()
    
    fun setSessionCompleted(completed: Boolean) {
        _isSessionCompleted.value = completed
    }
    
    fun setTaskContext(subjectId: String?, topicId: String?, subtopicId: String? = null, customTitle: String? = null, subjectName: String? = null, topicName: String? = null, subtopicName: String? = null) {
        _selectedSubjectId.value = subjectId
        _selectedTopicId.value = topicId
        _selectedSubtopicId.value = subtopicId
        _customTaskTitle.value = customTitle
        _selectedSubjectName.value = subjectName
        _selectedTopicName.value = topicName
        _selectedSubtopicName.value = subtopicName
        saveState()
    }
    
    fun incrementInterruptions() {
        _interruptions.value += 1
    }

    
    private var lastSaveTimeMs = 0L
    private val SAVE_INTERVAL_MS = 5000L

    fun updateState(state: TimerState) { 
        _timerState.value = state
        saveState() // Critical transitions save immediately
        lastSaveTimeMs = System.currentTimeMillis()
    }
    
    fun updateTime(seconds: Long) { 
        _timeRemaining.value = seconds
        val now = System.currentTimeMillis()
        if (now - lastSaveTimeMs >= SAVE_INTERVAL_MS) {
            lastSaveTimeMs = now
            saveState()
        }
    }

    fun setSessionType(type: SessionType) {
        _sessionType.value = type; saveState()
        if (_timerState.value == TimerState.STOPPED) {
            _timeRemaining.value = getDurationMinutes(type) * 60L
            originalDurationSeconds = _timeRemaining.value
        }
    }
    fun resetCurrentSession() {
        originalDurationSeconds = getDurationMinutes(_sessionType.value) * 60L
        _timeRemaining.value = getDurationMinutes(_sessionType.value) * 60L
        _timerState.value = TimerState.STOPPED
        _interruptions.value = 0
        saveState()
    }
}
