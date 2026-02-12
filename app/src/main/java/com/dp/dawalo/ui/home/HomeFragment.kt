package com.dp.dawalo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.databinding.FragmentHomeBinding
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.utils.ProteinCalculator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferenceManager
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager(requireContext())
        loadUserData()
    }
    
    private fun loadUserData() {
        lifecycleScope.launch {
            val app = requireActivity().application as MedNutriTrackApp
            val user = app.database.userDao().getUserById(prefs.userId)
            
            user?.let {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val greeting = when {
                    hour < 12 -> "Good Morning"
                    hour < 17 -> "Good Afternoon"
                    else -> "Good Evening"
                }
                binding.tvGreeting.text = "$greeting, ${it.name}!"
                
                val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
                binding.tvDate.text = dateFormat.format(Date())
                
                loadNutritionData(it)
                
                val bmi = ProteinCalculator.calculateBMI(it.weight, it.height)
                binding.tvBmi.text = String.format("%.1f", bmi)
                binding.tvBmiCategory.text = ProteinCalculator.getBMICategory(bmi)
            }
        }
    }
    
    private fun loadNutritionData(user: com.dp.dawalo.data.local.entity.User) {
        lifecycleScope.launch {
            val app = requireActivity().application as MedNutriTrackApp
            val startOfDay = getStartOfDay()
            val endOfDay = getEndOfDay()
            
            val logs = app.database.dailyFoodLogDao().getLogsBetween(prefs.userId, startOfDay, endOfDay)
            val protein = logs.sumOf { it.protein.toDouble() }.toFloat()
            val calories = logs.sumOf { it.calories.toDouble() }.toFloat()
            
            binding.tvProteinConsumed.text = "${protein.toInt()}g"
            binding.tvProteinTarget.text = "/ ${user.dailyProteinTarget.toInt()}g"
            binding.progressProtein.max = user.dailyProteinTarget.toInt()
            binding.progressProtein.progress = protein.toInt()
            
            binding.tvCalorieConsumed.text = "${calories.toInt()} kcal"
            binding.tvCalorieTarget.text = "/ ${user.dailyCalorieTarget.toInt()} kcal"
            binding.progressCalories.max = user.dailyCalorieTarget.toInt()
            binding.progressCalories.progress = calories.toInt()
        }
    }
    
    private fun getStartOfDay() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    private fun getEndOfDay() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
