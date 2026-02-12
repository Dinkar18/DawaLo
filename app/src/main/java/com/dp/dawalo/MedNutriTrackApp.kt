package com.dp.dawalo

import android.app.Application
import androidx.work.*
import com.dp.dawalo.data.local.AppDatabase
import com.dp.dawalo.sync.SyncWorker
import com.dp.dawalo.utils.IndianFoodDatabase
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MedNutriTrackApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val preferenceManager by lazy { PreferenceManager(this) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Pre-populate food database on first launch
        if (preferenceManager.isFirstLaunch) {
            CoroutineScope(Dispatchers.IO).launch {
                database.foodItemDao().insertAll(IndianFoodDatabase.getDefaultFoods())
                preferenceManager.isFirstLaunch = false
            }
        }
        
        // Schedule periodic background sync
        schedulePeriodicSync()
    }
    
    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES // Sync every 15 minutes
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.SECONDS
            )
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
