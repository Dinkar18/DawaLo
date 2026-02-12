package com.dp.dawalo.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.databinding.ActivitySettingsBinding
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = PreferenceManager(this)
        setupToolbar()
        loadSettings()
        setupListeners()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun loadSettings() {
        lifecycleScope.launch {
            val app = application as MedNutriTrackApp
            val user = app.database.userDao().getUserById(prefs.userId)
            
            user?.let {
                binding.switchNotifications.isChecked = true
                binding.switchVoiceAlerts.isChecked = true
                binding.tvLanguage.text = it.languageCode.uppercase()
            }
        }
    }
    
    private fun setupListeners() {
        binding.cardEditProfile.setOnClickListener {
            val intent = Intent(this, com.dp.dawalo.ui.profile.EditProfileActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardChangeLanguage.setOnClickListener {
            // TODO: Show language picker
        }
        
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Update notification preference
        }
        
        binding.switchVoiceAlerts.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Update voice alert preference
        }
    }
}
