package com.dp.dawalo.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.dp.dawalo.receiver.MedicineAlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

object AlarmScheduler {
    
    private const val TAG = "AlarmScheduler"
    
    fun scheduleMedicineAlarm(
        context: Context,
        medicineId: Long,
        medicineName: String,
        timeInMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    Log.d(TAG, "Alarm scheduled for $medicineName at ${dateFormat.format(Date(timeInMillis))}")
                } else {
                    Log.e(TAG, "Cannot schedule exact alarms - permission not granted")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                Log.d(TAG, "Alarm scheduled for $medicineName at ${dateFormat.format(Date(timeInMillis))}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}", e)
        }
    }
    
    fun cancelMedicineAlarm(context: Context, medicineId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Alarm cancelled for medicine ID: $medicineId")
    }
}
