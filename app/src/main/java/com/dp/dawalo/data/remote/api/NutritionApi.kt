package com.dp.dawalo.data.remote.api

import com.dp.dawalo.data.local.entity.DailyFoodLog
import com.dp.dawalo.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface NutritionApi {
    @POST("api/nutrition/log")
    suspend fun logFood(@Body foodLog: DailyFoodLog): Response<ApiResponse<DailyFoodLog>>

    @GET("api/nutrition/today")
    suspend fun getTodayLogs(): Response<ApiResponse<List<DailyFoodLog>>>

    @GET("api/nutrition/summary")
    suspend fun getTodaySummary(): Response<ApiResponse<Map<String, Float>>>
    
    @GET("api/nutrition/date/{date}")
    suspend fun getLogsByDate(@Path("date") date: String): Response<ApiResponse<List<DailyFoodLog>>>
    
    @DELETE("api/nutrition/{id}")
    suspend fun deleteLog(@Path("id") id: Long): Response<ApiResponse<Unit>>
}
