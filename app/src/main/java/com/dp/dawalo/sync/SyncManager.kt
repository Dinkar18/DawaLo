package com.dp.dawalo.sync

import android.content.Context
import android.util.Log
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.local.entity.DailyFoodLog
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.data.remote.RetrofitClient
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncManager(private val context: Context) {
    
    private val prefs = PreferenceManager(context)
    private val database = (context.applicationContext as MedNutriTrackApp).database
    private val syncScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val TAG = "SyncManager"
    }
    
    /**
     * Sync all unsynced data to backend
     */
    fun syncAll() {
        syncScope.launch {
            try {
                syncFoodLogs()
                syncMedicines()
                Log.d(TAG, "Sync completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed: ${e.message}", e)
            }
        }
    }
    
    /**
     * Sync food logs to backend
     */
    suspend fun syncFoodLogs() {
        try {
            val unsyncedLogs = database.dailyFoodLogDao().getUnsyncedLogs(prefs.userId)
            
            if (unsyncedLogs.isEmpty()) {
                Log.d(TAG, "No food logs to sync")
                return
            }
            
            val api = RetrofitClient.getNutritionApi(prefs)
            
            unsyncedLogs.forEach { log ->
                try {
                    val response = api.logFood(log)
                    
                    if (response.isSuccessful && response.body()?.data != null) {
                        val serverId = response.body()!!.data!!.id
                        database.dailyFoodLogDao().markAsSynced(log.id, serverId)
                        Log.d(TAG, "Synced food log: ${log.id} -> $serverId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync food log ${log.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing food logs: ${e.message}")
        }
    }
    
    /**
     * Sync medicines to backend
     */
    suspend fun syncMedicines() {
        try {
            val unsyncedMedicines = database.medicineDao().getUnsyncedMedicines(prefs.userId)
            
            if (unsyncedMedicines.isEmpty()) {
                Log.d(TAG, "No medicines to sync")
                return
            }
            
            val api = RetrofitClient.getMedicineApi(prefs)
            
            unsyncedMedicines.forEach { medicine ->
                try {
                    val response = api.addMedicine(medicine)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val serverId = response.body()!!.id
                        database.medicineDao().markAsSynced(medicine.id, serverId)
                        Log.d(TAG, "Synced medicine: ${medicine.id} -> $serverId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync medicine ${medicine.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing medicines: ${e.message}")
        }
    }
    
    /**
     * Check if device has internet connection
     */
    fun isOnline(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            false
        }
    }
}
