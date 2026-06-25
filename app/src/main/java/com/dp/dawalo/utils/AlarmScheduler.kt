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
    
    /**
     * Generate unique request code per medicine+time slot.
     */
    fun getRequestCode(medicineId: Long, timeIndex: Int): Int {
        return (medicineId * 10 + timeIndex).toInt()
    }
    
    fun scheduleMedicineAlarm(
        context: Context,
        medicineId: Long,
        medicineName: String,
        timeInMillis: Long,
        timeIndex: Int = 0
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = getRequestCode(medicineId, timeIndex)
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("scheduled_time", timeInMillis)
            putExtra("time_index", timeIndex)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                    )
                    Log.d(TAG, "EXACT alarm set for $medicineName at ${dateFormat.format(Date(timeInMillis))} [rc=$requestCode]")
                } else {
                    // Fallback: use inexact alarm (may be delayed by up to 10 min)
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                    )
                    Log.w(TAG, "INEXACT alarm set for $medicineName (exact alarm permission not granted) [rc=$requestCode]")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                )
                Log.d(TAG, "EXACT alarm set for $medicineName at ${dateFormat.format(Date(timeInMillis))} [rc=$requestCode]")
            }
        } catch (e: SecurityException) {
            // Fallback for any security exception
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                )
                Log.w(TAG, "INEXACT alarm set (SecurityException fallback) for $medicineName [rc=$requestCode]")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule any alarm: ${e2.message}", e2)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}", e)
        }
    }
    
    fun cancelMedicineAlarm(context: Context, medicineId: Long, timeIndex: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = getRequestCode(medicineId, timeIndex)
        val intent = Intent(context, MedicineAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Alarm cancelled for medicine ID: $medicineId, timeIndex: $timeIndex")
    }
    
    fun cancelAllAlarmsForMedicine(context: Context, medicineId: Long) {
        for (i in 0 until 10) {
            cancelMedicineAlarm(context, medicineId, i)
        }
    }
}
