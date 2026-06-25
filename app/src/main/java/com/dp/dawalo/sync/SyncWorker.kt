package com.dp.dawalo.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
                return Result.success() // Don't retry when offline — WorkManager will run again on schedule
            }
            
            syncManager.syncFoodLogs()
            syncManager.syncMedicines()
            
            Log.d(TAG, "Background sync completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Background sync failed: ${e.message}", e)
            // Only retry on transient network errors
            when (e) {
                is ConnectException, is SocketTimeoutException, is UnknownHostException ->
                    Result.retry()
                else -> Result.success() // Don't retry permanent failures
            }
        }
    }
}
