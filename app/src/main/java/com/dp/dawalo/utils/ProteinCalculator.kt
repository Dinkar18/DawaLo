package com.dp.dawalo.utils

import com.dp.dawalo.data.local.entity.*

object ProteinCalculator {
    
    // Protein calculation based on goal
    fun calculateDailyProtein(weight: Float, goal: Goal): Float {
        return when (goal) {
            Goal.NORMAL -> weight * 0.8f
            Goal.MUSCLE_GAIN -> weight * 1.8f // Increased for muscle building
            Goal.WEIGHT_LOSS -> weight * 1.2f // Higher protein for weight loss
            Goal.WEIGHT_GAIN -> weight * 1.0f
        }
    }
    
    // Calorie calculation using Mifflin-St Jeor Equation
    fun calculateDailyCalories(
        weight: Float,
        height: Float,
        age: Int,
        gender: Gender,
        activityLevel: ActivityLevel,
        goal: Goal
    ): Float {
        // BMR calculation
        val bmr = when (gender) {
            Gender.MALE -> (10 * weight) + (6.25f * height) - (5 * age) + 5
            Gender.FEMALE -> (10 * weight) + (6.25f * height) - (5 * age) - 161
            Gender.OTHER -> (10 * weight) + (6.25f * height) - (5 * age) - 78 // Average
        }
        
        // Activity multiplier
        val activityMultiplier = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2f
            ActivityLevel.LIGHTLY_ACTIVE -> 1.375f
            ActivityLevel.MODERATELY_ACTIVE -> 1.55f
            ActivityLevel.VERY_ACTIVE -> 1.725f
            ActivityLevel.EXTREMELY_ACTIVE -> 1.9f
        }
        
        val maintenanceCalories = bmr * activityMultiplier
        
        // Adjust for goal
        return when (goal) {
            Goal.WEIGHT_LOSS -> maintenanceCalories - 500 // 500 cal deficit
            Goal.WEIGHT_GAIN, Goal.MUSCLE_GAIN -> maintenanceCalories + 300 // 300 cal surplus
            Goal.NORMAL -> maintenanceCalories
        }
    }
    
    fun getProteinStatus(consumed: Float, target: Float): String {
        val percentage = (consumed / target) * 100
        return when {
            percentage < 70 -> "DEFICIENT"
            percentage < 90 -> "GOOD"
            else -> "EXCELLENT"
        }
    }
    
    // Smart suggestions based on diet type
    fun getSuggestions(remaining: Float, dietType: DietType): List<String> {
        return when (dietType) {
            DietType.NON_VEGETARIAN -> getNonVegSuggestions(remaining)
            DietType.VEGETARIAN -> getVegSuggestions(remaining)
            DietType.VEGAN -> getVeganSuggestions(remaining)
            DietType.EGGETARIAN -> getEggetarianSuggestions(remaining)
        }
    }
    
    private fun getNonVegSuggestions(remaining: Float): List<String> {
        return when {
            remaining > 30 -> listOf(
                "Add 150g chicken breast (40g protein)",
                "Add 2 eggs + 100g paneer (24g protein)",
                "Add 150g fish + 1 bowl dal (35g protein)"
            )
            remaining > 20 -> listOf(
                "Add 100g chicken (25g protein)",
                "Add 2 eggs + 1 bowl dal (19g protein)",
                "Add 100g fish (20g protein)"
            )
            remaining > 10 -> listOf(
                "Add 2 eggs (12g protein)",
                "Add 1 bowl dal + 1 glass milk (15g protein)",
                "Add 50g chicken (12g protein)"
            )
            else -> listOf("Great! You're on track!")
        }
    }
    
    private fun getVegSuggestions(remaining: Float): List<String> {
        return when {
            remaining > 30 -> listOf(
                "Add 200g paneer + 2 bowls dal (43g protein)",
                "Add 100g paneer + 1 bowl rajma + milk (35g protein)",
                "Add 100g soya chunks + 1 bowl dal (33g protein)"
            )
            remaining > 20 -> listOf(
                "Add 100g paneer + 1 bowl dal (25g protein)",
                "Add 2 bowls dal + 1 glass milk (22g protein)",
                "Add 50g soya chunks + 1 bowl chana (34g protein)"
            )
            remaining > 10 -> listOf(
                "Add 1 bowl dal + 1 glass milk (15g protein)",
                "Add 100g paneer (18g protein)",
                "Add 50g roasted chana + curd (12g protein)"
            )
            else -> listOf("Excellent! Target achieved!")
        }
    }
    
    private fun getVeganSuggestions(remaining: Float): List<String> {
        return when {
            remaining > 20 -> listOf(
                "Add 100g soya chunks + 2 bowls dal (40g protein)",
                "Add 100g tofu + 1 bowl rajma (28g protein)",
                "Add 100g peanuts + 1 bowl chana (39g protein)"
            )
            remaining > 10 -> listOf(
                "Add 50g soya chunks + 1 bowl dal (33g protein)",
                "Add 2 bowls dal (14g protein)",
                "Add 50g roasted chana + 50g peanuts (19g protein)"
            )
            else -> listOf("Perfect! Keep it up!")
        }
    }
    
    private fun getEggetarianSuggestions(remaining: Float): List<String> {
        return when {
            remaining > 20 -> listOf(
                "Add 3 eggs + 100g paneer (36g protein)",
                "Add 4 eggs + 1 bowl dal (31g protein)",
                "Add 2 eggs + 1 glass milk + paneer (32g protein)"
            )
            remaining > 10 -> listOf(
                "Add 2 eggs + 1 bowl dal (19g protein)",
                "Add 3 eggs (18g protein)",
                "Add 2 eggs + 1 glass milk (20g protein)"
            )
            else -> listOf("Amazing! Goal reached!")
        }
    }
    
    // BMI calculation
    fun calculateBMI(weight: Float, height: Float): Float {
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }
    
    fun getBMICategory(bmi: Float): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }
}
