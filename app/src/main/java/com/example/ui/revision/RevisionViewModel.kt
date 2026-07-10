package com.example.ui.revision

import androidx.lifecycle.viewModelScope
import com.example.data.RevisionEntity
import com.example.data.RevisionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel

@HiltViewModel
class RevisionViewModel @Inject constructor(
    private val repository: RevisionRepository
) : ViewModel() {

    val revisions: StateFlow<List<RevisionEntity>> = repository.allRevisions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Extended spaced repetition schedule that does NOT terminate at L4.
     * Instead, follows an SM-2-inspired growth: 1 → 3 → 7 → 15 → 30 → 60 → 120 days.
     * Beyond L7, intervals double with a 365-day cap.
     */
    private fun nextIntervalDays(level: Int): Long = when (level) {
        1 -> 3L
        2 -> 7L
        3 -> 15L
        4 -> 30L
        5 -> 60L
        6 -> 120L
        else -> minOf(365L, (1L shl (level - 1))) // 2^(level-1) capped at 1 year
    }

    fun toggleRevisionCompletion(revision: RevisionEntity, onCompletion: (RevisionEntity) -> Unit = {}) {
        viewModelScope.launch {
            val newlyCompleted = !revision.isCompleted
            repository.updateRevision(revision.copy(isCompleted = newlyCompleted))

            if (newlyCompleted) {
                val nextInterval = nextIntervalDays(revision.repetitionLevel)
                val nextRevision = revision.copy(
                    id = UUID.randomUUID().toString(),
                    isCompleted = false,
                    scheduledDateMillis = System.currentTimeMillis() + (nextInterval * 86_400_000L),
                    repetitionLevel = revision.repetitionLevel + 1
                )
                repository.insertRevision(nextRevision)
                onCompletion(nextRevision)
            }
        }
    }

    fun undoRevisionCompletion(completedRevision: RevisionEntity, futureRevisionId: String) {
        viewModelScope.launch {
            repository.updateRevision(completedRevision.copy(isCompleted = false))
            if (futureRevisionId.isNotEmpty()) {
                val allRevs = repository.allRevisions.first()
                val futureRevision = allRevs.find { it.id == futureRevisionId }
                if (futureRevision != null) {
                    repository.deleteRevision(futureRevision)
                }
            }
        }
    }

    fun deleteRevision(revision: RevisionEntity) {
        viewModelScope.launch {
            repository.deleteRevision(revision)
        }
    }

    fun addCustomRevision(
        title: String,
        subjectName: String = "Custom",
        priority: String = "Medium",
        estimatedMinutes: Int = 15
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val entity = RevisionEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                subjectName = subjectName,
                relatedId = "custom_${System.currentTimeMillis()}",
                scheduledDateMillis = System.currentTimeMillis(),
                type = "Custom",
                repetitionLevel = 1,
                priority = priority,
                confidence = 0,
                estimatedMinutes = estimatedMinutes
            )
            repository.insertRevision(entity)
        }
    }

    fun rescheduleRevision(revision: RevisionEntity, daysFromNow: Int) {
        viewModelScope.launch {
            repository.updateRevision(
                revision.copy(scheduledDateMillis = System.currentTimeMillis() + daysFromNow * 86_400_000L)
            )
        }
    }

    fun updateConfidence(revision: RevisionEntity, confidence: Int) {
        viewModelScope.launch {
            repository.updateRevision(revision.copy(confidence = confidence.coerceIn(0, 5)))
        }
    }

    fun updatePriority(revision: RevisionEntity, priority: String) {
        viewModelScope.launch {
            repository.updateRevision(revision.copy(priority = priority))
        }
    }
}
