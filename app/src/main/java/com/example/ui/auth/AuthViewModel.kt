package com.example.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val userName: String? = null,
    val error: String? = null,
    val isSignUp: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _uiState.value = AuthUiState(
                isLoggedIn = true,
                userEmail = currentUser.email,
                userName = currentUser.displayName
            )
        }
    }

    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(isSignUp = !_uiState.value.isSignUp, error = null)
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email aur password dono daalo")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isLoggedIn = true,
                    userEmail = user?.email,
                    userName = user?.displayName
                )
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("password is invalid") == true -> "Galat password"
                    e.message?.contains("no user record") == true -> "Ye email register nahi hai"
                    e.message?.contains("badly formatted") == true -> "Email format galat hai"
                    else -> "Login failed: ${e.message ?: "unknown error"}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Saare fields bharo")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password kam se kam 6 characters ka hona chahiye")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isLoggedIn = true,
                    userEmail = user?.email,
                    userName = name
                )
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("already in use") == true -> "Ye email pehle se registered hai"
                    e.message?.contains("badly formatted") == true -> "Email format galat hai"
                    e.message?.contains("weak password") == true -> "Password bahut weak hai (6+ chars)"
                    else -> "Signup failed: ${e.message ?: "unknown error"}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
            }
        }
    }

    fun signInAnonymously() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                auth.signInAnonymously().await()
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isLoggedIn = true,
                    userEmail = "Guest",
                    userName = "Guest User"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Guest login failed")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
