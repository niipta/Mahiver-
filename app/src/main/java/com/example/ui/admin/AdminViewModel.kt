package com.example.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.admin.AdminRepository
import com.example.data.admin.AppConfig
import com.example.data.admin.UserProfile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = false,
    val isAdmin: Boolean = false,
    val config: AppConfig = AppConfig(),
    val users: List<UserProfile> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repo: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        checkAdmin()
    }

    fun checkAdmin() {
        viewModelScope.launch {
            val admin = repo.isAdmin()
            if (admin) {
                _state.value = _state.value.copy(isAdmin = true, isLoading = true)
                loadConfig()
                loadUsers()
            } else {
                _state.value = _state.value.copy(isAdmin = false)
            }
        }
    }

    fun loadConfig() {
        viewModelScope.launch {
            val config = repo.getAppConfig()
            // If adminUid is empty, set current user as admin
            val finalConfig = if (config.adminUid.isBlank()) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val newConfig = config.copy(adminUid = uid)
                repo.saveAppConfig(newConfig)
                newConfig
            } else config
            _state.value = _state.value.copy(config = finalConfig, isLoading = false)
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            val users = repo.getAllUsers()
            _state.value = _state.value.copy(users = users)
        }
    }

    fun saveConfig(config: AppConfig) {
        viewModelScope.launch {
            val success = repo.saveAppConfig(config)
            _state.value = _state.value.copy(
                config = config,
                message = if (success) "Settings saved!" else "Failed to save"
            )
        }
    }

    fun toggleSubscription(uid: String, subscribed: Boolean) {
        viewModelScope.launch {
            val success = repo.toggleSubscription(uid, subscribed)
            if (success) {
                _state.value = _state.value.copy(message = if (subscribed) "Subscription activated" else "Subscription removed")
                loadUsers()
            }
        }
    }

    fun toggleBlockUser(uid: String, blocked: Boolean) {
        viewModelScope.launch {
            val success = repo.toggleBlockUser(uid, blocked)
            if (success) {
                _state.value = _state.value.copy(message = if (blocked) "User blocked" else "User unblocked")
                loadUsers()
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
