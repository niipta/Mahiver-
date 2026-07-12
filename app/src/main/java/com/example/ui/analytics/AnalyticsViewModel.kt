package com.example.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FocusSessionEntity
import com.example.data.SettingsRepository
import com.example.data.SubjectWithTopics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.example.data.FocusDao
import com.example.data.SyllabusDao
import com.example.data.RevisionDao

// ============================================================
// DATA MODELS
// ============================================================

data class DailyStudyTime(val dayName: String, val minutes: Long)

/** One cell in the calendar heatmap. */
data class CalendarDay(
    val dateMillis: Long,
    val dayOfMonth: Int,
    val minutes: Long,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val subjects: List<DaySubjectDetail>
)

/** Subject breakdown for a specific day (shown when user taps a calendar cell). */
data class DaySubjectDetail(
    val subjectName: String,
    val minutes: Long,
    val color: Long
)

/** Leaderboard entry — one row in the leaderboard. */
data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: Long,
    val streak: Int,
    val isCurrentUser: Boolean
)

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
    val longestStreak: Int = 0,
    // New: calendar heatmap data (last 35 days = 5 weeks)
    val calendarDays: List<CalendarDay> = emptyList(),
    // New: leaderboard
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    // New: user's total points
    val totalPoints: Long = 0,
    // New: streak freezes available + monthly reset info
    val streakFreezesAvailable: Int = 0,
    val streakFreezesUsedThisMonth: Int = 0,
    val userName: String = "MAHIR"
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
        settingsRepository.longestStreak,
        settingsRepository.streakFreezesAvailable,
        settingsRepository.userName
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val subjectsList = values[0] as? List<SubjectWithTopics> ?: emptyList()
        val sessions = values[1] as? List<FocusSessionEntity> ?: emptyList()
        val currentStreak = values[3] as Int
        val longestStreak = values[4] as Int
        val streakFreezes = values[5] as Int
        val userName = values[6] as String

        val validFocusTypes = listOf("Focus", "Study")
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayStr = dateFormat.format(java.util.Date(System.currentTimeMillis()))

        val lifetimeFocusTime = sessions.filter { it.sessionType in validFocusTypes }.sumOf { it.actualDurationSeconds.toLong() / 60 }
        val todayFocusTime = sessions.filter { session ->
            dateFormat.format(java.util.Date(session.timestamp)) == todayStr && session.sessionType in validFocusTypes
        }.sumOf { it.actualDurationSeconds.toLong() / 60 }
        val totalRevisionTime = sessions.filter { it.sessionType == "Revision" }.sumOf { it.actualDurationSeconds.toLong() / 60 }

        var completedTopics = 0
        var allTopics = 0
        subjectsList.forEach { swt ->
            allTopics += swt.totalTopics
            completedTopics += swt.completedTopics
        }

        val weak = subjectsList.sortedBy { swt ->
            val total = swt.totalTopics
            if (total == 0) 1f else swt.completedTopics.toFloat() / total
        }.take(3).filter { it.topics.isNotEmpty() }.map { it.subject.name }

        // Weekly data (Mon-Sun)
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
            val minutes = sessions.filter { it.timestamp in dayStart until dayEnd && it.sessionType in validFocusTypes }.sumOf { it.actualDurationSeconds / 60L }
            weeklyData.add(DailyStudyTime(dayNames[i], minutes))
            weeklyFocusTime += minutes
        }

        // Monthly/quarterly data
        val monthlyData = mutableListOf<DailyStudyTime>()
        val quarterlyData = mutableListOf<DailyStudyTime>()
        val weekFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        for (i in 12 downTo 0) {
            val weekStart = startOfWeek - (i * 7 * 24 * 60 * 60 * 1000L)
            val weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000L)
            val minutes = sessions.filter { it.timestamp in weekStart until weekEnd && it.sessionType in validFocusTypes }.sumOf { it.actualDurationSeconds / 60L }
            val label = weekFormat.format(java.util.Date(weekStart))
            val record = DailyStudyTime(label, minutes)
            quarterlyData.add(record)
            if (i < 4) monthlyData.add(record)
        }

        // === CALENDAR HEATMAP (last 35 days) ===
        val calendarDays = buildCalendarDays(sessions, subjectsList, validFocusTypes)

        // === POINTS CALCULATION ===
        // Points: 1 min focus = 1 point, 1 topic completed = 50 points,
        // 1 day streak = 10 points, 1 revision done = 20 points
        val revisionsDone = sessions.count { it.sessionType == "Revision" }
        val totalPoints = lifetimeFocusTime + (completedTopics * 50L) + (currentStreak * 10L) + (revisionsDone * 20L)

        // === LEADERBOARD ===
        // Build a mock leaderboard with the current user + some AI-generated
        // competitors so the user has motivation. In a real SaaS app this would
        // come from Firestore, but for now we generate it locally.
        val leaderboard = buildLeaderboard(userName, totalPoints, currentStreak, longestStreak)

        // === STREAK FREEZE MONTHLY RESET ===
        // Check if we need to reset monthly streak freezes (4 free per month)
        val prefs = settingsRepository
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        val lastResetMonth = prefs.getLastFreezeResetMonth()
        if (lastResetMonth != currentMonth) {
            // New month — give 4 free streak freezes
            val current = settingsRepository.streakFreezesAvailable.value
            val newCount = (current + 4).coerceAtMost(8) // max 8 total
            prefs.setStreakFreezes(newCount)
            prefs.setLastFreezeResetMonth(currentMonth)
        }
        val freezesUsed = 4 - (streakFreezes - (streakFreezes - 4)).coerceAtLeast(0) // approximate

        AnalyticsUiState(
            todayFocusMinutes = todayFocusTime,
            weeklyFocusMinutes = weeklyFocusTime,
            lifetimeFocusMinutes = lifetimeFocusTime,
            totalRevisionMinutes = totalRevisionTime,
            topicsCompleted = completedTopics,
            totalTopics = allTopics,
            subjectsData = subjectsList,
            focusSessions = sessions,
            weakSubjects = weak,
            weeklyStudyData = weeklyData,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            monthlyStudyData = monthlyData,
            quarterlyStudyData = quarterlyData,
            calendarDays = calendarDays,
            leaderboard = leaderboard,
            totalPoints = totalPoints,
            streakFreezesAvailable = settingsRepository.streakFreezesAvailable.value,
            streakFreezesUsedThisMonth = 4 - settingsRepository.streakFreezesAvailable.value.coerceAtMost(4),
            userName = userName
        )
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    /**
     * Builds 35 calendar days (5 weeks) for the heatmap.
     * Each day has: date, minutes studied, and subject breakdown.
     */
    private fun buildCalendarDays(
        sessions: List<FocusSessionEntity>,
        subjects: List<SubjectWithTopics>,
        validFocusTypes: List<String>
    ): List<CalendarDay> {
        val result = mutableListOf<CalendarDay>()
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.clear(java.util.Calendar.MINUTE)
        cal.clear(java.util.Calendar.SECOND)
        cal.clear(java.util.Calendar.MILLISECOND)

        val todayMillis = cal.timeInMillis

        // Go back 34 days (35 total including today)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -34)

        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)

        for (i in 0 until 35) {
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + (24 * 60 * 60 * 1000L)

            val daySessions = sessions.filter {
                it.timestamp in dayStart until dayEnd && it.sessionType in validFocusTypes
            }

            val minutes = daySessions.sumOf { it.actualDurationSeconds / 60L }

            // Subject breakdown for this day
            val subjectDetails = daySessions.groupBy { session ->
                subjects.find { swt -> swt.topics.any { t -> t.topic.id == session.topicId } }?.subject?.name
                    ?: session.subjectName
            }.map { (name, sess) ->
                DaySubjectDetail(
                    subjectName = name,
                    minutes = sess.sumOf { it.actualDurationSeconds / 60L },
                    color = subjects.find { it.subject.name == name }?.subject?.color ?: 0xFF888888L
                )
            }.sortedByDescending { it.minutes }

            result.add(CalendarDay(
                dateMillis = dayStart,
                dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH),
                minutes = minutes,
                isToday = dayStart == todayMillis,
                isCurrentMonth = cal.get(java.util.Calendar.MONTH) == currentMonth,
                subjects = subjectDetails
            ))

            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        return result
    }

    /**
     * Builds a leaderboard with ONLY the real user's data.
     * No fake competitors — when Firestore sync is enabled and other users
     * join, this will pull real data from the cloud.
     */
    private fun buildLeaderboard(
        userName: String,
        userPoints: Long,
        userStreak: Int,
        userLongestStreak: Int
    ): List<LeaderboardEntry> {
        return listOf(
            LeaderboardEntry(
                rank = 1,
                name = userName,
                points = userPoints,
                streak = userStreak,
                isCurrentUser = true
            )
        )
    }
}
