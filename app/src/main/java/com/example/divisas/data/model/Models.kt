package com.example.divisas.data.model

const val MAX_DEVIATION_PCT = 2.5
val SUPPORTED_CURRENCIES = listOf("COP", "EUR", "GBP", "JPY", "MXN", "BRL", "CAD")

object Role {
    const val USER = "USER"
    const val ADMIN = "ADMIN"
}

object TxType {
    const val BUY = "BUY"
    const val SELL = "SELL"
}

object TxStatus {
    const val SUCCESS = "EXITOSA"
    const val REJECTED = "RECHAZADA"
}

/** Documento users/{uid}. Los valores por defecto permiten que Firestore lo deserialice. */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = Role.USER,
    val photoUrl: String = "",
    val balances: Map<String, Double> = mapOf("USD" to 10000.0)
)

/** Documento transactions/{id}. */
data class FxTransaction(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val type: String = TxType.BUY,
    val currency: String = "",
    val amount: Double = 0.0,
    val referenceRate: Double = 0.0,
    val executedRate: Double = 0.0,
    val deviationPct: Double = 0.0,
    val status: String = TxStatus.SUCCESS,
    val note: String = "",
    val audited: Boolean = false,
    val auditResult: String = "",
    val timestamp: Long = 0L
)
