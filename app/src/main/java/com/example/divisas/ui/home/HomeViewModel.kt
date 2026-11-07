package com.example.divisas.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divisas.ServiceLocator
import com.example.divisas.data.model.TxStatus
import com.example.divisas.data.model.TxType
import com.example.divisas.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val profile: UserProfile? = null,
    val rates: Map<String, Double> = emptyMap(),
    val selected: String = "COP",
    val type: String = TxType.BUY,
    val amount: String = "",
    val price: String = "",
    val message: String? = null,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val auth = ServiceLocator.authRepo
    private val rateRepo = ServiceLocator.rateRepo
    private val txRepo = ServiceLocator.txRepo

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val profile = auth.getProfile()
                val rates = rateRepo.getRates()
                _state.update { s ->
                    val sel = if (s.selected in rates) s.selected else rates.keys.firstOrNull() ?: s.selected
                    s.copy(
                        loading = false, profile = profile, rates = rates, selected = sel,
                        price = rates[sel]?.let { "%.4f".format(it) } ?: ""
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Error al cargar datos") }
            }
        }
    }

    fun selectCurrency(code: String) = _state.update {
        it.copy(selected = code, price = it.rates[code]?.let { r -> "%.4f".format(r) } ?: "", message = null)
    }
    fun setType(type: String) = _state.update { it.copy(type = type, message = null) }
    fun setAmount(v: String) = _state.update { it.copy(amount = v) }
    fun setPrice(v: String) = _state.update { it.copy(price = v) }

    fun submit() {
        val s = _state.value
        val profile = s.profile ?: return
        val reference = s.rates[s.selected]
        val amount = s.amount.replace(',', '.').toDoubleOrNull()
        val price = s.price.replace(',', '.').toDoubleOrNull()
        if (reference == null || amount == null || amount <= 0 || price == null || price <= 0) {
            _state.update { it.copy(error = "Ingresa una cantidad y un precio validos") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            try {
                val tx = txRepo.execute(profile, s.type, s.selected, amount, reference, price)
                val newProfile = auth.getProfile()
                val msg = if (tx.status == TxStatus.SUCCESS) "Operacion exitosa"
                else "Operacion rechazada: ${tx.note}"
                _state.update { it.copy(busy = false, profile = newProfile, amount = "", message = msg) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.message ?: "Error al operar") }
            }
        }
    }
}
