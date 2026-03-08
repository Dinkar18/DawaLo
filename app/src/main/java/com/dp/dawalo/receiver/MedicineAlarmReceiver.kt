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
import androidx.core.app.NotificationCompat
import com.dp.dawalo.MainActivity
import com.dp.dawalo.R
import com.dp.dawalo.service.VoiceAlertService
import com.dp.dawalo.utils.PreferenceManager

class MedicineAlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getLongExtra("medicine_id", -1L)
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val scheduledTime = intent.getLongExtra("scheduled_time", System.currentTimeMillis())
        
        android.util.Log.d("MedicineAlarmReceiver", "Alarm received for: $medicineName (ID: $medicineId)")
        
        // Launch full-screen alarm activity
        val alarmIntent = Intent(context, com.dp.dawalo.ui.alarm.MedicineAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("dosage", dosage)
            putExtra("scheduled_time", scheduledTime)
        }
        context.startActivity(alarmIntent)
        
        // Get language preference
        val prefs = PreferenceManager(context)
        val languageCode = prefs.languageCode
        
        // Start continuous voice alert service
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
            android.util.Log.d("MedicineAlarmReceiver", "Voice alert service started")
        } catch (e: Exception) {
            android.util.Log.e("MedicineAlarmReceiver", "Error starting service: ${e.message}", e)
        }
        
        // Also show notification as backup
        showNotification(context, medicineId, medicineName)
        
        // Reschedule for next day (recurring alarm)
        val nextDayMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
        com.dp.dawalo.utils.AlarmScheduler.scheduleMedicineAlarm(context, medicineId, medicineName, nextDayMillis)
    }
    
    private fun showNotification(context: Context, medicineId: Long, medicineName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medicine reminders"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            medicineId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Stop action
        val stopIntent = Intent(context, VoiceAlertService::class.java).apply {
            action = VoiceAlertService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            (medicineId + 1000).toInt(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Mark as Taken action
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
            .addAction(R.drawable.ic_launcher_foreground, "Taken", takenPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Skip", skipPendingIntent)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "STOP ALERT", stopPendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()
        
        notificationManager.notify(medicineId.toInt(), notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "medicine_reminder_channel"
    }
}
