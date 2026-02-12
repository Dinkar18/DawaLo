package com.dp.dawalo.data.remote.api

import com.dp.dawalo.data.local.entity.Medicine
import retrofit2.Response
import retrofit2.http.*

interface MedicineApi {
    @GET("api/medicines")
    suspend fun getMedicines(): Response<List<Medicine>>

    @POST("api/medicines")
    suspend fun addMedicine(@Body medicine: Medicine): Response<Medicine>

    @DELETE("api/medicines/{id}")
    suspend fun deleteMedicine(@Path("id") id: Long): Response<Unit>
}
