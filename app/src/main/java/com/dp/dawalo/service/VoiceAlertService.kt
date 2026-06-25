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
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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
    private var isTtsReady = false
    private var pendingSpeak = false
    
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
        Log.d(TAG, "Service created")
        tts = TextToSpeech(this, this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Stop action received")
            stopAlert()
            return START_NOT_STICKY
        }
        
        medicineName = intent?.getStringExtra("medicine_name") ?: "Medicine"
        languageCode = intent?.getStringExtra("language_code") ?: "en"
        
        Log.d(TAG, "Starting alert for: $medicineName, lang: $languageCode")
        
        startForeground(NOTIFICATION_ID, createNotification())
        startAlarmSound()
        
        isRunning = true
        if (isTtsReady) {
            setTtsLanguage()
            handler.post(repeatRunnable)
        } else {
            pendingSpeak = true
        }
        
        return START_STICKY
    }
    
    override fun onInit(status: Int) {
        isTtsReady = status == TextToSpeech.SUCCESS
        if (isTtsReady) {
            Log.d(TAG, "TTS initialized successfully")
            setTtsLanguage()
            if (pendingSpeak && isRunning) {
                pendingSpeak = false
                handler.post(repeatRunnable)
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
        }
    }
    
    private fun setTtsLanguage() {
        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale("en", "IN")
        }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language $languageCode not supported, falling back to English")
            tts?.language = Locale.ENGLISH
        }
    }
    
    private fun startAlarmSound() {
        try {
            mediaPlayer?.release()
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
            Log.e(TAG, "Error starting alarm sound: ${e.message}")
        }
    }
    
    private fun speakAlert() {
        if (!isTtsReady) return
        
        val reminderText = getString(R.string.medicine_reminder)
        val message = "$reminderText: $medicineName"
        
        // Lower alarm volume briefly while speaking
        try { mediaPlayer?.setVolume(0.2f, 0.2f) } catch (_: Exception) {}
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                try { mediaPlayer?.setVolume(1.0f, 1.0f) } catch (_: Exception) {}
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                try { mediaPlayer?.setVolume(1.0f, 1.0f) } catch (_: Exception) {}
            }
        })
        
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "medicine_alert")
    }
    
    private fun stopAlert() {
        isRunning = false
        handler.removeCallbacks(repeatRunnable)
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
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
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
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
        isRunning = false
        handler.removeCallbacks(repeatRunnable)
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        private const val TAG = "VoiceAlertService"
        const val ACTION_STOP = "com.dp.dawalo.ACTION_STOP_ALERT"
        private const val CHANNEL_ID = "voice_alert_channel"
        private const val NOTIFICATION_ID = 9999
        private const val REPEAT_INTERVAL = 10000L
    }
}
