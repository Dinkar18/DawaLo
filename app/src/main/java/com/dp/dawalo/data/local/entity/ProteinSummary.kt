package com.dp.dawalo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protein_summary")
data class ProteinSummary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val date: Long,
    val totalProteinConsumed: Float,
    val targetProtein: Float,
    val status: ProteinStatus
)

enum class ProteinStatus {
    DEFICIENT, GOOD, EXCELLENT
}
