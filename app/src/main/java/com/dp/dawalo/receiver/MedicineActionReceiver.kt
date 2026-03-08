package com.dp.dawalo.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.local.entity.MedicineLog
import com.dp.dawalo.data.local.entity.MedicineStatus
import com.dp.dawalo.service.VoiceAlertService
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicineActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getLongExtra("medicine_id", -1L)
        val action = intent.action
        
        when (action) {
            "ACTION_TAKEN" -> logMedicine(context, medicineId, MedicineStatus.TAKEN)
            "ACTION_SKIP" -> logMedicine(context, medicineId, MedicineStatus.SKIPPED)
        }
        
        // Stop voice alert
        val stopIntent = Intent(context, VoiceAlertService::class.java).apply {
            this.action = VoiceAlertService.ACTION_STOP
        }
        context.startService(stopIntent)
        
        // Cancel notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(medicineId.toInt())
    }
    
    private fun logMedicine(context: Context, medicineId: Long, status: MedicineStatus) {
        CoroutineScope(Dispatchers.IO).launch {
            val app = context.applicationContext as MedNutriTrackApp
            val prefs = PreferenceManager(context)
            val now = System.currentTimeMillis()
            val log = MedicineLog(
                userId = prefs.userId,
                medicineId = medicineId,
                scheduledTime = now,
                actualTime = if (status == MedicineStatus.TAKEN) now else null,
                status = status
            )
            app.database.medicineLogDao().insert(log)
        }
        
        val message = if (status == MedicineStatus.TAKEN) "Marked as taken" else "Skipped"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
