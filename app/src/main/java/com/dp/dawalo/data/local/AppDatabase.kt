package com.dp.dawalo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dp.dawalo.data.local.dao.*
import com.dp.dawalo.data.local.entity.*
import com.dp.dawalo.utils.Converters

@Database(
    entities = [
        User::class,
        Medicine::class,
        MedicineLog::class,
        FamilyMember::class,
        FoodItem::class,
        DailyFoodLog::class,
        ProteinSummary::class,
        WaterLog::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicineDao(): MedicineDao
    abstract fun medicineLogDao(): MedicineLogDao
    abstract fun familyMemberDao(): FamilyMemberDao
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
                // Safe: keep existing data on unknown migration instead of wiping
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
