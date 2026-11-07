package com.example.divisas.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divisas.ServiceLocator
import com.example.divisas.data.model.FxTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val loading: Boolean = true,
    val transactions: List<FxTransaction> = emptyList(),
    val error: String? = null
)

class AdminViewModel : ViewModel() {
    private val txRepo = ServiceLocator.txRepo
    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val list = txRepo.getAllTransactions()
                _state.update { it.copy(loading = false, transactions = list) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Error al cargar operaciones") }
            }
        }
    }

    fun audit(tx: FxTransaction) {
        viewModelScope.launch {
            try {
                txRepo.audit(tx)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Error al auditar") }
            }
        }
    }
}
