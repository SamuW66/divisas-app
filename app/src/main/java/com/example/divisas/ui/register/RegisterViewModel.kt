package com.example.divisas.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divisas.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

class RegisterViewModel : ViewModel() {
    private val repo = ServiceLocator.authRepo
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun register(name: String, email: String, pass: String, confirm: String) {
        val error = when {
            name.isBlank() || email.isBlank() || pass.isBlank() -> "Completa todos los campos"
            pass.length < 6 -> "La contrasena debe tener al menos 6 caracteres"
            pass != confirm -> "Las contrasenas no coinciden"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repo.register(name.trim(), email.trim(), pass) }
                .onSuccess { _state.update { it.copy(loading = false, done = true) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message ?: "Error al registrar") } }
        }
    }
}
