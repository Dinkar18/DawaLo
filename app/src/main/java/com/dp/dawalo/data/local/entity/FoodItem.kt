package com.dp.dawalo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val nameHi: String,
    val nameBn: String,
    val nameTa: String,
    val proteinPerUnit: Float,
    val unit: String,
    val category: String
)
