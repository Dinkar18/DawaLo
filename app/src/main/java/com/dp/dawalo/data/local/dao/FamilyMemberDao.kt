package com.dp.dawalo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dp.dawalo.data.local.entity.FamilyMember

@Dao
interface FamilyMemberDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(familyMember: FamilyMember): Long
    
    @Update
    suspend fun update(familyMember: FamilyMember)
    
    @Delete
    suspend fun delete(familyMember: FamilyMember)
    
    @Query("SELECT * FROM family_members WHERE userId = :userId ORDER BY isPrimary DESC, name ASC")
    fun getFamilyMembers(userId: Long): LiveData<List<FamilyMember>>
    
    @Query("SELECT * FROM family_members WHERE userId = :userId AND notifyOnMissed = 1")
    suspend fun getNotifiableMembers(userId: Long): List<FamilyMember>
    
    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getFamilyMemberById(id: Long): FamilyMember?
    
    @Query("SELECT * FROM family_members WHERE userId = :userId AND isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryContact(userId: Long): FamilyMember?
    
    @Query("SELECT * FROM family_members WHERE needsSync = 1")
    suspend fun getUnsyncedMembers(): List<FamilyMember>
    
    @Query("UPDATE family_members SET isSynced = 1, needsSync = 0, serverId = :serverId WHERE id = :id")
    suspend fun markAsSynced(id: Long, serverId: Long)
    
    @Query("DELETE FROM family_members WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
    
    @Query("SELECT * FROM family_members WHERE userId = :userId")
    suspend fun getFamilyMembersByUserId(userId: Long): List<FamilyMember>
}
