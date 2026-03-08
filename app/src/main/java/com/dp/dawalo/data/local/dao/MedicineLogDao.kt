package com.dp.dawalo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dp.dawalo.data.local.entity.MedicineLog
import com.dp.dawalo.data.local.entity.MedicineStatus

@Dao
interface MedicineLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MedicineLog): Long
    
    @Query("SELECT * FROM medicine_logs WHERE medicineId = :medicineId ORDER BY scheduledTime DESC")
    fun getLogsForMedicine(medicineId: Long): LiveData<List<MedicineLog>>
    
    @Query("SELECT * FROM medicine_logs WHERE scheduledTime >= :startTime AND scheduledTime <= :endTime ORDER BY scheduledTime DESC")
    fun getLogsBetween(startTime: Long, endTime: Long): LiveData<List<MedicineLog>>
    
    @Query("SELECT * FROM medicine_logs WHERE status = :status ORDER BY scheduledTime DESC")
    fun getLogsByStatus(status: MedicineStatus): LiveData<List<MedicineLog>>
    
    @Query("SELECT COUNT(*) FROM medicine_logs WHERE medicineId = :medicineId AND status = 'TAKEN'")
    suspend fun getTakenCount(medicineId: Long): Int
    
    @Query("SELECT COUNT(*) FROM medicine_logs WHERE medicineId = :medicineId AND status = 'MISSED'")
    suspend fun getMissedCount(medicineId: Long): Int
    
    @Query("SELECT COUNT(*) FROM medicine_logs WHERE medicineId = :medicineId AND status = 'SKIPPED'")
    suspend fun getSkippedCount(medicineId: Long): Int
    
    @Query("DELETE FROM medicine_logs WHERE medicineId = :medicineId")
    suspend fun deleteLogsForMedicine(medicineId: Long)
    
    @Query("SELECT * FROM medicine_logs WHERE userId = :userId ORDER BY scheduledTime DESC")
    suspend fun getLogsByUserId(userId: Long): List<MedicineLog>
    
    @Query("SELECT * FROM medicine_logs WHERE status = :status AND scheduledTime >= :startTime AND scheduledTime <= :endTime")
    suspend fun getLogsByStatusAndTimeRange(status: MedicineStatus, startTime: Long, endTime: Long): List<MedicineLog>
}
