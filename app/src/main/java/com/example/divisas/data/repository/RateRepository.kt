package com.example.divisas.data.repository

import com.example.divisas.BuildConfig
import com.example.divisas.data.model.SUPPORTED_CURRENCIES
import com.example.divisas.data.remote.FallbackRatesApi
import com.example.divisas.data.remote.FinnhubApi

class RateRepository(
    private val finnhub: FinnhubApi,
    private val fallback: FallbackRatesApi
) {
    /** Fuente usada en la ultima consulta, para mostrarla en pantalla. */
    var lastSource: String = "Finnhub"
        private set

    /** Devuelve cuantas unidades de cada divisa equivalen a 1 USD. */
    suspend fun getRates(): Map<String, Double> {
        val fromFinnhub = runCatching {
            require(BuildConfig.FINNHUB_TOKEN.isNotBlank()) { "Falta FINNHUB_TOKEN" }
            finnhub.rates("USD", BuildConfig.FINNHUB_TOKEN).quote.orEmpty()
                .filterKeys { it in SUPPORTED_CURRENCIES }
        }.getOrNull()

        if (!fromFinnhub.isNullOrEmpty()) {
            lastSource = "Finnhub"
            return fromFinnhub
        }
        lastSource = "respaldo open.er-api.com"
        return fallback.latest().rates.orEmpty().filterKeys { it in SUPPORTED_CURRENCIES }
    }
}
