package com.dp.dawalo.domain.usecase

import com.dp.dawalo.data.remote.RetrofitClient
import com.dp.dawalo.data.remote.dto.LoginRequest
import com.dp.dawalo.data.remote.dto.AuthResponse
import com.dp.dawalo.utils.PreferenceManager

class LoginUseCase(private val preferenceManager: PreferenceManager) {
    
    suspend operator fun invoke(phone: String, password: String): Result<AuthResponse?> {
        return try {
            val api = RetrofitClient.getAuthApi(preferenceManager)
            val response = api.login(LoginRequest(phone, password))
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
