package com.example.divisas.data.repository

import com.example.divisas.data.model.FxTransaction
import com.example.divisas.data.model.MAX_DEVIATION_PCT
import com.example.divisas.data.model.TxStatus
import com.example.divisas.data.model.TxType
import com.example.divisas.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

class TransactionRepository(private val db: FirebaseFirestore) {

    /**
     * Registra una compra/venta. Regla: si el precio ejecutado se desvia mas de 2.5 %
     * del precio de referencia (Finnhub) la operacion queda RECHAZADA y no mueve saldos.
     * Todo ocurre dentro de una transaccion de Firestore.
     * [amount] son unidades de la divisa; [referenceRate]/[executedRate] son unidades por 1 USD.
     */
    suspend fun execute(
        user: UserProfile,
        type: String,
        currency: String,
        amount: Double,
        referenceRate: Double,
        executedRate: Double
    ): FxTransaction {
        val deviation = abs(executedRate - referenceRate) / referenceRate * 100.0
        val userRef = db.collection("users").document(user.uid)
        val txRef = db.collection("transactions").document()

        return db.runTransaction<FxTransaction> { t ->
            val snap = t.get(userRef)
            val raw = (snap.get("balances") as? Map<*, *>) ?: emptyMap<String, Any>()
            val balances = mutableMapOf<String, Double>()
            raw.forEach { (k, v) -> balances[k.toString()] = (v as Number).toDouble() }

            var status = TxStatus.SUCCESS
            var note = ""

            if (deviation > MAX_DEVIATION_PCT) {
                status = TxStatus.REJECTED
                note = "Desviacion %.2f%% supera el limite de %.1f%%".format(deviation, MAX_DEVIATION_PCT)
            } else {
                val usd = amount / executedRate
                if (type == TxType.BUY) {
                    if ((balances["USD"] ?: 0.0) < usd) {
                        status = TxStatus.REJECTED
                        note = "Saldo USD insuficiente"
                    } else {
                        balances["USD"] = (balances["USD"] ?: 0.0) - usd
                        balances[currency] = (balances[currency] ?: 0.0) + amount
                    }
                } else {
                    if ((balances[currency] ?: 0.0) < amount) {
                        status = TxStatus.REJECTED
                        note = "Saldo $currency insuficiente"
                    } else {
                        balances[currency] = (balances[currency] ?: 0.0) - amount
                        balances["USD"] = (balances["USD"] ?: 0.0) + usd
                    }
                }
            }

            if (status == TxStatus.SUCCESS) t.update(userRef, "balances", balances)

            val result = FxTransaction(
                id = txRef.id,
                userId = user.uid,
                userEmail = user.email,
                type = type,
                currency = currency,
                amount = amount,
                referenceRate = referenceRate,
                executedRate = executedRate,
                deviationPct = deviation,
                status = status,
                note = note,
                timestamp = System.currentTimeMillis()
            )
            t.set(txRef, result)
            result
        }.await()
    }

    suspend fun getUserTransactions(uid: String): List<FxTransaction> =
        db.collection("transactions").whereEqualTo("userId", uid).get().await()
            .toObjects(FxTransaction::class.java)
            .sortedByDescending { it.timestamp }

    suspend fun getAllTransactions(): List<FxTransaction> =
        db.collection("transactions").get().await()
            .toObjects(FxTransaction::class.java)
            .sortedByDescending { it.timestamp }

    /** El administrador verifica la operacion: exitosa solo si la desviacion es <= 2.5 %. */
    suspend fun audit(tx: FxTransaction) {
        val conforme = tx.deviationPct <= MAX_DEVIATION_PCT
        val result = when {
            tx.status == TxStatus.SUCCESS && conforme -> "CONFORME"
            tx.status == TxStatus.REJECTED && !conforme -> "RECHAZO CORRECTO"
            else -> "OBSERVADA"
        }
        db.collection("transactions").document(tx.id)
            .update(mapOf("audited" to true, "auditResult" to result)).await()
    }
}
