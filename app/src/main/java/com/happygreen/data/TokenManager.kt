package com.happygreen.data

import android.content.Context
import android.content.SharedPreferences

// Aggiunto pattern Singleton per permettere l'accesso globale
class TokenManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Metodi esistenti
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }

    fun saveUserData(accessToken: String, refreshToken: String, username: String) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("username", username)
            apply()
        }
    }

    fun getUsername(): String? {
        return prefs.getString("username", null)
    }

    fun clearTokens() {
        prefs.edit().apply {
            remove("access_token")
            remove("refresh_token")
            remove("username")
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    // Pattern Singleton
    companion object {
        @Volatile
        private var INSTANCE: TokenManager? = null

        // Funzione per inizializzare il Singleton (chiamare in Application o MainActivity)
        fun initialize(context: Context) {
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Funzione per accedere all'istanza
        fun getInstance(): TokenManager {
            return INSTANCE ?: throw IllegalStateException("TokenManager deve essere inizializzato prima dell'uso")
        }
    }
}