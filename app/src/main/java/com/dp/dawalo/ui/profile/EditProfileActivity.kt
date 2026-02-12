package com.dp.dawalo.ui.profile

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.R
import com.dp.dawalo.databinding.ActivityEditProfileBinding
import com.dp.dawalo.data.local.entity.User
import com.dp.dawalo.data.local.entity.Goal
import com.dp.dawalo.data.local.entity.ActivityLevel
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.utils.ProteinCalculator
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var prefs: PreferenceManager
    private var currentUser: User? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = PreferenceManager(this)
        
        setupToolbar()
        setupSpinners()
        loadUserData()
        
        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun setupSpinners() {
        val goals = resources.getStringArray(R.array.goals)
        binding.spinnerGoal.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goals)
        
        val activities = resources.getStringArray(R.array.activity_levels)
        binding.spinnerActivity.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activities)
    }
    
    private fun loadUserData() {
        lifecycleScope.launch {
            val app = application as MedNutriTrackApp
            currentUser = app.database.userDao().getUserById(prefs.userId)
            
            currentUser?.let {
                binding.etWeight.setText(it.weight.toString())
                binding.etHeight.setText(it.height.toString())
                binding.spinnerGoal.setSelection(it.goal.ordinal)
                binding.spinnerActivity.setSelection(it.activityLevel.ordinal)
            }
        }
    }
    
    private fun saveProfile() {
        val weight = binding.etWeight.text.toString().toFloatOrNull()
        val height = binding.etHeight.text.toString().toFloatOrNull()
        
        if (weight == null || height == null) {
            Toast.makeText(this, "Enter valid values", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            currentUser?.let { user ->
                val goal = Goal.values()[binding.spinnerGoal.selectedItemPosition]
                val activityLevel = ActivityLevel.values()[binding.spinnerActivity.selectedItemPosition]
                
                // Recalculate targets
                val proteinTarget = ProteinCalculator.calculateDailyProtein(weight, goal)
                val calorieTarget = ProteinCalculator.calculateDailyCalories(
                    weight, height, user.age, user.gender, activityLevel, goal
                )
                
                // Create updated user
                val updatedUser = user.copy(
                    weight = weight,
                    height = height,
                    goal = goal,
                    activityLevel = activityLevel,
                    dailyProteinTarget = proteinTarget,
                    dailyCalorieTarget = calorieTarget
                )
                
                val app = application as MedNutriTrackApp
                app.database.userDao().update(updatedUser)
                
                Toast.makeText(this@EditProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
