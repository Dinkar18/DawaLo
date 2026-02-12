package com.dp.dawalo.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.dp.dawalo.data.local.dao.MedicineDao
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.data.remote.RetrofitClient
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicineRepository(
    private val medicineDao: MedicineDao,
    private val prefs: PreferenceManager? = null
) {
    
    companion object {
        private const val TAG = "MedicineRepository"
    }
    
    fun getAllMedicines(userId: Long): LiveData<List<Medicine>> {
        return medicineDao.getAllMedicines(userId)
    }
    
    suspend fun insert(medicine: Medicine): Long {
        // 1. Save to local DB first (instant)
        val localId = medicineDao.insert(medicine)
        
        // 2. Sync to backend in background
        prefs?.let { syncToBackend(medicine.copy(id = localId)) }
        
        return localId
    }
    
    private fun syncToBackend(medicine: Medicine) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                prefs?.let {
                    val api = RetrofitClient.getMedicineApi(it)
                    val response = api.addMedicine(medicine)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val serverId = response.body()!!.id
                        medicineDao.markAsSynced(medicine.id, serverId)
                        Log.d(TAG, "Medicine synced: ${medicine.id} -> $serverId")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Backend sync failed, will retry later: ${e.message}")
            }
        }
    }
    
    suspend fun update(medicine: Medicine) {
        medicineDao.update(medicine)
    }
    
    suspend fun delete(medicine: Medicine) {
        medicineDao.delete(medicine)
        
        // Try to delete from backend
        prefs?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    medicine.serverId?.let { serverId ->
                        val api = RetrofitClient.getMedicineApi(it)
                        api.deleteMedicine(serverId)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Backend delete failed: ${e.message}")
                }
            }
        }
    }
    
    suspend fun getMedicineById(id: Long): Medicine? {
        return medicineDao.getMedicineById(id)
    }
}
