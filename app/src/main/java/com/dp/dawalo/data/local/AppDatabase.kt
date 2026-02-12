package com.dp.dawalo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dp.dawalo.data.local.dao.*
import com.dp.dawalo.data.local.entity.*
import com.dp.dawalo.utils.Converters

@Database(
    entities = [
        User::class,
        Medicine::class,
        MedicineLog::class,
        FoodItem::class,
        DailyFoodLog::class,
        ProteinSummary::class,
        WaterLog::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicineDao(): MedicineDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun dailyFoodLogDao(): DailyFoodLogDao
    abstract fun waterLogDao(): WaterLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mednutritrack_db"
                )
                .fallbackToDestructiveMigration() // For development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
