package com.dp.dawalo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dp.dawalo.data.local.entity.DailyFoodLog

@Dao
interface DailyFoodLogDao {
    @Insert
    suspend fun insert(log: DailyFoodLog): Long
    
    @Update
    suspend fun update(log: DailyFoodLog)
    
    @Query("SELECT * FROM daily_food_logs WHERE userId = :userId AND date = :date")
    fun getTodayLogs(userId: Long, date: Long): LiveData<List<DailyFoodLog>>
    
    @Query("SELECT * FROM daily_food_logs WHERE userId = :userId AND time BETWEEN :startTime AND :endTime ORDER BY time DESC")
    suspend fun getLogsBetween(userId: Long, startTime: Long, endTime: Long): List<DailyFoodLog>
    
    @Query("SELECT * FROM daily_food_logs WHERE needsSync = 1 AND userId = :userId")
    suspend fun getUnsyncedLogs(userId: Long): List<DailyFoodLog>
    
    @Query("UPDATE daily_food_logs SET isSynced = 1, needsSync = 0, serverId = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: Long, serverId: Long)
    
    @Query("SELECT SUM(protein) FROM daily_food_logs WHERE userId = :userId AND date = :date")
    suspend fun getTodayTotalProtein(userId: Long, date: Long): Float?
    
    @Query("SELECT SUM(calories) FROM daily_food_logs WHERE userId = :userId AND date = :date")
    suspend fun getTodayTotalCalories(userId: Long, date: Long): Float?
    
    @Query("SELECT * FROM daily_food_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getLogsInRange(userId: Long, startDate: Long, endDate: Long): List<DailyFoodLog>
    
    @Query("DELETE FROM daily_food_logs WHERE id = :logId")
    suspend fun deleteById(logId: Long)
    
    @Delete
    suspend fun delete(log: DailyFoodLog)
}
