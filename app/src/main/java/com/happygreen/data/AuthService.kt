package com.happygreen.data

import com.happygreen.viewmodel.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

}