package com.dp.dawalo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_logs")
data class MedicineLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicineId: Int,
    val scheduledTime: Long,
    val takenTime: Long?,
    val status: Status
)

enum class Status {
    PENDING, TAKEN, SKIPPED, MISSED
}
