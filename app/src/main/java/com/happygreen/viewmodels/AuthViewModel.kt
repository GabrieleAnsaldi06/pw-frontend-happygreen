package com.happygreen.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val authService: AuthService = RetrofitInstance.authService

    private val _isAuthenticated = MutableStateFlow(tokenManager.isLoggedIn())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = authService.login(LoginRequest(username, password))

                if (response.isSuccessful) {
                    val tokens = response.body()
                    if (tokens != null) {
                        // Salva i token
                        tokenManager.saveTokens(tokens.access, tokens.refresh)
                        _isAuthenticated.value = true
                        _username.value = username
                        onSuccess()
                    } else {
                        onError("Risposta vuota dal server")
                    }
                } else {
                    when (response.code()) {
                        401 -> onError("Username o password non validi")
                        else -> onError("Errore: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: IOException) {
                onError("Errore di rete. Verifica la connessione.")
            } catch (e: HttpException) {
                onError("Errore del server: ${e.message}")
            } catch (e: Exception) {
                onError("Errore sconosciuto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (password != confirmPassword) {
            onError("Le password non coincidono")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = authService.register(
                    RegisterRequest(
                        username = username,
                        email = email,
                        password = password,
                        password2 = confirmPassword
                    )
                )

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    when (response.code()) {
                        400 -> {
                            // Parsing degli errori dal server
                            if (errorBody?.contains("username") == true) {
                                onError("Username già esistente")
                            } else if (errorBody?.contains("email") == true) {
                                onError("Email già registrata")
                            } else {
                                onError("Errore nella registrazione: $errorBody")
                            }
                        }
                        else -> onError("Errore: ${response.code()}")
                    }
                }
            } catch (e: IOException) {
                onError("Errore di rete durante la registrazione")
            } catch (e: HttpException) {
                onError("Errore del server: ${e.message}")
            } catch (e: Exception) {
                onError("Errore sconosciuto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        tokenManager.clearTokens()
        _isAuthenticated.value = false
        _username.value = null
    }
}