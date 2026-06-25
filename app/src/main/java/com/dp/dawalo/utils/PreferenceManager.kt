package com.dp.dawalo.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("mednutritrack_prefs", Context.MODE_PRIVATE)
    
    var userId: Long
        get() = prefs.getLong("user_id", -1L)
        set(value) = prefs.edit().putLong("user_id", value).apply()
    
    var languageCode: String
        get() = prefs.getString("language_code", "en") ?: "en"
        set(value) = prefs.edit().putString("language_code", value).apply()
    
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("is_first_launch", true)
        set(value) = prefs.edit().putBoolean("is_first_launch", value).apply()
    
    var token: String?
        get() = prefs.getString("token", null)
        set(value) = prefs.edit().putString("token", value).apply()
    
    var isSimplifiedMode: Boolean
        get() = prefs.getBoolean("simplified_mode", false)
        set(value) = prefs.edit().putBoolean("simplified_mode", value).apply()
    
    var isVoiceGuidanceEnabled: Boolean
        get() = prefs.getBoolean("voice_guidance", true)
        set(value) = prefs.edit().putBoolean("voice_guidance", value).apply()
    
    fun isLoggedIn(): Boolean = token != null && userId != -1L
    
    fun logout() {
        // Preserve isFirstLaunch and language preference across logout
        val firstLaunch = isFirstLaunch
        val lang = languageCode
        prefs.edit().clear().apply()
        isFirstLaunch = firstLaunch
        languageCode = lang
    }
}
