package com.dp.dawalo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MainActivity
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.R
import com.dp.dawalo.data.local.entity.ActivityLevel
import com.dp.dawalo.data.local.entity.DietType
import com.dp.dawalo.data.local.entity.Gender
import com.dp.dawalo.data.local.entity.Goal
import com.dp.dawalo.data.local.entity.User
import com.dp.dawalo.databinding.ActivityRegisterBinding
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.utils.ProteinCalculator
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var prefs: PreferenceManager
    private var currentStep = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = PreferenceManager(this)
        
        setupSpinners()
        showStep(1)
        
        binding.btnNext.setOnClickListener {
            when (currentStep) {
                1 -> if (validateStep1()) showStep(2)
                2 -> if (validateStep2()) showStep(3)
                3 -> if (validateStep3()) completeRegistration()
            }
        }
        
        binding.btnBack.setOnClickListener {
            if (currentStep > 1) showStep(currentStep - 1)
        }
    }
    
    private fun setupSpinners() {
        ArrayAdapter.createFromResource(this, R.array.genders, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerGender.adapter = adapter
            }
        
        ArrayAdapter.createFromResource(this, R.array.diet_types, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDietType.adapter = adapter
            }
        
        ArrayAdapter.createFromResource(this, R.array.activity_levels, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerActivityLevel.adapter = adapter
            }
        
        ArrayAdapter.createFromResource(this, R.array.goals, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerGoal.adapter = adapter
            }
        
        ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerLanguage.adapter = adapter
                binding.spinnerLanguage.setSelection(0)
            }
    }
    
    private fun showStep(step: Int) {
        currentStep = step
        
        binding.step1Layout.visibility = if (step == 1) android.view.View.VISIBLE else android.view.View.GONE
        binding.step2Layout.visibility = if (step == 2) android.view.View.VISIBLE else android.view.View.GONE
        binding.step3Layout.visibility = if (step == 3) android.view.View.VISIBLE else android.view.View.GONE
        
        binding.tvStepIndicator.text = "Step $step of 3"
        binding.btnBack.visibility = if (step == 1) android.view.View.GONE else android.view.View.VISIBLE
        binding.btnNext.text = if (step == 3) "Complete" else "Next"
    }
    
    private fun validateStep1(): Boolean {
        val name = binding.etName.text.toString()
        val phone = binding.etPhone.text.toString()
        val password = binding.etPassword.text.toString()
        val age = binding.etAge.text.toString()
        
        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun validateStep2(): Boolean {
        val weight = binding.etWeight.text.toString()
        val height = binding.etHeight.text.toString()
        
        if (weight.isEmpty() || height.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun validateStep3(): Boolean = true
    
    private fun completeRegistration() {
        binding.btnNext.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val app = application as MedNutriTrackApp
                val userDao = app.database.userDao()
                
                val weight = binding.etWeight.text.toString().toFloat()
                val height = binding.etHeight.text.toString().toFloat()
                val age = binding.etAge.text.toString().toInt()
                val goalString = binding.spinnerGoal.selectedItem.toString()
                val genderString = binding.spinnerGender.selectedItem.toString()
                val activityString = binding.spinnerActivityLevel.selectedItem.toString()
                
                // Convert strings to enums
                val goal = when (goalString) {
                    "Weight Loss" -> Goal.WEIGHT_LOSS
                    "Muscle Gain" -> Goal.MUSCLE_GAIN
                    "Weight Gain" -> Goal.WEIGHT_GAIN
                    else -> Goal.NORMAL
                }
                
                val gender = when (genderString) {
                    "Male" -> Gender.MALE
                    "Female" -> Gender.FEMALE
                    else -> Gender.OTHER
                }
                
                val activityLevel = when (activityString) {
                    "Sedentary" -> ActivityLevel.SEDENTARY
                    "Lightly Active" -> ActivityLevel.LIGHTLY_ACTIVE
                    "Very Active" -> ActivityLevel.VERY_ACTIVE
                    "Extremely Active" -> ActivityLevel.EXTREMELY_ACTIVE
                    else -> ActivityLevel.MODERATELY_ACTIVE
                }
                
                val dietType = when (binding.spinnerDietType.selectedItem.toString()) {
                    "Vegetarian" -> DietType.VEGETARIAN
                    "Vegan" -> DietType.VEGAN
                    "Eggetarian" -> DietType.EGGETARIAN
                    else -> DietType.NON_VEGETARIAN
                }
                
                val proteinTarget = ProteinCalculator.calculateDailyProtein(weight, goal)
                val calorieTarget = ProteinCalculator.calculateDailyCalories(
                    weight, height, age, gender, activityLevel, goal
                )
                
                val user = User(
                    name = binding.etName.text.toString(),
                    email = binding.etPhone.text.toString() + "@dawalo.com",
                    weight = weight,
                    height = height,
                    age = age,
                    gender = gender,
                    goal = goal,
                    dietType = dietType,
                    activityLevel = activityLevel,
                    dailyProteinTarget = proteinTarget,
                    dailyCalorieTarget = calorieTarget,
                    languageCode = when (binding.spinnerLanguage.selectedItem.toString()) {
                        "Hindi" -> "hi"
                        "Bengali" -> "bn"
                        "Tamil" -> "ta"
                        else -> "en"
                    }
                )
                
                val userId = userDao.insert(user)
                prefs.userId = userId
                prefs.token = "offline_mode"
                prefs.languageCode = user.languageCode
                
                Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnNext.isEnabled = true
            }
        }
    }
}
