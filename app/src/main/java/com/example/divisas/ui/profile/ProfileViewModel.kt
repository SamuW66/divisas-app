package com.example.divisas.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divisas.ServiceLocator
import com.example.divisas.data.model.FxTransaction
import com.example.divisas.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = true,
    val uploading: Boolean = false,
    val profile: UserProfile? = null,
    val history: List<FxTransaction> = emptyList(),
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val auth = ServiceLocator.authRepo
    private val media = ServiceLocator.mediaRepo
    private val txRepo = ServiceLocator.txRepo

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val profile = auth.getProfile()
                val history = txRepo.getUserTransactions(profile.uid)
                _state.update { it.copy(loading = false, profile = profile, history = history, error = null) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun uploadPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(uploading = true, error = null) }
            try {
                val url = media.upload(context, uri)
                auth.updatePhoto(url)
                val profile = auth.getProfile()
                _state.update { it.copy(uploading = false, profile = profile) }
            } catch (e: Exception) {
                _state.update { it.copy(uploading = false, error = e.message ?: "Error al subir la imagen") }
            }
        }
    }
}
