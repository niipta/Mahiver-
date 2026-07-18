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
                _uiState.update { it.copy(isProcessing = false, message = "Backup saved successfully!") }
            } else {
                _uiState.update { it.copy(isProcessing = false, message = "Failed to create backup.", error = true) }
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        _uiState.update { it.copy(isProcessing = true, message = "Restoring data...", error = false) }
        viewModelScope.launch {
            val result = repository.restoreBackup(uri)
            if (result.isSuccess) {
                _uiState.update { it.copy(isProcessing = false, message = "Data restored successfully!") }
            } else {
                _uiState.update { it.copy(isProcessing = false, message = "Failed to restore data. Invalid file or format.", error = true) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = false) }
    }
}
