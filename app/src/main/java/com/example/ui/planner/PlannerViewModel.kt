package com.example.ui.planner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch

data class PlannerUiState(
    val activeTabIndex: Int = 0,
    val selectedDate: String = PlannerRepository.getDateString(),
    val availableSubjects: List<SubjectWithTopics> = emptyList(),
    val availableRevisions: List<RevisionEntity> = emptyList(),
    val selectedTopicIds: Set<String> = emptySet(),
    val selectedSubtopicIds: Set<String> = emptySet(),
    val selectedRevisionIds: Set<String> = emptySet(),
    val loading: Boolean = true,
    val plannedSubtopicsCount: Int = 0,
    val completedSubtopicsCount: Int = 0,
    val pendingSubtopicsCount: Int = 0
)


@HiltViewModel
@kotlinx.coroutines.ExperimentalCoroutinesApi
class PlannerViewModel @Inject constructor(
    private val syllabusDao: SyllabusDao,
    private val revisionDao: RevisionDao,
    private val plannerDao: PlannerDao,
    private val syncDao: com.example.data.sync.SyncDao,
    private val repository: PlannerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState = _uiState.asStateFlow()

        private val _activeTabIndex = MutableStateFlow(0)
    private val _selectedMonthDate = MutableStateFlow<String?>(null)

    private val _visibleMonth = MutableStateFlow(java.time.YearMonth.now())
    val visibleMonth = _visibleMonth.asStateFlow()

    val monthlyPlans: StateFlow<Map<String, DailyPlanEntity>> = _visibleMonth.flatMapLatest { month ->
        val startDate = month.atDay(1).toString()
        val endDate = month.atEndOfMonth().toString()
        repository.getPlansInRange(startDate, endDate)
    }.map { plans ->
        plans.associateBy { it.dateString }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun nextMonth() {
        _visibleMonth.value = _visibleMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _visibleMonth.value = _visibleMonth.value.minusMonths(1)
    }

    fun currentMonth() {
        _visibleMonth.value = java.time.YearMonth.now()
    }

    fun setSelectedMonthDate(date: String) {
        _selectedMonthDate.value = date
    }

    init {
        viewModelScope.launch {
            combine(
                _activeTabIndex,
                _selectedMonthDate
            ) { tabIndex, monthDate ->
                if (tabIndex == 1) {
                    // FIX: use LocalDate for DST-safe date math
                    java.time.LocalDate.now().plusDays(1).toString()
                } else if (tabIndex == 2) {
                    monthDate ?: PlannerRepository.getDateString()
                } else {
                    PlannerRepository.getDateString()
                }
            }.flatMapLatest { dateStr ->
                combine(
                    syllabusDao.getAllSubjectsWithTopics(),
                    revisionDao.getAllRevisions(),
                    repository.getPlanForDate(dateStr)
                ) { subjects, revisions, plan ->
                    val topicSet = plan?.plannedTopicIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
                    val subtopicSet = plan?.plannedSubtopicIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
                    val revisionSet = plan?.plannedRevisionIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
                    
                    var plannedCount = 0
                    var completedCount = 0
                    
                    subjects.forEach { subjectWithTopics ->
                        subjectWithTopics.topics.forEach { topicWithSub ->
                            topicWithSub.subtopics.forEach { subtopic ->
                                if (subtopicSet.contains(subtopic.id)) {
                                    plannedCount++
                                    if (subtopic.isCompleted) {
                                        completedCount++
                                    }
                                }
                            }
                        }
                    }
                    
                    PlannerUiState(
                        activeTabIndex = _activeTabIndex.value,
                        selectedDate = dateStr,
                        availableSubjects = subjects,
                        availableRevisions = revisions,
                        selectedTopicIds = topicSet,
                        selectedSubtopicIds = subtopicSet,
                        selectedRevisionIds = revisionSet,
                        loading = false,
                        plannedSubtopicsCount = plannedCount,
                        completedSubtopicsCount = completedCount,
                        pendingSubtopicsCount = plannedCount - completedCount
                    )
                }
            }.flowOn(kotlinx.coroutines.Dispatchers.IO).collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun setTabIndex(index: Int) {
        _activeTabIndex.value = index
    }


    fun toggleTopicSelection(topicId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val date = _uiState.value.selectedDate
            val oldPlan = plannerDao.getPlanSync(date)
            val current = oldPlan?.plannedTopicIds?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            if (current.contains(topicId)) current.remove(topicId) else current.add(topicId)
            savePlan(current, oldPlan?.plannedSubtopicIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet(), oldPlan?.plannedRevisionIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet(), date)
        }
    }

    fun toggleSubtopicSelection(subtopicId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val date = _uiState.value.selectedDate
            val oldPlan = plannerDao.getPlanSync(date)
            val current = oldPlan?.plannedSubtopicIds?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            if (current.contains(subtopicId)) current.remove(subtopicId) else current.add(subtopicId)
            savePlan(oldPlan?.plannedTopicIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet(), current, oldPlan?.plannedRevisionIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet(), date)
        }
    }

    fun toggleRevisionSelection(revisionId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val date = _uiState.value.selectedDate
            val oldPlan = plannerDao.getPlanSync(date)
            val current = oldPlan?.plannedRevisionIds?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            if (current.contains(revisionId)) current.remove(revisionId) else current.add(revisionId)
            savePlan(oldPlan?.plannedTopicIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet(), oldPlan?.plannedSubtopicIds?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet(), current, date)
        }
    }

    fun markSubtopicCompleted(subtopic: SubtopicEntity, completed: Boolean, addToRevision: Boolean = false) {
        val completionManager = com.example.domain.CompletionManager(syllabusDao, revisionDao, syncDao)

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val topic = syllabusDao.getTopicById(subtopic.topicId)
            val subject = if (topic != null) syllabusDao.getSubjectById(topic.subjectId) else null
            val subjectName = subject?.name ?: "Subject"
            completionManager.toggleSubtopicCompletion(subtopic, subjectName, completed, addToRevision)
        }
    }

    /**
     * Marks a whole topic (no subtopics case) complete. When [addToRevision]
     * is true, also schedules a revision for it.
     */
    fun markTopicCompleted(topic: TopicEntity, completed: Boolean, addToRevision: Boolean = false) {
        val completionManager = com.example.domain.CompletionManager(syllabusDao, revisionDao, syncDao)

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val subject = syllabusDao.getSubjectById(topic.subjectId)
            val subjectName = subject?.name ?: "Subject"
            completionManager.toggleTopicCompletion(topic, subjectName, completed, addToRevision)
        }
    }

    fun markRevisionCompleted(revision: RevisionEntity, completed: Boolean) {
        viewModelScope.launch {
            val updated = revision.copy(isCompleted = completed)
            revisionDao.updateRevision(updated)
            syncDao.insertSyncTask(SyncQueueEntity(operationType = "UPDATE", entityType = "REVISION", entityId = revision.id))
        }
    }
        
    /**
     * FIX: persist real dateMillis (start-of-day for the planned date),
     * not System.currentTimeMillis() which is last-edit time.
     */
    private suspend fun savePlan(topics: Set<String>, subtopics: Set<String>, revisions: Set<String>, date: String) {
        val oldPlan = plannerDao.getPlanSync(date)
        val dateMillis = try {
            java.time.LocalDate.parse(date)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        val entity = DailyPlanEntity(
            dateString = date,
            dateMillis = dateMillis,
            plannedTopicIds = topics.joinToString(","),
            plannedSubtopicIds = subtopics.joinToString(","),
            plannedRevisionIds = revisions.joinToString(","),
            isCompleted = oldPlan?.isCompleted ?: false
        )
        repository.insertPlan(entity)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "UPDATE", entityType = "PLAN", entityId = date))
    }
}
