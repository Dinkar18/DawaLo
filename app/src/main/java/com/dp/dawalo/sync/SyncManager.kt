package com.dp.dawalo.sync

import android.content.Context
import android.util.Log
import com.dp.dawalo.MedNutriTrackApp
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

    fun syncAll() {
        syncScope.launch {
            try {
                syncUserProfile()
                syncFoodLogs()
                syncMedicines()
                Log.d(TAG, "Sync completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed: ${e.message}", e)
            }
        }
    }

    suspend fun syncUserProfile() {
        try {
            val unsyncedUsers = database.userDao().getUnsyncedUsers()
            if (unsyncedUsers.isEmpty()) {
                Log.d(TAG, "No user profile to sync")
                return
            }

            val api = RetrofitClient.getAuthApi(prefs)
            unsyncedUsers.forEach { user ->
                try {
                    val response = api.updateProfile(user)
                    if (response.isSuccessful) {
                        database.userDao().markAsSynced(user.id)
                        Log.d(TAG, "Synced user profile: ${user.id}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync user ${user.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user profile: ${e.message}")
        }
    }

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

    fun isOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            false
        }
    }
}
