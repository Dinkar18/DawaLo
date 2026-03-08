package com.dp.dawalo.ui.voice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.data.local.AppDatabase
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.databinding.ActivityVoiceInputBinding
import com.dp.dawalo.utils.MedicineParser
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.utils.VoiceInputHelper
import kotlinx.coroutines.launch

class VoiceInputActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVoiceInputBinding
    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var prefs: PreferenceManager
    private var parsedMedicine: com.dp.dawalo.utils.MedicineInput? = null
    
    companion object {
        private const val REQUEST_RECORD_AUDIO = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = PreferenceManager(this)
        voiceHelper = VoiceInputHelper(this)
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        binding.btnStartVoice.setOnClickListener {
            if (checkPermissions()) {
                startVoiceRecognition()
            }
        }
        
        binding.btnConfirm.setOnClickListener {
            confirmAndSave()
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun checkPermissions(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
            false
        } else {
            true
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int, 
        permissions: Array<out String>, 
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.isNotEmpty() 
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecognition()
        } else {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startVoiceRecognition() {
        binding.btnStartVoice.isEnabled = false
        binding.btnStartVoice.text = "🎤 Listening..."
        
        voiceHelper.speak("Please say medicine name, dosage, and time")
        
        android.os.Handler(mainLooper).postDelayed({
            voiceHelper.startListening(prefs.languageCode + "-IN", 
                object : VoiceInputHelper.VoiceInputListener {
                    override fun onVoiceResult(text: String) {
                        handleVoiceResult(text)
                    }
                    
                    override fun onVoiceError(error: String) {
                        binding.btnStartVoice.isEnabled = true
                        binding.btnStartVoice.text = "🎤 Start Speaking"
                        Toast.makeText(this@VoiceInputActivity, 
                            "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                })
        }, 2000)
    }
    
    private fun handleVoiceResult(text: String) {
        binding.btnStartVoice.isEnabled = true
        binding.btnStartVoice.text = "🎤 Try Again"
        
        parsedMedicine = MedicineParser.parseVoiceInput(text)
        
        if (parsedMedicine != null) {
            binding.cardResult.visibility = android.view.View.VISIBLE
            binding.tvMedicineName.text = "Medicine: ${parsedMedicine!!.name}"
            binding.tvDosage.text = "Dosage: ${parsedMedicine!!.dosage}"
            binding.tvTime.text = "Time: ${parsedMedicine!!.time}"
            
            voiceHelper.speak("${parsedMedicine!!.name}, ${parsedMedicine!!.dosage}, ${parsedMedicine!!.time}. Is this correct?")
        } else {
            Toast.makeText(this, "Could not understand. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun confirmAndSave() {
        if (parsedMedicine == null) {
            Toast.makeText(this, "Please add medicine by voice first", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            val medicine = Medicine(
                userId = prefs.userId,
                name = parsedMedicine!!.name,
                dosage = parsedMedicine!!.dosage,
                frequency = "Daily",
                times = com.google.gson.Gson().toJson(listOf(parsedMedicine!!.time)),
                startDate = System.currentTimeMillis(),
                endDate = null,
                isActive = true
            )
            
            database.medicineDao().insert(medicine)
            
            Toast.makeText(this@VoiceInputActivity, 
                "Medicine added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.shutdown()
    }
}
