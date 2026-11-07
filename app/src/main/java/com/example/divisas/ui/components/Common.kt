package com.example.divisas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.divisas.data.model.FxTransaction
import com.example.divisas.data.model.TxStatus
import com.example.divisas.data.model.TxType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(title = { Text(title) }, actions = actions)
}

@Composable
fun TransactionItem(
    tx: FxTransaction,
    showUser: Boolean = false,
    footer: @Composable () -> Unit = {}
) {
    val ok = tx.status == TxStatus.SUCCESS
    val container = if (ok) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.errorContainer
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(tx.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (tx.type == TxType.BUY) "COMPRA " else "VENTA ") + "%.2f %s".format(tx.amount, tx.currency),
                    fontWeight = FontWeight.Bold
                )
                Text(tx.status, fontWeight = FontWeight.Bold)
            }
            if (showUser) Text(tx.userEmail, style = MaterialTheme.typography.bodySmall)
            Text("Precio: %.4f  |  Ref: %.4f".format(tx.executedRate, tx.referenceRate))
            Text("Desviacion: %.2f%%".format(tx.deviationPct))
            if (tx.note.isNotBlank()) Text(tx.note, style = MaterialTheme.typography.bodySmall)
            if (tx.audited) Text("Auditoria: ${tx.auditResult}", fontWeight = FontWeight.SemiBold)
            Text(date, style = MaterialTheme.typography.labelSmall)
            footer()
        }
    }
}
