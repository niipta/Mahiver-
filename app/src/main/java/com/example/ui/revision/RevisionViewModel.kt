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

    /**
     * Marks a revision complete and schedules the next spaced-repetition
     * instance. Returns the newly-created future revision via [onCompletion]
     * so the UI can offer an undo action.
     */
    fun toggleRevisionCompletion(revision: RevisionEntity, onCompletion: (RevisionEntity) -> Unit = {}) {
        viewModelScope.launch {
            val newlyCompleted = !revision.isCompleted
            if (newlyCompleted) {
                // Mark this instance complete
                repository.updateRevision(revision.copy(isCompleted = true))

                // Schedule the next repetition
                val nextInterval = nextIntervalDays(revision.repetitionLevel)
                val nextRevision = revision.copy(
                    id = UUID.randomUUID().toString(),
                    isCompleted = false,
                    isActive = true,
                    scheduledDateMillis = System.currentTimeMillis() + (nextInterval * 86_400_000L),
                    repetitionLevel = revision.repetitionLevel + 1
                )
                repository.insertRevision(nextRevision)
                onCompletion(nextRevision)
            } else {
                // Un-marking: just flip the flag back
                repository.updateRevision(revision.copy(isCompleted = false))
            }
        }
    }

    /**
     * Undo a completion: revert the original revision to incomplete and delete
     * the future repetition that was created.
     */
    fun undoRevisionCompletion(completedRevision: RevisionEntity, futureRevisionId: String) {
        viewModelScope.launch {
            repository.updateRevision(completedRevision.copy(isCompleted = false, isActive = true))
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

    /**
     * Permanently delete a completed revision (used when user swipes to delete
     * in the "Completed" section). This does NOT create a new repetition.
     */
    fun deleteCompletedRevision(revision: RevisionEntity) {
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
                estimatedMinutes = estimatedMinutes,
                isActive = true
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

    /**
     * Update confidence. Stored as 0-100 in the entity (matches the
     * RevisionEntity.confidence field range). The UI converts to 0-5 stars.
     */
    fun updateConfidence(revision: RevisionEntity, confidence: Int) {
        viewModelScope.launch {
            repository.updateRevision(revision.copy(confidence = confidence.coerceIn(0, 100)))
        }
    }

    fun updatePriority(revision: RevisionEntity, priority: String) {
        viewModelScope.launch {
            repository.updateRevision(revision.copy(priority = priority))
        }
    }
}
