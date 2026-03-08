package com.dp.dawalo.utils

import androidx.room.TypeConverter
import com.dp.dawalo.data.local.entity.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromGoal(value: Goal): String = value.name
    
    @TypeConverter
    fun toGoal(value: String): Goal = Goal.valueOf(value)
    
    @TypeConverter
    fun fromMedicineStatus(value: MedicineStatus): String = value.name
    
    @TypeConverter
    fun toMedicineStatus(value: String): MedicineStatus = MedicineStatus.valueOf(value)
    
    @TypeConverter
    fun fromProteinStatus(value: ProteinStatus): String = value.name
    
    @TypeConverter
    fun toProteinStatus(value: String): ProteinStatus = ProteinStatus.valueOf(value)
    
    @TypeConverter
    fun fromGender(value: Gender): String = value.name
    
    @TypeConverter
    fun toGender(value: String): Gender = Gender.valueOf(value)
    
    @TypeConverter
    fun fromDietType(value: DietType): String = value.name
    
    @TypeConverter
    fun toDietType(value: String): DietType = DietType.valueOf(value)
    
    @TypeConverter
    fun fromActivityLevel(value: ActivityLevel): String = value.name
    
    @TypeConverter
    fun toActivityLevel(value: String): ActivityLevel = ActivityLevel.valueOf(value)
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
