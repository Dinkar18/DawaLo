package com.dp.dawalo.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dp.dawalo.MainActivity
import com.dp.dawalo.R
import com.dp.dawalo.service.VoiceAlertService
import com.dp.dawalo.utils.AlarmScheduler
import com.dp.dawalo.utils.PreferenceManager
import java.util.*

class MedicineAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "MedicineAlarmReceiver"
        private const val CHANNEL_ID = "medicine_reminder_channel"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getLongExtra("medicine_id", -1L)
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val scheduledTime = intent.getLongExtra("scheduled_time", System.currentTimeMillis())
        val timeIndex = intent.getIntExtra("time_index", 0)
        
        Log.d(TAG, "Alarm received for: $medicineName (ID: $medicineId, timeIndex: $timeIndex)")
        
        // Get language preference
        val prefs = PreferenceManager(context)
        val languageCode = prefs.languageCode
        
        // Start voice alert service (alarm sound + TTS)
        val serviceIntent = Intent(context, VoiceAlertService::class.java).apply {
            putExtra("medicine_name", medicineName)
            putExtra("language_code", languageCode)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice service: ${e.message}", e)
        }
        
        // Show full-screen notification (this is the correct way to show alarm UI on Android 10+)
        showFullScreenNotification(context, medicineId, medicineName, dosage, scheduledTime)
        
        // Reschedule for next day using ORIGINAL scheduled time + 24h (prevents drift)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = scheduledTime
            add(Calendar.DAY_OF_MONTH, 1)
        }
        AlarmScheduler.scheduleMedicineAlarm(
            context, medicineId, medicineName,
            calendar.timeInMillis, timeIndex
        )
    }
    
    private fun showFullScreenNotification(
        context: Context,
        medicineId: Long,
        medicineName: String,
        dosage: String,
        scheduledTime: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create high-importance channel for alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medicine reminders"
                enableVibration(true)
                // No sound on notification channel — VoiceAlertService handles audio
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Full-screen intent → launches MedicineAlarmActivity
        val fullScreenIntent = Intent(context, com.dp.dawalo.ui.alarm.MedicineAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("dosage", dosage)
            putExtra("scheduled_time", scheduledTime)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            medicineId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Taken action
        val takenIntent = Intent(context, MedicineActionReceiver::class.java).apply {
            action = "ACTION_TAKEN"
            putExtra("medicine_id", medicineId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicineId + 2000).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Skip action
        val skipIntent = Intent(context, MedicineActionReceiver::class.java).apply {
            action = "ACTION_SKIP"
            putExtra("medicine_id", medicineId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicineId + 3000).toInt(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.medicine_reminder))
            .setContentText(medicineName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            // Full-screen intent — shows alarm activity on lock screen & when app is in background
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_launcher_foreground, "✅ Taken", takenPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "❌ Skip", skipPendingIntent)
            .setContentIntent(fullScreenPendingIntent)
            .build()
        
        notificationManager.notify(medicineId.toInt(), notification)
    }
}
