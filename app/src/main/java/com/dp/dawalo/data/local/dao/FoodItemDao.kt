package com.dp.dawalo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dp.dawalo.data.local.entity.FoodItem

@Dao
interface FoodItemDao {
    @Insert
    suspend fun insertAll(foods: List<FoodItem>)
    
    @Query("SELECT * FROM food_items ORDER BY category, name")
    fun getAllFoodItems(): LiveData<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE category = :category")
    fun getFoodsByCategory(category: String): LiveData<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getFoodById(id: Int): FoodItem?
}
