package com.example.ui.focus

import android.app.Application
import androidx.core.content.ContextCompat
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FocusRepository
import com.example.data.FocusSessionEntity
import com.example.service.FocusService
import com.example.service.SessionType
import com.example.service.TimerManager
import com.example.service.TimerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class FocusViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val repository: FocusRepository
) : ViewModel() {

    val timeRemaining = TimerManager.timeRemaining.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 25 * 60L)
    val timerState = TimerManager.timerState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TimerState.STOPPED)
    val sessionType = TimerManager.sessionType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionType.FOCUS)
    val selectedSubjectId = TimerManager.selectedSubjectId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val selectedTopicId = TimerManager.selectedTopicId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val selectedSubtopicId = TimerManager.selectedSubtopicId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val customTaskTitle = TimerManager.customTaskTitle.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val isSessionCompleted = TimerManager.isSessionCompleted.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    
    fun dismissCompletion() {
        TimerManager.setSessionCompleted(false)
        TimerManager.resetCurrentSession()
    }
    
    // We already have repository, but we need SyllabusDao for subjects. Wait, let's grab it.
    private val syllabusDao = AppDatabase.getDatabase(application).syllabusDao()
    private val plannerDao = AppDatabase.getDatabase(application).plannerDao()
    
    val subjectsWithTopics = syllabusDao.getAllSubjectsWithTopics().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val todayPlan = kotlinx.coroutines.flow.flow {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayStr = dateFormat.format(java.util.Date(System.currentTimeMillis()))
        plannerDao.getPlan(todayStr).collect { emit(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val recentSessions: StateFlow<List<FocusSessionEntity>> = repository.allSessions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun startTimer() {
        val intent = Intent(application, FocusService::class.java).apply {
            action = FocusService.ACTION_START
        }
        ContextCompat.startForegroundService(application, intent)
    }

    fun pauseTimer() {
        val intent = Intent(application, FocusService::class.java).apply {
            action = FocusService.ACTION_PAUSE
        }
        ContextCompat.startForegroundService(application, intent)
    }

    fun resumeTimer() {
        val intent = Intent(application, FocusService::class.java).apply {
            action = FocusService.ACTION_RESUME
        }
        ContextCompat.startForegroundService(application, intent)
    }

    fun stopTimer() {
        val intent = Intent(application, FocusService::class.java).apply {
            action = FocusService.ACTION_STOP
        }
        ContextCompat.startForegroundService(application, intent)
    }

    fun resetTimer() {
        if (timerState.value != TimerState.RUNNING) {
            TimerManager.resetCurrentSession()
            val intent = Intent(application, FocusService::class.java).apply {
                action = FocusService.ACTION_STOP
            }
            application.stopService(intent)
        }
    }

    fun setSessionType(type: SessionType) {
        if (timerState.value == TimerState.STOPPED) {
            TimerManager.setSessionType(type)
        }
    }
    
    fun adjustDuration(minutesDelta: Int) {
        if (timerState.value == TimerState.STOPPED) {
            val currentType = sessionType.value
            val current = TimerManager.getDurationMinutes(currentType)
            // Clamp between 1 min and 180 min (3 hours) to keep things sane
            val newDuration = (current + minutesDelta).coerceIn(1, 180)
            if (newDuration != current) {
                TimerManager.setDuration(currentType, newDuration)
            }
        }
    }

    fun setTargetTopic(subjectId: String?, topicId: String?, subtopicId: String? = null, customTitle: String? = null) {
        viewModelScope.launch {
            var sName: String? = null
            var tName: String? = null
            var subName: String? = null

            if (subjectId != null) {
                syllabusDao.getSubjectById(subjectId)?.let { sName = it.name }
            }
            if (topicId != null) {
                syllabusDao.getTopicById(topicId)?.let { tName = it.name }
            }
            if (subtopicId != null) {
                syllabusDao.getSubtopicById(subtopicId)?.let { subName = it.name }
            }
            TimerManager.setTaskContext(subjectId, topicId, subtopicId, customTitle, sName, tName, subName)
        }
    }

    /**
     * Explicitly sets a "General Study" context so the focus session records
     * meaningful data even when no specific topic is selected. This is called
     * when the user chooses "General Study" from the missing-topic dialog.
     */
    fun startGeneralStudy() {
        TimerManager.setTaskContext(null, null, null, null, "General Study", null, null)
        startTimer()
    }
}
