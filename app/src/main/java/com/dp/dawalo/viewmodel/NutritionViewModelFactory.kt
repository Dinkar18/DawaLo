package com.dp.dawalo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dp.dawalo.data.local.dao.DailyFoodLogDao
import com.dp.dawalo.utils.PreferenceManager

class NutritionViewModelFactory(
    private val preferenceManager: PreferenceManager,
    private val foodLogDao: DailyFoodLogDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutritionViewModel(preferenceManager, foodLogDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
