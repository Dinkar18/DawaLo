package com.dp.dawalo.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class VoiceInputHelper(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    
    interface VoiceInputListener {
        fun onVoiceResult(text: String)
        fun onVoiceError(error: String)
    }
    
    init {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        tts = TextToSpeech(context) { status ->
            isTtsReady = status == TextToSpeech.SUCCESS
            if (isTtsReady) {
                tts?.language = Locale("en", "IN")
            }
        }
    }
    
    fun startListening(language: String = "en-IN", listener: VoiceInputListener) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceInputHelper", "Ready for speech")
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("VoiceInputHelper", "End of speech")
            }
            
            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Please try again."
                    else -> "Unknown error"
                }
                listener.onVoiceError(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("VoiceInputHelper", "Results: $matches")
                matches?.firstOrNull()?.let { 
                    listener.onVoiceResult(it)
                } ?: listener.onVoiceError("No results")
            }
            
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            listener.onVoiceError("Could not start speech recognition: ${e.message}")
        }
    }
    
    /**
     * Speak text with optional completion callback.
     */
    fun speak(text: String, language: String = "en", onDone: (() -> Unit)? = null) {
        if (!isTtsReady) {
            // If TTS not ready, invoke callback after a delay as fallback
            onDone?.let {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ it() }, 1500)
            }
            return
        }
        
        val locale = when (language) {
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale("en", "IN")
        }
        tts?.language = locale
        
        if (onDone != null) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    // Small delay after TTS finishes to avoid mic picking up speaker
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ onDone() }, 500)
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ onDone() }, 500)
                }
            })
        }
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voice_input_${System.currentTimeMillis()}")
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
    }
    
    fun shutdown() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

data class MedicineInput(
    val name: String,
    val dosage: String,
    val time: String
)
