package com.dp.dawalo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
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
        
        // Create notification channels once
        createNotificationChannels()
        
        // Pre-populate food database on first launch
        if (preferenceManager.isFirstLaunch) {
            CoroutineScope(Dispatchers.IO).launch {
                database.foodItemDao().insertAll(IndianFoodDatabase.getDefaultFoods())
                preferenceManager.isFirstLaunch = false
            }
        }
        
        // Schedule periodic background sync
        schedulePeriodicSync()
        
        // Schedule missed medicine checker
        com.dp.dawalo.worker.MissedMedicineChecker.schedule(this)
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            
            // Medicine reminder channel (no sound — VoiceAlertService handles audio)
            val reminderChannel = NotificationChannel(
                "medicine_reminder_channel",
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medicine reminders"
                enableVibration(true)
                setSound(null, null)
            }
            manager.createNotificationChannel(reminderChannel)
            
            // Voice alert service channel
            val alertChannel = NotificationChannel(
                "voice_alert_channel",
                "Voice Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ongoing voice alert for medicine reminders"
            }
            manager.createNotificationChannel(alertChannel)
        }
    }
    
    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
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
