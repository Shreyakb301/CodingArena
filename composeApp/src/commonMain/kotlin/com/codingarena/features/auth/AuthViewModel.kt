package com.codingarena.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.model.LoginRequest
import com.codingarena.domain.model.RegisterRequest
import com.codingarena.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthMode { LOG_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOG_IN,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() &&
            (mode == AuthMode.LOG_IN || displayName.isNotBlank())
}

/**
 * Email/password auth only - a plain [ClassroomGateway] login or register,
 * nothing else. Deliberately separate from Settings' own account section,
 * which also pulls and pushes *course* progress through a different,
 * classroom-oriented endpoint; this screen's job is just to get a session
 * token. On the web build, cross-device sync runs on its own (see `sync.js`)
 * once a token exists, regardless of which screen obtained it.
 */
class AuthViewModel(
    private val gateway: ClassroomGateway,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun setMode(mode: AuthMode) = update { it.copy(mode = mode, error = null) }
    fun setName(value: String) = update { it.copy(displayName = value) }
    fun setEmail(value: String) = update { it.copy(email = value) }
    fun setPassword(value: String) = update { it.copy(password = value) }

    fun submit() {
        val current = _state.value
        if (current.busy || !current.canSubmit) return
        _state.value = current.copy(busy = true, error = null)
        viewModelScope.launch {
            val result = runCatching {
                when (current.mode) {
                    AuthMode.LOG_IN -> gateway.login(LoginRequest(current.email.trim(), current.password))
                    AuthMode.SIGN_UP -> gateway.register(
                        RegisterRequest(
                            displayName = current.displayName.trim(),
                            email = current.email.trim(),
                            password = current.password,
                            role = UserRole.STUDENT,
                        ),
                    )
                }
            }
            _state.value = result.fold(
                onSuccess = { _state.value.copy(busy = false, done = true) },
                onFailure = { error -> _state.value.copy(busy = false, error = error.message ?: "Could not sign in") },
            )
        }
    }

    private inline fun update(block: (AuthUiState) -> AuthUiState) {
        _state.value = block(_state.value)
    }
}
