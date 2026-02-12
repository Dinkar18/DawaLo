package com.dp.dawalo.data.remote.dto

data class LoginRequest(
    val phone: String,
    val password: String
)

data class RegisterRequest(
    val phone: String,
    val password: String,
    val name: String,
    val age: Int,
    val gender: String,
    val weight: Float,
    val height: Float,
    val goal: String,
    val dietType: String,
    val activityLevel: String,
    val languageCode: String
)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val name: String,
    val phone: String
)
