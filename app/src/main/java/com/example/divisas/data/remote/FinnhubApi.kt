package com.example.divisas.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

data class RatesResponse(val base: String?, val quote: Map<String, Double>?)

interface FinnhubApi {
    @GET("forex/rates")
    suspend fun rates(
        @Query("base") base: String,
        @Query("token") token: String
    ): RatesResponse
}
