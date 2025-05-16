package com.happygreen.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {
    @POST("api/token/")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/users/")
    @Headers("Content-Type: application/json")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val access: String,
    val refresh: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val password2: String,
    val first_name: String = "",
    val last_name: String = ""
)

data class RegisterResponse(
    val id: Int,
    val username: String,
    val email: String
)