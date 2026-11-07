package com.example.divisas

import com.example.divisas.data.remote.CloudinaryApi
import com.example.divisas.data.remote.FallbackRatesApi
import com.example.divisas.data.remote.FinnhubApi
import com.example.divisas.data.repository.AuthRepository
import com.example.divisas.data.repository.MediaRepository
import com.example.divisas.data.repository.RateRepository
import com.example.divisas.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Inyeccion de dependencias manual y sencilla (singleton). */
object ServiceLocator {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val finnhubApi: FinnhubApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://finnhub.io/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(FinnhubApi::class.java)
    }
    private val cloudinaryApi: CloudinaryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CloudinaryApi::class.java)
    }

    private val fallbackApi: FallbackRatesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(FallbackRatesApi::class.java)
    }

    val authRepo by lazy { AuthRepository(auth, db) }
    val rateRepo by lazy { RateRepository(finnhubApi, fallbackApi) }
    val txRepo by lazy { TransactionRepository(db) }
    val mediaRepo by lazy { MediaRepository(cloudinaryApi) }
}
