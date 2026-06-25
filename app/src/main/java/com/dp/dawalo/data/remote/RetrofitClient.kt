package com.dp.dawalo.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.dp.dawalo.data.remote.api.*
import com.dp.dawalo.utils.PreferenceManager
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"
    private const val TAG = "RetrofitClient"
    
    @Volatile
    private var cachedRetrofit: Retrofit? = null
    @Volatile
    private var cachedPrefs: PreferenceManager? = null

    private fun getRetrofit(preferenceManager: PreferenceManager): Retrofit {
        // Return cached instance if prefs haven't changed
        if (cachedRetrofit != null && cachedPrefs === preferenceManager) {
            return cachedRetrofit!!
        }
        
        synchronized(this) {
            if (cachedRetrofit != null && cachedPrefs === preferenceManager) {
                return cachedRetrofit!!
            }
            
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val token = preferenceManager.token
                val request = if (!token.isNullOrEmpty()) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            cachedRetrofit = retrofit
            cachedPrefs = preferenceManager
            return retrofit
        }
    }

    fun getAuthApi(preferenceManager: PreferenceManager): AuthApi =
        getRetrofit(preferenceManager).create(AuthApi::class.java)

    fun getMedicineApi(preferenceManager: PreferenceManager): MedicineApi =
        getRetrofit(preferenceManager).create(MedicineApi::class.java)
    
    fun getNutritionApi(preferenceManager: PreferenceManager): NutritionApi =
        getRetrofit(preferenceManager).create(NutritionApi::class.java)
    
    /** Call on logout to clear cached instance */
    fun clearCache() {
        synchronized(this) {
            cachedRetrofit = null
            cachedPrefs = null
        }
    }
}
