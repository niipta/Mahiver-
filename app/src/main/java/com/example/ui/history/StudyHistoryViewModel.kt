package com.example.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FocusSessionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.example.data.SyllabusDao
import com.example.data.FocusDao
import com.example.data.RevisionDao
import com.example.data.sync.SyncDao
import java.util.*

enum class HistoryTab { TODAY, THIS_WEEK, THIS_MONTH, ALL_TIME }

data class DailySummary(
    val dateString: String,
    val displayDate: String,
    val totalStudyMinutes: Int,
    val totalSessions: Int,
    val sessions: List<FocusSessionEntity>,
    val dateMillis: Long
)

data class HistoryUiState(
    val activeTab: HistoryTab = HistoryTab.TODAY,
    val searchQuery: String = "",
    val groupedSessions: List<DailySummary> = emptyList(),
    val totalStudyMinutes: Int = 0,
    val totalSessionCount: Int = 0,
    val totalTopicsLearned: Int = 0,
    val focusScore: Int = 100,
    val totalBreakMinutes: Int = 0,
    val isLoading: Boolean = true
)


@HiltViewModel
class StudyHistoryViewModel @Inject constructor(
    private val focusDao: FocusDao,
    private val syllabusDao: SyllabusDao,
    private val revisionDao: RevisionDao,
    private val syncDao: SyncDao
) : ViewModel() {

    val subjectsWithTopics: StateFlow<List<com.example.data.SubjectWithTopics>> = syllabusDao.getAllSubjectsWithTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTab = MutableStateFlow(HistoryTab.TODAY)
    private val _searchQuery = MutableStateFlow("")
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = combine(
        focusDao.getAllSessions(),
        _activeTab,
        _searchQuery
    ) { sessions, tab, query ->
        
        val validFocusTypes = listOf("Focus", "Study")
        val breakTypes = listOf("Short Break", "Long Break")
        
        // Filter out breaks from study sessions list to display, 
        // Break sessions are only for calculating total break time.
        val studySessions = sessions.filter { it.sessionType in validFocusTypes && it.actualDurationSeconds >= 60 }
        
        // Apply search query
        val filteredBySearch = if (query.isNotBlank()) {
            studySessions.filter { 
                it.subjectName.contains(query, ignoreCase = true) || 
                (it.topicName?.contains(query, ignoreCase = true) == true) 
            }
        } else {
            studySessions
        }

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis
        
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekStart = cal.timeInMillis
        
        cal.timeInMillis = todayStart
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = cal.timeInMillis
        
        val filteredByTab = when (tab) {
            HistoryTab.TODAY -> filteredBySearch.filter { it.timestamp >= todayStart }
            HistoryTab.THIS_WEEK -> filteredBySearch.filter { it.timestamp >= weekStart }
            HistoryTab.THIS_MONTH -> filteredBySearch.filter { it.timestamp >= monthStart }
            HistoryTab.ALL_TIME -> filteredBySearch
        }

        // Group by Date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        
        val groupedMap = filteredByTab.groupBy { dateFormat.format(Date(it.timestamp)) }
        val summaries = groupedMap.map { (dateStr, sessions) ->
            val totalSec = sessions.sumOf { it.actualDurationSeconds }
            val firstSessionMillis = sessions.firstOrNull()?.timestamp ?: 0L
            
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = firstSessionMillis
            
            // Format Display Date: Today, Yesterday, or actual date
            val sessCal = Calendar.getInstance().apply { timeInMillis = firstSessionMillis }
            sessCal.set(Calendar.HOUR_OF_DAY, 0)
            sessCal.set(Calendar.MINUTE, 0)
            sessCal.set(Calendar.SECOND, 0)
            sessCal.set(Calendar.MILLISECOND, 0)
            
            val displayDate = when (sessCal.timeInMillis) {
                todayStart -> "Today"
                todayStart - 86400000 -> "Yesterday"
                else -> displayFormat.format(Date(firstSessionMillis))
            }
            
            val actualDateStr = displayFormat.format(Date(firstSessionMillis))
            val finalDisplayDate = if (displayDate == "Today" || displayDate == "Yesterday") "$displayDate\n$actualDateStr" else displayDate // Hacky way, better handled in UI

            DailySummary(
                dateString = dateStr,
                displayDate = displayDate,
                totalStudyMinutes = totalSec / 60,
                totalSessions = sessions.size,
                sessions = sessions.sortedByDescending { it.timestamp },
                dateMillis = firstSessionMillis
            )
        }.sortedByDescending { it.dateMillis }

        // Top analytics based on the selected tab
        val tabTotalSeconds = filteredByTab.sumOf { it.actualDurationSeconds }
        val tabSessions = filteredByTab.size
        
        // Calculate breaks for the active tab scope
        val breakSessions = sessions.filter { it.sessionType in breakTypes }
        val tabBreaks = when(tab) {
            HistoryTab.TODAY -> breakSessions.filter { it.timestamp >= todayStart }
            HistoryTab.THIS_WEEK -> breakSessions.filter { it.timestamp >= weekStart }
            HistoryTab.THIS_MONTH -> breakSessions.filter { it.timestamp >= monthStart }
            HistoryTab.ALL_TIME -> breakSessions
        }
        val tabBreakSeconds = tabBreaks.sumOf { it.actualDurationSeconds }

        // Unique topics
        val uniqueTopics = filteredByTab
            .mapNotNull { it.topicId ?: it.topicName }
            .filter { it.isNotBlank() && !it.contains("Custom") && !it.contains("Break") }
            .toSet()
            .size

        HistoryUiState(
            activeTab = tab,
            searchQuery = query,
            groupedSessions = summaries,
            totalStudyMinutes = tabTotalSeconds / 60,
            totalSessionCount = tabSessions,
            totalTopicsLearned = uniqueTopics,
            focusScore = if (tabSessions > 0) ((tabTotalSeconds.toFloat() / (tabTotalSeconds + tabBreakSeconds).coerceAtLeast(1)) * 100).toInt() else 0,
            totalBreakMinutes = tabBreakSeconds / 60,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setActiveTab(tab: HistoryTab) {
        _activeTab.value = tab
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun findAutoRevision(session: FocusSessionEntity): com.example.data.RevisionEntity? {
        if (session.actualDurationSeconds >= 600 && session.topicId != null && session.sessionType != "Revision") {
            val revisions = revisionDao.getRevisionsByRelatedId(session.topicId)
            return revisions.find { !it.isCompleted && Math.abs(it.scheduledDateMillis - (session.timestamp + 86400000L)) < 10000L }
        }
        return null
    }

    fun deleteSession(session: FocusSessionEntity, deleteAutoRevision: Boolean, autoRevision: com.example.data.RevisionEntity? = null) {
        viewModelScope.launch {
            focusDao.deleteSession(session)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "DELETE", entityType = "FOCUS_SESSION", entityId = session.id))
            
            if (deleteAutoRevision && autoRevision != null) {
                revisionDao.deleteRevision(autoRevision)
                syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "DELETE", entityType = "REVISION", entityId = autoRevision.id))
            }
        }
    }

    fun undoDeleteSession(session: FocusSessionEntity, autoRevision: com.example.data.RevisionEntity? = null) {
        viewModelScope.launch {
            focusDao.insertSession(session)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "FOCUS_SESSION", entityId = session.id))
            
            if (autoRevision != null) {
                revisionDao.insertRevision(autoRevision)
                syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "REVISION", entityId = autoRevision.id))
            }
        }
    }

    fun updateSessionDuration(session: FocusSessionEntity, durationSeconds: Int) {
        viewModelScope.launch {
            val updated = session.copy(actualDurationSeconds = durationSeconds)
            focusDao.updateSession(updated)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "FOCUS_SESSION", entityId = session.id))
        }
    }

    fun updateSessionTopic(session: FocusSessionEntity, subjectId: String?, topicId: String?, subtopicId: String?, customTaskTitle: String?) {
        viewModelScope.launch {
            var subjectName = session.subjectName
            var topicName = session.topicName

            if (subjectId != null) {
                val subject = syllabusDao.getSubjectById(subjectId)
                if (subject != null) {
                    subjectName = subject.name
                }
            } else {
                subjectName = "Custom"
            }

            if (topicId != null) {
                val topic = syllabusDao.getTopicById(topicId)
                if (topic != null) {
                    topicName = topic.name
                }
            } else if (customTaskTitle != null) {
                topicName = customTaskTitle
            }

            val updated = session.copy(
                subjectId = subjectId,
                topicId = topicId,
                subtopicId = subtopicId,
                subjectName = subjectName,
                topicName = topicName
            )
            focusDao.updateSession(updated)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "FOCUS_SESSION", entityId = session.id))
        }
    }
}
