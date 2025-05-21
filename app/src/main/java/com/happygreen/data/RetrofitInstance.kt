package com.happygreen.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://7884-79-8-186-194.ngrok-free.app/"
    private var authToken: String? = null

    fun setAuthToken(token: String) {
        authToken = token
        Log.d("RetrofitInstance", "Token impostato: $token")
    }

    // Interceptor per il logging delle richieste e risposte
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor per l'aggiunta dell'header di autenticazione
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // Se il token non è stato impostato direttamente, proviamo a ottenerlo dal TokenManager
        if (authToken == null) {
            try {
                val tokenFromManager = TokenManager.getInstance().getAccessToken()
                if (tokenFromManager != null) {
                    authToken = tokenFromManager
                    Log.d("RetrofitInstance", "Token recuperato da TokenManager: $authToken")
                }
            } catch (e: Exception) {
                Log.e("RetrofitInstance", "Errore nel recupero del token: ${e.message}")
            }
        }

        Log.d("AuthCheck", "Token: $authToken")

        // Aggiungiamo l'header di autenticazione se il token è disponibile
        val newRequest = if (authToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    // Configurazione del client HTTP
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    // Configurazione di Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Servizi API
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}