package com.dp.dawalo.ui.water

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.databinding.FragmentWaterBinding
import com.dp.dawalo.databinding.DialogAddWaterBinding
import com.dp.dawalo.data.local.entity.WaterLog
import com.dp.dawalo.utils.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WaterFragment : Fragment() {
    
    private var _binding: FragmentWaterBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferenceManager
    private val dailyGoal = 2000 // ml
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWaterBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager(requireContext())
        
        loadTodayWater()
        
        binding.btn250ml.setOnClickListener { addWater(250) }
        binding.btn500ml.setOnClickListener { addWater(500) }
        binding.btn1000ml.setOnClickListener { addWater(1000) }
        binding.btnCustom.setOnClickListener { showCustomDialog() }
    }
    
    private fun loadTodayWater() {
        lifecycleScope.launch {
            val app = requireActivity().application as MedNutriTrackApp
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toLong()
            val total = app.database.waterLogDao().getTodayTotal(prefs.userId, today) ?: 0
            
            binding.tvWaterConsumed.text = "${total}ml"
            binding.tvWaterGoal.text = "/ ${dailyGoal}ml"
            binding.progressWater.max = dailyGoal
            binding.progressWater.progress = total
            
            val percentage = (total * 100) / dailyGoal
            binding.tvPercentage.text = "$percentage%"
        }
    }
    
    private fun addWater(amount: Int) {
        lifecycleScope.launch {
            val app = requireActivity().application as MedNutriTrackApp
            val now = System.currentTimeMillis()
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toLong()
            
            val waterLog = WaterLog(
                userId = prefs.userId,
                amount = amount,
                date = today,
                time = now
            )
            
            app.database.waterLogDao().insert(waterLog)
            loadTodayWater()
        }
    }
    
    private fun showCustomDialog() {
        val dialogBinding = DialogAddWaterBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Water")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val amount = dialogBinding.etAmount.text.toString().toIntOrNull() ?: 0
                if (amount > 0) addWater(amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
