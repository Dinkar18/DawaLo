package com.dp.dawalo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.dp.dawalo.MainActivity
import com.dp.dawalo.R
import java.util.*

class VoiceAlertService : Service(), TextToSpeech.OnInitListener {
    
    private var tts: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var medicineName: String = ""
    private var languageCode: String = "en"
    private var isRunning = false
    
    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                speakAlert()
                handler.postDelayed(this, REPEAT_INTERVAL)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("VoiceAlertService", "Service created")
        tts = TextToSpeech(this, this)
        startAlarmSound()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("VoiceAlertService", "Service started")
        
        if (intent?.action == ACTION_STOP) {
            android.util.Log.d("VoiceAlertService", "Stop action received")
            stopAlert()
            return START_NOT_STICKY
        }
        
        medicineName = intent?.getStringExtra("medicine_name") ?: "Medicine"
        languageCode = intent?.getStringExtra("language_code") ?: "en"
        
        android.util.Log.d("VoiceAlertService", "Starting alert for: $medicineName")
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        isRunning = true
        handler.post(repeatRunnable)
        
        return START_STICKY
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = when (languageCode) {
                "hi" -> Locale("hi", "IN")
                "bn" -> Locale("bn", "IN")
                "ta" -> Locale("ta", "IN")
                else -> Locale.ENGLISH
            }
            tts?.language = locale
        }
    }
    
    private fun startAlarmSound() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun speakAlert() {
        // Get message in selected language from resources
        val reminderText = getString(R.string.medicine_reminder)
        
        // For Hindi, speak in pure Hindi
        val message = if (languageCode == "hi") {
            "$reminderText: $medicineName"
        } else {
            "$reminderText: $medicineName"
        }
        
        android.util.Log.d("VoiceAlertService", "Speaking in language: $languageCode - Message: $message")
        
        // Set language before speaking
        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale("en", "IN")
        }
        
        tts?.language = locale
        
        // Wait for language to be set, then speak
        handler.postDelayed({
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }, 500)
    }
    
    private fun stopAlert() {
        isRunning = false
        handler.removeCallbacks(repeatRunnable)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        tts?.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        
        val stopIntent = Intent(this, VoiceAlertService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Medicine Alert")
            .setContentText("$medicineName - Tap STOP to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "STOP", stopPendingIntent)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlert()
        tts?.shutdown()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        const val ACTION_STOP = "com.dp.dawalo.ACTION_STOP_ALERT"
        private const val CHANNEL_ID = "voice_alert_channel"
        private const val NOTIFICATION_ID = 9999
        private const val REPEAT_INTERVAL = 10000L // 10 seconds
    }
}
