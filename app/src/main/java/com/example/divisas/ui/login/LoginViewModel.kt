package com.example.divisas.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divisas.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val role: String? = null
)

class LoginViewModel : ViewModel() {
    private val repo = ServiceLocator.authRepo
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        // Si ya hay sesion activa, entrar directo segun el rol
        if (repo.currentUid != null) {
            viewModelScope.launch {
                runCatching { repo.getProfile() }
                    .onSuccess { p -> _state.update { it.copy(role = p.role) } }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Completa correo y contrasena") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repo.login(email.trim(), password) }
                .onSuccess { p -> _state.update { it.copy(loading = false, role = p.role) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message ?: "Error al iniciar sesion") } }
        }
    }

    fun consumed() = _state.update { it.copy(role = null) }
}
