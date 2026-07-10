package com.example.ui.syllabus

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.data.SubjectEntity
import com.example.data.SubjectWithTopics
import com.example.data.SubtopicEntity
import com.example.data.SyllabusImporter
import com.example.data.SyllabusRepository
import com.example.data.TopicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.example.data.RevisionRepository
import com.example.data.ExamDao
import com.example.data.sync.SyncDao

/** One-time UI events emitted by the ViewModel. */
sealed class SyllabusUiEvent {
    data class ImportSuccess(val subjects: Int, val topics: Int, val subtopics: Int) : SyllabusUiEvent()
    data class ImportError(val message: String) : SyllabusUiEvent()
    data class TemplateImportSuccess(val templateName: String, val subjects: Int, val topics: Int, val subtopics: Int) : SyllabusUiEvent()
    data class ShowRevisionPrompt(val topicId: String, val topicName: String, val subjectName: String) : SyllabusUiEvent()
    data class ShowRevisionPromptSubtopic(val subtopicId: String, val subtopicName: String, val subjectName: String) : SyllabusUiEvent()
    data class AiGenerateSuccess(val subjects: Int, val topics: Int, val subtopics: Int) : SyllabusUiEvent()
    data class AiGenerateError(val message: String) : SyllabusUiEvent()
}

