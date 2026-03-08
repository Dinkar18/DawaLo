package com.dp.dawalo.ui.adherence

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.local.entity.MedicineStatus
import com.dp.dawalo.databinding.FragmentAdherenceBinding
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.launch

class AdherenceFragment : Fragment() {
    
    private var _binding: FragmentAdherenceBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferenceManager
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdherenceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager(requireContext())
        loadAdherenceData()
    }
    
    private fun loadAdherenceData() {
        lifecycleScope.launch {
            val app = requireActivity().application as MedNutriTrackApp
            val logs = app.database.medicineLogDao().getLogsByUserId(prefs.userId)
            
            val taken = logs.count { it.status == MedicineStatus.TAKEN }
            val missed = logs.count { it.status == MedicineStatus.MISSED }
            val skipped = logs.count { it.status == MedicineStatus.SKIPPED }
            val total = taken + missed + skipped
            val adherence = if (total > 0) (taken * 100f / total) else 0f
            
            binding.tvAdherencePercentage.text = "${adherence.toInt()}%"
            binding.tvTakenCount.text = "$taken"
            binding.tvMissedCount.text = "$missed"
            binding.tvSkippedCount.text = "$skipped"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
