package com.dp.dawalo.data.repository

import androidx.lifecycle.LiveData
import com.dp.dawalo.data.local.dao.FamilyMemberDao
import com.dp.dawalo.data.local.entity.FamilyMember

class FamilyRepository(
    private val familyMemberDao: FamilyMemberDao
) {
    
    fun getFamilyMembers(userId: Long): LiveData<List<FamilyMember>> {
        return familyMemberDao.getFamilyMembers(userId)
    }
    
    suspend fun insert(familyMember: FamilyMember): Long {
        return familyMemberDao.insert(familyMember.copy(needsSync = true))
    }
    
    suspend fun update(familyMember: FamilyMember) {
        familyMemberDao.update(familyMember.copy(needsSync = true))
    }
    
    suspend fun delete(familyMember: FamilyMember) {
        familyMemberDao.delete(familyMember)
    }
    
    suspend fun getNotifiableMembers(userId: Long): List<FamilyMember> {
        return familyMemberDao.getNotifiableMembers(userId)
    }
}
