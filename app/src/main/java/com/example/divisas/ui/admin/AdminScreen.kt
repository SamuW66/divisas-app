package com.example.divisas.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divisas.data.model.MAX_DEVIATION_PCT
import com.example.divisas.data.model.TxStatus
import com.example.divisas.ui.components.AppTopBar
import com.example.divisas.ui.components.TransactionItem

@Composable
fun AdminScreen(onLogout: () -> Unit, vm: AdminViewModel = viewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val total = s.transactions.size
    val ok = s.transactions.count { it.status == TxStatus.SUCCESS }
    val rejected = total - ok
    val anomalies = s.transactions.count { it.status == TxStatus.SUCCESS && it.deviationPct > MAX_DEVIATION_PCT }

    Scaffold(topBar = {
        AppTopBar("Auditoria (Admin)") {
            TextButton(onClick = { vm.load() }) { Text("Actualizar") }
            TextButton(onClick = onLogout) { Text("Salir") }
        }
    }) { padding ->
        if (s.loading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Resumen", fontWeight = FontWeight.Bold)
                        Text("Total: $total  |  Exitosas: $ok  |  Rechazadas: $rejected")
                        Text("Anomalias (exitosas con desv. > %.1f%%): $anomalies".format(MAX_DEVIATION_PCT))
                    }
                }
            }
            s.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (total == 0) item { Text("No hay operaciones registradas") }
            items(s.transactions, key = { it.id }) { tx ->
                TransactionItem(tx, showUser = true) {
                    if (!tx.audited) {
                        Button(onClick = { vm.audit(tx) }) { Text("Auditar") }
                    }
                }
            }
        }
    }
}
