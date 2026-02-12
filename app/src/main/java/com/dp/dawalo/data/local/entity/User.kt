package com.dp.dawalo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val age: Int = 25,
    val gender: Gender = Gender.OTHER,
    val weight: Float,
    val height: Float = 165f, // cm
    val goal: Goal,
    val dietType: DietType = DietType.VEGETARIAN,
    val dailyProteinTarget: Float,
    val dailyCalorieTarget: Float = 2000f,
    val languageCode: String = "hi", // Default to Hindi for India
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE
)

enum class Goal {
    NORMAL, MUSCLE_GAIN, WEIGHT_LOSS, WEIGHT_GAIN
}

enum class Gender {
    MALE, FEMALE, OTHER
}

enum class DietType {
    VEGETARIAN, NON_VEGETARIAN, VEGAN, EGGETARIAN
}

enum class ActivityLevel {
    SEDENTARY, 
    LIGHTLY_ACTIVE, 
    MODERATELY_ACTIVE, 
    VERY_ACTIVE, 
    EXTREMELY_ACTIVE
}
