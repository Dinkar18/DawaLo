package com.dp.dawalo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dp.dawalo.data.local.entity.FamilyMember
import com.dp.dawalo.data.repository.FamilyRepository
import kotlinx.coroutines.launch

class FamilyViewModel(
    private val repository: FamilyRepository
) : ViewModel() {
    
    fun getFamilyMembers(userId: Long): LiveData<List<FamilyMember>> {
        return repository.getFamilyMembers(userId)
    }
    
    fun addFamilyMember(familyMember: FamilyMember) {
        viewModelScope.launch {
            repository.insert(familyMember)
        }
    }
    
    fun updateFamilyMember(familyMember: FamilyMember) {
        viewModelScope.launch {
            repository.update(familyMember)
        }
    }
    
    fun deleteFamilyMember(familyMember: FamilyMember) {
        viewModelScope.launch {
            repository.delete(familyMember)
        }
    }
}
