package com.dp.dawalo.data.local.dao

import androidx.room.*
import com.dp.dawalo.data.local.entity.WaterLog

@Dao
interface WaterLogDao {
    
    @Insert
    suspend fun insert(waterLog: WaterLog)
    
    @Query("SELECT SUM(amount) FROM water_logs WHERE userId = :userId AND date = :date")
    suspend fun getTodayTotal(userId: Long, date: Long): Int?
    
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date ORDER BY time DESC")
    suspend fun getTodayLogs(userId: Long, date: Long): List<WaterLog>
    
    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun delete(id: Long)
}
