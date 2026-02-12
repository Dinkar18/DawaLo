package com.dp.dawalo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dp.dawalo.data.local.dao.DailyFoodLogDao
import com.dp.dawalo.data.local.entity.DailyFoodLog
import com.dp.dawalo.data.remote.RetrofitClient
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.util.Calendar

class NutritionViewModel(
    private val preferenceManager: PreferenceManager,
    private val foodLogDao: DailyFoodLogDao
) : ViewModel() {

    private val _todayLogs = MutableLiveData<List<DailyFoodLog>>()
    val todayLogs: LiveData<List<DailyFoodLog>> = _todayLogs

    private val _summary = MutableLiveData<Map<String, Float>>()
    val summary: LiveData<Map<String, Float>> = _summary

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    companion object {
        private const val TAG = "NutritionViewModel"
    }

    fun loadTodayData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val startOfDay = getStartOfDay()
                val endOfDay = getEndOfDay()
                
                // 1. Load from local DB FIRST (instant)
                val logs = foodLogDao.getLogsBetween(preferenceManager.userId, startOfDay, endOfDay)
                _todayLogs.value = logs
                
                val totalProtein = logs.sumOf { it.protein.toDouble() }.toFloat()
                val totalCalories = logs.sumOf { it.calories.toDouble() }.toFloat()
                
                _summary.value = mapOf(
                    "protein" to totalProtein,
                    "calories" to totalCalories
                )
                
                // 2. Try to fetch from backend (background)
                syncFromBackend()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data: ${e.message}", e)
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    
    private suspend fun syncFromBackend() {
        try {
            val api = RetrofitClient.getNutritionApi(preferenceManager)
            val response = api.getTodaySummary()
            
            if (response.isSuccessful && response.body()?.data != null) {
                _syncStatus.value = "Synced with server"
                Log.d(TAG, "Successfully synced with backend")
            }
        } catch (e: Exception) {
            Log.d(TAG, "Backend sync failed (offline mode): ${e.message}")
            _syncStatus.value = "Offline mode"
        }
    }

    fun logFood(foodLog: DailyFoodLog, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Save to local DB FIRST (instant)
                val localId = foodLogDao.insert(foodLog)
                loadTodayData() // Refresh UI immediately
                onSuccess()
                
                // 2. Sync to backend in background
                syncFoodLogToBackend(foodLog.copy(id = localId))
                
            } catch (e: Exception) {
                Log.e(TAG, "Error logging food: ${e.message}", e)
                _error.value = e.message
            }
        }
    }
    
    private suspend fun syncFoodLogToBackend(foodLog: DailyFoodLog) {
        try {
            val api = RetrofitClient.getNutritionApi(preferenceManager)
            val response = api.logFood(foodLog)
            
            if (response.isSuccessful && response.body()?.data != null) {
                val serverId = response.body()!!.data!!.id
                foodLogDao.markAsSynced(foodLog.id, serverId)
                Log.d(TAG, "Food log synced to backend: ${foodLog.id} -> $serverId")
                _syncStatus.value = "Synced"
            }
        } catch (e: Exception) {
            Log.d(TAG, "Backend sync failed, will retry later: ${e.message}")
            _syncStatus.value = "Pending sync"
        }
    }

    fun deleteLog(logId: Long) {
        viewModelScope.launch {
            try {
                // Delete from local DB
                foodLogDao.deleteById(logId)
                loadTodayData()
                
                // Try to delete from backend
                try {
                    val api = RetrofitClient.getNutritionApi(preferenceManager)
                    api.deleteLog(logId)
                } catch (e: Exception) {
                    Log.d(TAG, "Backend delete failed (offline): ${e.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting log: ${e.message}", e)
                _error.value = e.message
            }
        }
    }
    
    private fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    private fun getEndOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
