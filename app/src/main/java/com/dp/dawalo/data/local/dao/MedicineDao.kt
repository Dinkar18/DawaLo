package com.dp.dawalo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.data.local.entity.MedicineLog

@Dao
interface MedicineDao {
    @Insert
    suspend fun insert(medicine: Medicine): Long
    
    @Insert
    suspend fun insert(log: MedicineLog)
    
    @Update
    suspend fun update(medicine: Medicine)
    
    @Delete
    suspend fun delete(medicine: Medicine)
    
    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 ORDER BY startDate DESC")
    fun getAllMedicines(userId: Long): LiveData<List<Medicine>>
    
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): Medicine?
    
    @Query("SELECT * FROM medicines WHERE needsSync = 1 AND userId = :userId")
    suspend fun getUnsyncedMedicines(userId: Long): List<Medicine>
    
    @Query("UPDATE medicines SET isSynced = 1, needsSync = 0, serverId = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: Long, serverId: Long)
}
