package com.dp.dawalo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.data.repository.MedicineRepository
import kotlinx.coroutines.launch

class MedicineViewModel(private val repository: MedicineRepository) : ViewModel() {
    
    fun getAllMedicines(userId: Long): LiveData<List<Medicine>> {
        return repository.getAllMedicines(userId)
    }
    
    fun addMedicine(medicine: Medicine, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insert(medicine)
            onSuccess(id)
        }
    }
    
    fun updateMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.update(medicine)
        }
    }
    
    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.delete(medicine)
        }
    }
}
