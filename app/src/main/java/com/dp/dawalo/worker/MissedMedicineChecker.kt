package com.dp.dawalo.worker

import android.content.Context
import androidx.work.*
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.local.entity.MedicineStatus
import com.dp.dawalo.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MissedMedicineChecker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = applicationContext as MedNutriTrackApp
            val db = app.database
            
            val now = System.currentTimeMillis()
            val thirtyMinAgo = now - (30 * 60 * 1000)
            
            val missedLogs = db.medicineLogDao().getLogsByStatusAndTimeRange(
                MedicineStatus.MISSED, 
                thirtyMinAgo, 
                now
            )
            
            if (missedLogs.isNotEmpty()) {
                missedLogs.forEach { log ->
                    val medicine = db.medicineDao().getMedicineById(log.medicineId)
                    val user = db.userDao().getUserById(log.userId)
                    val familyMembers = db.familyMemberDao().getFamilyMembersByUserId(log.userId)
                    
                    familyMembers.filter { it.notifyOnMissed }.forEach { member ->
                        NotificationHelper.sendSMS(
                            applicationContext,
                            member.phone,
                            "${user?.name} missed ${medicine?.name} at ${log.scheduledTime}"
                        )
                    }
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<MissedMedicineChecker>(15, TimeUnit.MINUTES)
                .build() // No network constraint — SMS works offline
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "MissedMedicineChecker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
