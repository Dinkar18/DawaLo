package com.dp.dawalo.data.remote.api

import com.dp.dawalo.data.local.entity.User
import com.dp.dawalo.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: Map<String, String>): ApiResponse<Void>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: Map<String, String>): ApiResponse<Boolean>

    @POST("api/auth/login-otp")
    suspend fun loginWithOtp(@Body request: Map<String, String>): ApiResponse<AuthResponse>

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body user: User): Response<ApiResponse<User>>
}
