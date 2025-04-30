package com.happygreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.AuthService
import com.happygreen.data.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthViewModel : ViewModel() {

    private val authService: AuthService = RetrofitInstance.api

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = authService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        // Salva il token JWT se necessario
                        onSuccess()
                    } else {
                        onError("Token non ricevuto.")
                    }
                } else {
                    onError("Credenziali non valide.")
                }
            } catch (e: IOException) {
                onError("Errore di rete. Riprova.")
            } catch (e: HttpException) {
                onError("Errore del server. ${e.message}")
            }
        }
    }

    fun register(
        email: String,
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = authService.register(RegisterRequest(email, username, password))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Registrazione fallita: ${response.code()}")
                }
            } catch (e: IOException) {
                onError("Errore di rete durante la registrazione.")
            } catch (e: HttpException) {
                onError("Errore del server. ${e.message}")
            }
        }
    }

}

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

data class RegisterResponse(
    val message: String
)


data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)
