package com.happygreen.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

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

    fun clearTokens() {
        prefs.edit().apply {
            remove("access_token")
            remove("refresh_token")
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}