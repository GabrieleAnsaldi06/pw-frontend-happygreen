package com.happygreen.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    // Utilizziamo l'istanza singleton di TokenManager
    private val tokenManager = TokenManager.getInstance()
    private val authService: AuthService = RetrofitInstance.authService

    // Stati osservabili
    private val _isAuthenticated = MutableStateFlow(tokenManager.isLoggedIn())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _username = MutableStateFlow<String?>(tokenManager.getUsername())
    val username: StateFlow<String?> = _username

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        // Assicuriamoci che TokenManager sia inizializzato
        if (TokenManager.getInstance() == null) {
            TokenManager.initialize(application.applicationContext)
        }
    }

    fun login(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                logDebug("Inizio processo di login per $username")
                _isLoading.value = true
                _errorMessage.value = null

                val response = authService.login(LoginRequest(username, password))
                logDebug("Risposta login - Code: ${response.code()}, Body: ${response.body()}")

                when {
                    response.isSuccessful -> {
                        response.body()?.let { tokens ->
                            tokenManager.saveUserData(
                                accessToken = tokens.access,
                                refreshToken = tokens.refresh,
                                username = username
                            )
                            RetrofitInstance.setAuthToken(tokens.access)
                            _isAuthenticated.value = true
                            _username.value = username
                            logDebug("Login riuscito, token salvato")
                            onSuccess()
                        } ?: run {
                            logError("Risposta vuota dal server")
                            _errorMessage.value = "Errore nel server"
                            onError("Errore nel server")
                        }
                    }
                    else -> {
                        val errorMsg = handleErrorResponse(response.code(), response.errorBody()?.string(), "login")
                        onError(errorMsg)
                    }
                }
            } catch (e: IOException) {
                val errorMsg = handleNetworkError(e)
                onError(errorMsg)
            } catch (e: HttpException) {
                val errorMsg = handleHttpError(e)
                onError(errorMsg)
            } catch (e: Exception) {
                val errorMsg = handleGenericError(e)
                onError(errorMsg)
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
        viewModelScope.launch {
            try {
                logDebug("Inizio registrazione per $email")
                _isLoading.value = true
                _errorMessage.value = null

                val response = authService.register(
                    RegisterRequest(
                        username = username,
                        email = email,
                        password = password,
                        password2 = confirmPassword
                    )
                )

                logDebug("Risposta registrazione - Code: ${response.code()}, Body: ${response.body()}")

                when {
                    response.isSuccessful -> {
                        logDebug("Registrazione riuscita")
                        onSuccess()
                    }
                    else -> {
                        val errorMsg = handleErrorResponse(response.code(), response.errorBody()?.string(), "registrazione")
                        onError(errorMsg)
                    }
                }
            } catch (e: IOException) {
                val errorMsg = handleNetworkError(e)
                onError(errorMsg)
            } catch (e: HttpException) {
                val errorMsg = handleHttpError(e)
                onError(errorMsg)
            } catch (e: Exception) {
                val errorMsg = handleGenericError(e)
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleErrorResponse(code: Int, errorBody: String?, operation: String): String {
        val errorMsg = when (code) {
            400 -> {
                when {
                    errorBody?.contains("username") == true -> "Username già esistente"
                    errorBody?.contains("email") == true -> "Email già registrata"
                    else -> "Richiesta malformata ($operation)"
                }
            }
            401 -> "Credenziali non valide"
            500 -> "Errore interno del server"
            else -> "Errore sconosciuto ($code)"
        }
        logError("Errore $operation: $errorMsg")
        _errorMessage.value = errorMsg
        return errorMsg
    }

    private fun handleNetworkError(e: IOException): String {
        val errorMsg = "Errore di connessione: ${e.message}"
        logError("Errore di rete", e)
        _errorMessage.value = errorMsg
        return errorMsg
    }

    private fun handleHttpError(e: HttpException): String {
        val errorMsg = "Errore server: ${e.message()}"
        logError("Errore HTTP ${e.code()}", e)
        _errorMessage.value = errorMsg
        return errorMsg
    }

    private fun handleGenericError(e: Exception): String {
        val errorMsg = "Errore: ${e.localizedMessage}"
        logError("Errore generico", e)
        _errorMessage.value = errorMsg
        return errorMsg
    }

    fun logout() {
        tokenManager.clearTokens()
        _isAuthenticated.value = false
        _username.value = null
        logDebug("Logout effettuato")
    }

    // Funzioni di logging
    private fun logDebug(message: String) = Log.d("AuthDebug", message)
    private fun logError(message: String, e: Exception? = null) = Log.e("AuthError", message, e)
}