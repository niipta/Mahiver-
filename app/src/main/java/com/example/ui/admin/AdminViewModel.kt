package com.example.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.admin.AdminRepository
import com.example.data.admin.AppConfig
import com.example.data.admin.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val noAdminSet: Boolean = false, // true if no admin has been set yet
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

    /**
     * Checks if current user is admin. If no admin has been set yet
     * (adminUid is empty in Firestore), shows "Become Admin" option.
     */
    fun checkAdmin() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                _state.value = _state.value.copy(isLoading = false, isAdmin = false)
                return@launch
            }
            val config = repo.getAppConfig()
            when {
                config.adminUid == uid -> {
                    // Already admin
                    _state.value = _state.value.copy(isLoading = false, isAdmin = true, config = config, noAdminSet = false)
                    loadUsers()
                }
                config.adminUid.isBlank() -> {
                    // No admin set yet — show "Become Admin" option
                    _state.value = _state.value.copy(isLoading = false, isAdmin = false, noAdminSet = true, config = config)
                }
                else -> {
                    // Admin is someone else
                    _state.value = _state.value.copy(isLoading = false, isAdmin = false, noAdminSet = false, config = config)
                }
            }
        }
    }

    /**
     * Makes the current user the admin. Called when user taps "Become Admin".
     * Only works if no admin has been set yet (noAdminSet == true).
     */
    fun becomeAdmin() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Admin"
            val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
            val config = _state.value.config.copy(adminUid = uid)
            val success = repo.saveAppConfig(config)
            if (success) {
                // Also create/update user profile in Firestore
                repo.updateUserProfile(UserProfile(
                    uid = uid,
                    name = userName,
                    email = email,
                    points = 0,
                    streak = 0,
                    isSubscribed = true,
                    isBlocked = false
                ))
                _state.value = _state.value.copy(isAdmin = true, noAdminSet = false, config = config, message = "You are now the admin!")
                loadUsers()
            } else {
                _state.value = _state.value.copy(message = "Failed to become admin. Check your connection.")
            }
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
