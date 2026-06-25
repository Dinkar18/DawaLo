package com.dp.dawalo

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.data.repository.MedicineRepository
import com.dp.dawalo.databinding.ActivityAddMedicineBinding
import com.dp.dawalo.utils.AlarmScheduler
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.viewmodel.MedicineViewModel
import com.dp.dawalo.viewmodel.MedicineViewModelFactory
import com.google.gson.Gson
import java.util.*

class AddMedicineActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddMedicineBinding
    private lateinit var viewModel: MedicineViewModel
    private lateinit var prefs: PreferenceManager
    private val selectedTimes = mutableListOf<String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = PreferenceManager(this)
        val app = application as MedNutriTrackApp
        val repository = MedicineRepository(app.database.medicineDao(), prefs)
        viewModel = ViewModelProvider(this, MedicineViewModelFactory(repository))[MedicineViewModel::class.java]
        
        setupListeners()
    }
    
    private fun setupListeners() {
        binding.btnAddTime.setOnClickListener {
            showTimePicker()
        }
        
        binding.btnSave.setOnClickListener {
            if (checkAlarmPermission()) {
                saveMedicine()
            }
        }
    }
    
    private fun checkAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Please grant exact alarm permission to schedule medicine reminders.")
                    .setPositiveButton("Grant") { _, _ ->
                        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return false
            }
        }
        return true
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val time = String.format("%02d:%02d", hour, minute)
            selectedTimes.add(time)
            updateTimesDisplay()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }
    
    private fun updateTimesDisplay() {
        binding.tvSelectedTimes.text = selectedTimes.joinToString(", ")
    }
    
    private fun saveMedicine() {
        val name = binding.etMedicineName.text.toString().trim()
        val dosage = binding.etDosage.text.toString().trim()
        val frequency = binding.etFrequency.text.toString().trim()
        
        if (name.isEmpty() || dosage.isEmpty() || selectedTimes.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val medicine = Medicine(
            userId = prefs.userId,
            name = name,
            dosage = dosage,
            frequency = frequency,
            times = Gson().toJson(selectedTimes),
            startDate = System.currentTimeMillis(),
            endDate = null
        )
        
        viewModel.addMedicine(medicine) { medicineId ->
            scheduleAlarms(medicineId, name)
            Toast.makeText(this, "Medicine added successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun scheduleAlarms(medicineId: Long, medicineName: String) {
        selectedTimes.forEachIndexed { index, time ->
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            AlarmScheduler.scheduleMedicineAlarm(
                this,
                medicineId,
                medicineName,
                calendar.timeInMillis,
                index
            )
        }
    }
}
