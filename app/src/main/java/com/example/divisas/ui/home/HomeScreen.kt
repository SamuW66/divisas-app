package com.example.divisas.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divisas.data.model.MAX_DEVIATION_PCT
import com.example.divisas.data.model.TxType
import com.example.divisas.ui.components.AppTopBar

@Composable
fun HomeScreen(onProfile: () -> Unit, vm: HomeViewModel = viewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val rate = s.rates[s.selected]

    Scaffold(topBar = {
        AppTopBar("Mercado de divisas") {
            TextButton(onClick = { vm.refresh() }) { Text("Actualizar") }
            TextButton(onClick = onProfile) { Text("Perfil") }
        }
    }) { padding ->
        if (s.loading) {
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Hola, ${s.profile?.name.orEmpty()}", fontWeight = FontWeight.Bold)
                        Text("Saldos:", style = MaterialTheme.typography.labelLarge)
                        s.profile?.balances?.entries?.sortedBy { it.key }?.forEach { (k, v) ->
                            Text("$k: %.2f".format(v))
                        }
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(s.rates.keys.toList()) { code ->
                        FilterChip(selected = code == s.selected, onClick = { vm.selectCurrency(code) },
                            label = { Text(code) })
                    }
                }
            }
            item {
                if (rate != null) {
                    Text("1 USD = %.4f %s (%s)".format(rate, s.selected, com.example.divisas.ServiceLocator.rateRepo.lastSource),
                        style = MaterialTheme.typography.titleMedium)
                    Text("Rango permitido (±%.1f%%): %.4f - %.4f".format(
                        MAX_DEVIATION_PCT, rate * (1 - MAX_DEVIATION_PCT / 100), rate * (1 + MAX_DEVIATION_PCT / 100)),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = s.type == TxType.BUY, onClick = { vm.setType(TxType.BUY) },
                        label = { Text("Comprar") })
                    FilterChip(selected = s.type == TxType.SELL, onClick = { vm.setType(TxType.SELL) },
                        label = { Text("Vender") })
                }
            }
            item {
                OutlinedTextField(s.amount, vm::setAmount, label = { Text("Cantidad de ${s.selected}") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            item {
                OutlinedTextField(s.price, vm::setPrice, label = { Text("Precio (${s.selected} por 1 USD)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            item {
                Button(onClick = vm::submit, enabled = !s.busy, modifier = Modifier.fillMaxWidth()) {
                    Text(if (s.busy) "Procesando..." else if (s.type == TxType.BUY) "Comprar" else "Vender")
                }
            }
            item {
                s.message?.let { Text(it, fontWeight = FontWeight.Bold) }
                s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
