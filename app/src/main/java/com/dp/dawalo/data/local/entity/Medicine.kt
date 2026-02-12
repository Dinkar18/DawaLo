package com.dp.dawalo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val dosage: String,
    val frequency: String,
    val times: String, // JSON array of times ["09:00", "21:00"]
    val startDate: Long,
    val endDate: Long?,
    val isActive: Boolean = true,
    val isSynced: Boolean = false,
    val needsSync: Boolean = true,
    val serverId: Long? = null
)
