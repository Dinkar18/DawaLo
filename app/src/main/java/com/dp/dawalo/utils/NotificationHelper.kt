package com.dp.dawalo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

object NotificationHelper {
    
    fun sendSMS(context: Context, phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
            == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
