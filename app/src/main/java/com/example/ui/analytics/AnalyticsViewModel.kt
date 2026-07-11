package com.example.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FocusSessionEntity
import com.example.data.RevisionEntity
import com.example.data.SettingsRepository
import com.example.data.SubjectWithTopics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.example.data.SyllabusDao
import com.example.data.RevisionDao
import com.example.data.FocusDao

data class DailyStudyTime(val dayName: String, val minutes: Long)

data class AnalyticsUiState(
    val todayFocusMinutes: Long = 0,
    val weeklyFocusMinutes: Long = 0,
    val lifetimeFocusMinutes: Long = 0,
    val totalRevisionMinutes: Long = 0,
    val topicsCompleted: Int = 0,
    val totalTopics: Int = 0,
    val subjectsData: List<SubjectWithTopics> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val weakSubjects: List<String> = emptyList(),
    val weeklyStudyData: List<DailyStudyTime> = emptyList(),
    val monthlyStudyData: List<DailyStudyTime> = emptyList(),
    val quarterlyStudyData: List<DailyStudyTime> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
) {
    val totalProductiveMinutes: Long
        get() = lifetimeFocusMinutes + totalRevisionMinutes
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val syllabusDao: SyllabusDao,
    private val revisionDao: RevisionDao,
    private val focusDao: FocusDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = combine(
        syllabusDao.getAllSubjectsWithTopics(),
        focusDao.getAllSessions(),
        revisionDao.getAllRevisions(),
        settingsRepository.currentStreak,
        settingsRepository.longestStreak
    ) { subjectsList, sessions, revisions, currentStreak, longestStreak ->
        // Null-safe guards — during cold start these lists can briefly be null
        val safeSubjects = subjectsList ?: emptyList()
        val safeSessions = sessions ?: emptyList()
        val validFocusTypes = listOf("Focus", "Study")

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayStr = dateFormat.format(java.util.Date(System.currentTimeMillis()))

        val lifetimeFocusTime = safeSessions.filter { it.sessionType in validFocusTypes }.sumOf { it.actualDurationSeconds.toLong() / 60 }

        val todayFocusTime = safeSessions.filter { session ->
            val sessionDate = dateFormat.format(java.util.Date(session.timestamp))
            sessionDate == todayStr && session.sessionType in validFocusTypes
        }.sumOf { it.actualDurationSeconds.toLong() / 60 }

        val totalRevisionTime = safeSessions.filter { it.sessionType == "Revision" }.sumOf { it.actualDurationSeconds.toLong() / 60 }

        var completedTopics = 0
        var allTopics = 0

        safeSubjects.forEach { swt ->
            allTopics += swt.totalTopics
            completedTopics += swt.completedTopics
        }

        val weak = safeSubjects.sortedBy { swt ->
            val total = swt.totalTopics
            if (total == 0) 1f else swt.completedTopics.toFloat() / total
        }.take(3).filter { it.topics.isNotEmpty() }.map { it.subject.name }

        // Compile weekly data (Monday -> Sunday for the current week)
        val cal = java.util.Calendar.getInstance()
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.clear(java.util.Calendar.MINUTE)
        cal.clear(java.util.Calendar.SECOND)
        cal.clear(java.util.Calendar.MILLISECOND)
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val startOfWeek = cal.timeInMillis
        
        val weeklyData = mutableListOf<DailyStudyTime>()
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        var weeklyFocusTime = 0L
        for (i in 0..6) {
            val dayStart = startOfWeek + (i * 24 * 60 * 60 * 1000L)
            val dayEnd = dayStart + (24 * 60 * 60 * 1000L)
            val minutes = safeSessions
                .filter { it.timestamp in dayStart until dayEnd && it.sessionType in validFocusTypes }
                .sumOf { it.actualDurationSeconds / 60L }
            weeklyData.add(DailyStudyTime(dayNames[i], minutes))
            weeklyFocusTime += minutes
        }


        val monthlyData = mutableListOf<DailyStudyTime>()
        val quarterlyData = mutableListOf<DailyStudyTime>()
        val weekFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())

        for (i in 12 downTo 0) {
            val weekStart = startOfWeek - (i * 7 * 24 * 60 * 60 * 1000L)
            val weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000L)
            val minutes = safeSessions
                .filter { it.timestamp in weekStart until weekEnd && it.sessionType in validFocusTypes }
                .sumOf { it.actualDurationSeconds / 60L }

            val label = weekFormat.format(java.util.Date(weekStart))
            val record = DailyStudyTime(label, minutes)

            quarterlyData.add(record)
            if (i < 4) {
                monthlyData.add(record)
            }
        }

        AnalyticsUiState(
            todayFocusMinutes = todayFocusTime,
            weeklyFocusMinutes = weeklyFocusTime,
            lifetimeFocusMinutes = lifetimeFocusTime,
            totalRevisionMinutes = totalRevisionTime,
            topicsCompleted = completedTopics,
            totalTopics = allTopics,
            subjectsData = safeSubjects,
            focusSessions = safeSessions,
            weakSubjects = weak,
            weeklyStudyData = weeklyData,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            monthlyStudyData = monthlyData,
            quarterlyStudyData = quarterlyData
        )
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )
}
