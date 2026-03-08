package com.dp.dawalo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val phone: String,
    val relation: String, // Son, Daughter, Spouse, Caregiver, etc.
    val photoUrl: String? = null,
    val isPrimary: Boolean = false,
    val notifyOnMissed: Boolean = true,
    val notifyDelay: Int = 15, // minutes
    val smsEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    // Sync fields
    val isSynced: Boolean = false,
    val needsSync: Boolean = false,
    val serverId: Long? = null
)
