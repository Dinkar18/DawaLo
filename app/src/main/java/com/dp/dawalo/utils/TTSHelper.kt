package com.dp.dawalo.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TTSHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
        }
    }
    
    fun speak(text: String, languageCode: String = "en") {
        if (!isInitialized) return
        
        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale.ENGLISH
        }
        
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
