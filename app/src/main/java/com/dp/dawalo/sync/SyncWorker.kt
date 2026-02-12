package com.dp.dawalo.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "sync_work"
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting background sync...")
            
            val syncManager = SyncManager(applicationContext)
            
            if (!syncManager.isOnline()) {
                Log.d(TAG, "Device offline, skipping sync")
                return Result.retry()
            }
            
            syncManager.syncFoodLogs()
            syncManager.syncMedicines()
            
            Log.d(TAG, "Background sync completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Background sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}