@HiltViewModel
class SyllabusViewModel @Inject constructor(
    private val repository: SyllabusRepository,
    private val syllabusDao: com.example.data.SyllabusDao,
    private val revisionDao: com.example.data.RevisionDao,
    private val revisionRepository: RevisionRepository,
    private val examDao: ExamDao,
    private val syncDao: SyncDao,
    private val settingsRepository: com.example.data.SettingsRepository
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<SyllabusUiEvent>(extraBufferCapacity = 4)
    val uiEvents: SharedFlow<SyllabusUiEvent> = _uiEvents.asSharedFlow()

    // AI syllabus generation loading state
    private val _aiLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    val exams: StateFlow<List<com.example.data.ExamEntity>> = examDao.getAllExams()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExam(name: String, dateMillis: Long) {
        viewModelScope.launch {
            val examId = UUID.randomUUID().toString()
            examDao.insertExam(
                com.example.data.ExamEntity(
                    id = examId,
                    name = name,
                    dateMillis = dateMillis
                )
            )
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "EXAM", entityId = examId))
        }
    }

    fun updateExam(exam: com.example.data.ExamEntity) {
        viewModelScope.launch {
            examDao.updateExam(exam)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "EXAM", entityId = exam.id))
        }
    }

    fun deleteExam(exam: com.example.data.ExamEntity) {
        viewModelScope.launch {
            examDao.deleteExam(exam)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "DELETE", entityType = "EXAM", entityId = exam.id))
        }
    }

    fun undoDeleteExam(exam: com.example.data.ExamEntity) {
        viewModelScope.launch {
            examDao.insertExam(exam)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "EXAM", entityId = exam.id))
        }
    }

    val subjects: StateFlow<List<SubjectWithTopics>> = repository.allSubjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSubject(name: String, icon: String, color: Long, onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val subjectId = UUID.randomUUID().toString()
            repository.insertSubject(
                SubjectEntity(
                    id = subjectId,
                    name = name,
                    icon = icon,
                    color = color
                )
            )
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "SUBJECT", entityId = subjectId))
            onCreated(subjectId)
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "DELETE", entityType = "SUBJECT", entityId = subject.id))
        }
    }

    fun addTopic(subjectId: String, name: String, estimatedMinutes: Int = 30) {
        viewModelScope.launch {
            val topicId = UUID.randomUUID().toString()
            repository.insertTopic(
                TopicEntity(
                    id = topicId,
                    subjectId = subjectId,
                    name = name,
                    isPriority = false,
                    isWeak = false,
                    estimatedMinutes = estimatedMinutes,
                    isCompleted = false
                )
            )
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "TOPIC", entityId = topicId))
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "DELETE", entityType = "TOPIC", entityId = topic.id))
        }
    }

    /**
     * Toggles a topic's completion. When marking COMPLETE, emits a [SyllabusUiEvent.ShowRevisionPrompt]
     * event instead of immediately creating a revision — the user must opt-in.
     */
    fun toggleTopicCompletion(topic: TopicEntity, subjectName: String = "Subject") {
        val completionManager = com.example.domain.CompletionManager(syllabusDao, revisionDao, syncDao)
        viewModelScope.launch {
            val newlyCompleted = !topic.isCompleted
            if (newlyCompleted) {
                // First, just mark as complete — DON'T add to revision yet
                completionManager.toggleTopicCompletion(topic, subjectName, isCompleted = true, addToRevision = false)
                // Emit prompt
                _uiEvents.tryEmit(
                    SyllabusUiEvent.ShowRevisionPrompt(topic.id, topic.name, subjectName)
                )
            } else {
                completionManager.toggleTopicCompletion(topic, subjectName, isCompleted = false, addToRevision = false)
            }
        }
    }

    /**
     * Called when the user accepts the "Add to revision?" prompt.
     */
    fun confirmAddTopicToRevision(topic: TopicEntity, subjectName: String) {
        val completionManager = com.example.domain.CompletionManager(syllabusDao, revisionDao, syncDao)
        viewModelScope.launch {
            completionManager.toggleTopicCompletion(topic, subjectName, isCompleted = true, addToRevision = true)
        }
    }

    fun addSubtopic(topicId: String, name: String) {
        viewModelScope.launch {
            val subId = UUID.randomUUID().toString()
            repository.insertSubtopic(
                SubtopicEntity(
                    id = subId,
                    topicId = topicId,
                    name = name,
                    isCompleted = false
                )
            )
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "INSERT", entityType = "SUBTOPIC", entityId = subId))
        }
    }

    fun deleteSubtopic(subtopic: SubtopicEntity) {
        viewModelScope.launch {
            repository.deleteSubtopic(subtopic)
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "DELETE", entityType = "SUBTOPIC", entityId = subtopic.id))
        }
    }

    fun toggleSubtopic(subtopic: SubtopicEntity, subjectName: String = "Subject") {
        val completionManager = com.example.domain.CompletionManager(syllabusDao, revisionDao, syncDao)
        viewModelScope.launch {
            val newlyCompleted = !subtopic.isCompleted
            if (newlyCompleted) {
                completionManager.toggleSubtopicCompletion(subtopic, subjectName, isCompleted = true, addToRevision = false)
                _uiEvents.tryEmit(
                    SyllabusUiEvent.ShowRevisionPromptSubtopic(subtopic.id, subtopic.name, subjectName)
                )
            } else {
                completionManager.toggleSubtopicCompletion(subtopic, subjectName, isCompleted = false, addToRevision = false)
            }
        }
    }

    fun confirmAddSubtopicToRevision(subtopic: SubtopicEntity, subjectName: String) {
        val completionManager = com.example.domain.CompletionManager(syllabusDao, revisionDao, syncDao)
        viewModelScope.launch {
            completionManager.toggleSubtopicCompletion(subtopic, subjectName, isCompleted = true, addToRevision = true)
        }
    }

    fun updateSubject(subject: SubjectEntity, newName: String) {
        viewModelScope.launch {
            repository.updateSubject(subject.copy(name = newName))
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "SUBJECT", entityId = subject.id))
        }
    }

    fun updateTopic(topic: TopicEntity, newName: String) {
        viewModelScope.launch {
            repository.updateTopic(topic.copy(name = newName))
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "TOPIC", entityId = topic.id))
        }
    }

    fun updateSubtopic(subtopic: SubtopicEntity, newName: String) {
        viewModelScope.launch {
            repository.updateSubtopic(subtopic.copy(name = newName))
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "SUBTOPIC", entityId = subtopic.id))
        }
    }

    fun togglePriority(topic: TopicEntity) {
        viewModelScope.launch {
            repository.updateTopic(topic.copy(isPriority = !topic.isPriority))
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "TOPIC", entityId = topic.id))
        }
    }

    fun toggleWeak(topic: TopicEntity) {
        viewModelScope.launch {
            repository.updateTopic(topic.copy(isWeak = !topic.isWeak))
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "TOPIC", entityId = topic.id))
        }
    }

    fun updateTopicEstimatedTime(topic: TopicEntity, minutes: Int) {
        viewModelScope.launch {
            repository.updateTopic(topic.copy(estimatedMinutes = minutes))
            syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "UPDATE", entityType = "TOPIC", entityId = topic.id))
        }
    }

    // ---------- JSON / Template Import ----------

    /**
     * Import a syllabus JSON file picked from the user's storage.
     */
    fun importSyllabusFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader().use { it.readText() }
                    } ?: throw IllegalArgumentException("Cannot read file")
                }
                val parsed = SyllabusImporter.parse(json)
                val (s, t, sub) = SyllabusImporter.import(context, parsed)
                _uiEvents.tryEmit(SyllabusUiEvent.ImportSuccess(s, t, sub))
            } catch (e: Throwable) {
                _uiEvents.tryEmit(SyllabusUiEvent.ImportError(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Import a built-in syllabus template (JEE, NEET, UPSC, ...).
     */
    fun importTemplate(context: Context, template: com.example.data.SyllabusTemplate) {
        viewModelScope.launch {
            try {
                val parsed = SyllabusImporter.parse(template.json)
                val (s, t, sub) = SyllabusImporter.import(context, parsed)
                _uiEvents.tryEmit(
                    SyllabusUiEvent.TemplateImportSuccess(template.name, s, t, sub)
                )
            } catch (e: Throwable) {
                _uiEvents.tryEmit(SyllabusUiEvent.ImportError(e.message ?: "Unknown error"))
            }
        }
    }

    // ---------- AI Syllabus Generation ----------

    /**
     * Generates a syllabus from a free-text prompt using Gemini AI, then
     * imports it into the database. Reuses [SyllabusImporter.import] so the
     * generated JSON goes through the same pipeline as manual imports.
     */
    fun generateSyllabusWithAi(context: Context, prompt: String) {
        if (_aiLoading.value) return
        _aiLoading.value = true
        viewModelScope.launch {
            try {
                val engine = com.example.data.ai.AiSyllabusEngine {
                    settingsRepository.geminiApiKey.value
                }
                when (val result = engine.generateSyllabus(prompt)) {
                    is com.example.data.ai.AiSyllabusResult.Success -> {
                        val (s, t, sub) = SyllabusImporter.import(context, result.syllabus)
                        _uiEvents.tryEmit(SyllabusUiEvent.AiGenerateSuccess(s, t, sub))
                    }
                    is com.example.data.ai.AiSyllabusResult.Error -> {
                        _uiEvents.tryEmit(SyllabusUiEvent.AiGenerateError(result.message))
                    }
                    is com.example.data.ai.AiSyllabusResult.NoApiKey -> {
                        _uiEvents.tryEmit(SyllabusUiEvent.AiGenerateError("No API key configured. Add your Gemini API key in Settings → AI & Intelligence."))
                    }
                }
            } catch (e: Throwable) {
                _uiEvents.tryEmit(SyllabusUiEvent.AiGenerateError(e.message ?: "Unknown error"))
            } finally {
                _aiLoading.value = false
            }
        }
    }
}
