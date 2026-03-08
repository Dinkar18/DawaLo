package com.dp.dawalo.ui.alarm

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
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
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        
        binding = ActivityMedicineAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get data from intent
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
            stopAlarmAndFinish()
        }
        
        binding.btnSnooze.setOnClickListener {
            logMedicine(MedicineStatus.SNOOZED)
            snoozeAlarm()
            finish()
        }
        
        binding.btnSkip.setOnClickListener {
            logMedicine(MedicineStatus.SKIPPED)
            stopAlarmAndFinish()
        }
    }
    
    private fun logMedicine(status: MedicineStatus) {
        lifecycleScope.launch {
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
        }
    }
    
    private fun snoozeAlarm() {
        // Reschedule alarm for 10 minutes later
        val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000)
        com.dp.dawalo.utils.AlarmScheduler.scheduleMedicineAlarm(
            this,
            medicineId,
            medicineName,
            snoozeTime
        )
    }
    
    private fun stopAlarmAndFinish() {
        // Stop voice alert service with correct action
        val serviceIntent = Intent(this, VoiceAlertService::class.java).apply {
            action = "com.dp.dawalo.ACTION_STOP_ALERT"
        }
        startService(serviceIntent)
        
        // Also stop the service completely
        stopService(Intent(this, com.dp.dawalo.service.VoiceAlertService::class.java))
        
        finish()
    }
    
    override fun onBackPressed() {
        // Prevent back button - force user to take action
    }
}
