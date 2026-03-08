package com.dp.dawalo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_logs")
data class MedicineLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val medicineId: Long,
    val scheduledTime: Long,
    val actualTime: Long?,
    val status: MedicineStatus,
    val notes: String? = null
)

enum class MedicineStatus {
    TAKEN, MISSED, SKIPPED, SNOOZED
}
