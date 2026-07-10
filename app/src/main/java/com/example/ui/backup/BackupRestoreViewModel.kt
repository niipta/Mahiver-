package com.example.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class BackupRestoreUiState(
    val isProcessing: Boolean = false,
    val message: String? = null,
    val error: Boolean = false
)

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val repository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    fun createBackup(uri: Uri) {
        _uiState.update { it.copy(isProcessing = true, message = "Creating backup...", error = false) }
        viewModelScope.launch {
            val result = repository.createBackup(uri)
            if (result.isSuccess) {
                val summary = result.getOrNull() ?: ""
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = if (summary.isNotEmpty()) "Backup saved! ($summary)" else "Backup saved successfully!"
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = "Failed to create backup: $errorMsg",
                        error = true
                    )
                }
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        _uiState.update { it.copy(isProcessing = true, message = "Restoring data...", error = false) }
        viewModelScope.launch {
            val result = repository.restoreBackup(uri)
            if (result.isSuccess) {
                val summary = result.getOrNull() ?: ""
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = if (summary.isNotEmpty()) "Restored! ($summary)" else "Data restored successfully!"
                    )
                }
            } else {
                // Show the actual error message from the repository instead of a
                // generic "Invalid file or format" — the repo returns specific
                // messages like "Invalid backup format: not a valid MahirVerse backup file"
                val errorMsg = result.exceptionOrNull()?.message
                    ?: result.exceptionOrNull()?.javaClass?.simpleName
                    ?: "Unknown error"
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = "Restore failed: $errorMsg",
                        error = true
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = false) }
    }
}
