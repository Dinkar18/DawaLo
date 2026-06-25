package com.dp.dawalo.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TTSHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeak: Pair<String, String>? = null
    
    init {
        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            if (isInitialized) {
                Log.d("TTSHelper", "TTS initialized")
                // Speak any pending text
                pendingSpeak?.let { (text, lang) ->
                    speak(text, lang)
                    pendingSpeak = null
                }
            }
        }
    }
    
    fun speak(text: String, languageCode: String = "en") {
        if (!isInitialized) {
            pendingSpeak = text to languageCode
            return
        }
        
        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale("en", "IN")
        }
        
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.ENGLISH
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
    }
    
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
