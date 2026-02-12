package com.dp.dawalo.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.dp.dawalo.data.remote.api.*
import com.dp.dawalo.utils.PreferenceManager

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"
    private const val TAG = "RetrofitClient"
    
    init {
        Log.d(TAG, "=== RetrofitClient initialized with BASE_URL: $BASE_URL ===")
    }

    private fun getOkHttpClient(preferenceManager: PreferenceManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val token = preferenceManager.token
            Log.d(TAG, "Request: ${chain.request().url}, Token present: ${token != null && token.isNotEmpty()}")
            
            val request = if (token != null && token.isNotEmpty()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            
            val response = chain.proceed(request)
            Log.d(TAG, "Response: ${response.code} for ${request.url}")
            response
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }

    private fun getRetrofit(preferenceManager: PreferenceManager): Retrofit {
        Log.d(TAG, "Creating Retrofit instance with BASE_URL: $BASE_URL")
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(preferenceManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getAuthApi(preferenceManager: PreferenceManager): AuthApi {
        Log.d(TAG, "Creating AuthApi instance")
        return getRetrofit(preferenceManager).create(AuthApi::class.java)
    }

    fun getMedicineApi(preferenceManager: PreferenceManager): MedicineApi {
        Log.d(TAG, "Creating MedicineApi instance")
        return getRetrofit(preferenceManager).create(MedicineApi::class.java)
    }
    
    fun getNutritionApi(preferenceManager: PreferenceManager): NutritionApi {
        Log.d(TAG, "Creating NutritionApi instance")
        return getRetrofit(preferenceManager).create(NutritionApi::class.java)
    }
}
