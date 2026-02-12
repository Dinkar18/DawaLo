package com.dp.dawalo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_food_logs")
data class DailyFoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val foodName: String,
    val quantity: Float,
    val unit: String,
    val protein: Float,
    val calories: Float,
    val mealType: MealType,
    val date: Long,
    val time: Long,
    val isSynced: Boolean = false,
    val needsSync: Boolean = true,
    val serverId: Long? = null
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACKS
}

