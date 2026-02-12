package com.dp.dawalo.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.databinding.FragmentProfileBinding
import com.dp.dawalo.ui.auth.LoginActivity
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.utils.ProteinCalculator
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferenceManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefs = PreferenceManager(requireContext())
        loadUserProfile()
        
        binding.btnSettings.setOnClickListener {
            val intent = Intent(requireContext(), com.dp.dawalo.ui.settings.SettingsActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun loadUserProfile() {
        lifecycleScope.launch {
            val app = requireActivity().application as MedNutriTrackApp
            val user = app.database.userDao().getUserById(prefs.userId)
            
            user?.let {
                binding.tvName.text = it.name
                binding.tvPhone.text = "📱 ${it.email}"
                binding.tvAge.text = "${it.age}"
                binding.tvWeight.text = "${it.weight.toInt()}"
                binding.tvHeight.text = "${it.height.toInt()}"
                binding.tvGender.text = it.gender.name.capitalize()
                binding.tvDietType.text = it.dietType.name.replace("_", " ").capitalize()
                binding.tvActivityLevel.text = it.activityLevel.name.replace("_", " ").capitalize()
                binding.tvGoal.text = it.goal.name.replace("_", " ").capitalize()
                binding.tvLanguage.text = it.languageCode.uppercase()
                
                val bmi = ProteinCalculator.calculateBMI(it.weight, it.height)
                binding.tvBmi.text = String.format("%.1f (%s)", bmi, ProteinCalculator.getBMICategory(bmi))
                
                binding.tvProteinTarget.text = "${it.dailyProteinTarget.toInt()}g/day"
                binding.tvCalorieTarget.text = "${it.dailyCalorieTarget.toInt()} kcal/day"
            }
        }
    }
    
    private fun logout() {
        prefs.logout()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
