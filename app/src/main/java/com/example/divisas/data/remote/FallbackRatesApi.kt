package com.example.divisas.data.remote

import retrofit2.http.GET

data class FallbackRatesResponse(val rates: Map<String, Double>?)

/** Respaldo gratuito (sin token) cuando Finnhub no da acceso a forex. */
interface FallbackRatesApi {
    @GET("v6/latest/USD")
    suspend fun latest(): FallbackRatesResponse
}
