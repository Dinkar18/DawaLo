package com.dp.dawalo.ui.medicine

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.data.repository.MedicineRepository
import com.dp.dawalo.databinding.FragmentMedicineBinding
import com.dp.dawalo.utils.AlarmScheduler
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.viewmodel.MedicineViewModel
import com.dp.dawalo.viewmodel.MedicineViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.util.*

class MedicineFragment : Fragment() {
    
    private var _binding: FragmentMedicineBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MedicineViewModel
    private lateinit var adapter: MedicineAdapter
    private lateinit var prefs: PreferenceManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            prefs = PreferenceManager(requireContext())
            
            if (!prefs.isLoggedIn()) {
                Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
                return
            }
            
            val app = requireActivity().application as MedNutriTrackApp
            val repository = MedicineRepository(app.database.medicineDao(), prefs)
            viewModel = ViewModelProvider(this, MedicineViewModelFactory(repository))[MedicineViewModel::class.java]
            
            setupRecyclerView()
            observeMedicines()
            
            binding.fabAdd.setOnClickListener {
                showAddMedicineDialog()
            }
            
            binding.fabVoiceAdd.setOnClickListener {
                val intent = android.content.Intent(requireContext(), com.dp.dawalo.ui.voice.VoiceInputActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("MedicineFragment", "Error in onViewCreated", e)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = MedicineAdapter { medicine ->
            // Cancel all alarms for this medicine before deleting
            AlarmScheduler.cancelAllAlarmsForMedicine(requireContext(), medicine.id)
            viewModel.deleteMedicine(medicine)
        }
        
        binding.rvMedicines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMedicines.adapter = adapter
    }
    
    private fun observeMedicines() {
        try {
            viewModel.getAllMedicines(prefs.userId).observe(viewLifecycleOwner) { medicines ->
                if (medicines != null) {
                    adapter.submitList(medicines)
                    binding.tvEmptyState.visibility = if (medicines.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading medicines: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showAddMedicineDialog() {
        val dialogView = layoutInflater.inflate(com.dp.dawalo.R.layout.dialog_add_medicine, null)
        val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.dp.dawalo.R.id.etMedicineName)
        val etDosage = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.dp.dawalo.R.id.etDosage)
        val etFrequency = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.dp.dawalo.R.id.etFrequency)
        val btnAddTime = dialogView.findViewById<android.widget.Button>(com.dp.dawalo.R.id.btnAddTime)
        val tvSelectedTimes = dialogView.findViewById<android.widget.TextView>(com.dp.dawalo.R.id.tvSelectedTimes)
        
        val selectedTimes = mutableListOf<String>()
        
        btnAddTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                selectedTimes.add(time)
                tvSelectedTimes.text = selectedTimes.joinToString(", ")
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Medicine")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val dosage = etDosage.text.toString().trim()
                val frequency = etFrequency.text.toString().trim()
                
                if (name.isNotEmpty() && dosage.isNotEmpty() && selectedTimes.isNotEmpty()) {
                    saveMedicine(name, dosage, frequency, selectedTimes)
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields and add at least one time", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun saveMedicine(name: String, dosage: String, frequency: String, times: List<String>) {
        val medicine = Medicine(
            userId = prefs.userId,
            name = name,
            dosage = dosage,
            frequency = frequency,
            times = Gson().toJson(times),
            startDate = System.currentTimeMillis(),
            endDate = null
        )
        
        viewModel.addMedicine(medicine) { medicineId ->
            scheduleAlarms(medicineId, name, times)
            Toast.makeText(requireContext(), "Medicine added successfully", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun scheduleAlarms(medicineId: Long, medicineName: String, times: List<String>) {
        times.forEachIndexed { index, time ->
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
                requireContext(),
                medicineId,
                medicineName,
                calendar.timeInMillis,
                index
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
