package com.dp.dawalo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dp.dawalo.data.repository.MedicineRepository
import com.dp.dawalo.databinding.ActivityMedicineListBinding
import com.dp.dawalo.ui.medicine.MedicineAdapter
import com.dp.dawalo.utils.AlarmScheduler
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.viewmodel.MedicineViewModel
import com.dp.dawalo.viewmodel.MedicineViewModelFactory

class MedicineListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMedicineListBinding
    private lateinit var viewModel: MedicineViewModel
    private lateinit var adapter: MedicineAdapter
    private lateinit var prefs: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = PreferenceManager(this)
        val app = application as MedNutriTrackApp
        val repository = MedicineRepository(app.database.medicineDao())
        viewModel = ViewModelProvider(this, MedicineViewModelFactory(repository))[MedicineViewModel::class.java]
        
        setupRecyclerView()
        observeMedicines()
        
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddMedicineActivity::class.java))
        }
    }
    
    private fun setupRecyclerView() {
        adapter = MedicineAdapter { medicine ->
            AlarmScheduler.cancelMedicineAlarm(this, medicine.id)
            viewModel.deleteMedicine(medicine)
        }
        
        binding.rvMedicines.layoutManager = LinearLayoutManager(this)
        binding.rvMedicines.adapter = adapter
    }
    
    private fun observeMedicines() {
        viewModel.getAllMedicines(prefs.userId).observe(this) { medicines ->
            adapter.submitList(medicines)
        }
    }
}