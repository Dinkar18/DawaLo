package com.dp.dawalo.ui.alarm

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.data.local.AppDatabase
import com.dp.dawalo.data.local.entity.MedicineLog
import com.dp.dawalo.data.local.entity.MedicineStatus
import com.dp.dawalo.databinding.ActivityMedicineAlarmBinding
import com.dp.dawalo.service.VoiceAlertService
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MedicineAlarmActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMedicineAlarmBinding
    private var medicineId: Long = -1L
    private var medicineName: String = ""
    private var dosage: String = ""
    private var scheduledTime: Long = 0L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show on lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        binding = ActivityMedicineAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Prevent back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* blocked */ }
        })
        
        medicineId = intent.getLongExtra("medicine_id", -1L)
        medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        dosage = intent.getStringExtra("dosage") ?: ""
        scheduledTime = intent.getLongExtra("scheduled_time", System.currentTimeMillis())
        
        setupUI()
        setupButtons()
    }
    
    private fun setupUI() {
        binding.tvMedicineName.text = medicineName
        binding.tvDosage.text = dosage
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.tvTime.text = "Scheduled: ${timeFormat.format(Date(scheduledTime))}"
    }
    
    private fun setupButtons() {
        binding.btnTaken.setOnClickListener {
            logMedicine(MedicineStatus.TAKEN)
            dismissAll()
        }
        
        binding.btnSnooze.setOnClickListener {
            logMedicine(MedicineStatus.SNOOZED)
            // Schedule snooze alarm
            val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000)
            com.dp.dawalo.utils.AlarmScheduler.scheduleMedicineAlarm(
                this, medicineId, medicineName, snoozeTime
            )
            dismissAll()
        }
        
        binding.btnSkip.setOnClickListener {
            logMedicine(MedicineStatus.SKIPPED)
            dismissAll()
        }
    }
    
    private fun logMedicine(status: MedicineStatus) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                val prefs = PreferenceManager(applicationContext)
                val log = MedicineLog(
                    userId = prefs.userId,
                    medicineId = medicineId,
                    scheduledTime = scheduledTime,
                    actualTime = if (status == MedicineStatus.TAKEN) System.currentTimeMillis() else null,
                    status = status,
                    notes = null
                )
                database.medicineLogDao().insert(log)
            } catch (e: Exception) {
                android.util.Log.e("MedicineAlarmActivity", "Error logging: ${e.message}")
            }
        }
    }
    
    /**
     * Stop voice alert service + dismiss notification + finish activity
     */
    private fun dismissAll() {
        // 1. Stop voice alert service
        try {
            val serviceIntent = Intent(this, VoiceAlertService::class.java).apply {
                action = VoiceAlertService.ACTION_STOP
            }
            startService(serviceIntent)
        } catch (e: Exception) {
            android.util.Log.e("MedicineAlarmActivity", "Error stopping service: ${e.message}")
        }
        
        // 2. Cancel the alarm notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(medicineId.toInt())
        
        // 3. Close activity
        finish()
    }
}
