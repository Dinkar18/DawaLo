package com.dp.dawalo.ui.medicine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.databinding.ItemMedicineBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MedicineAdapter(
    private val onDeleteClick: (Medicine) -> Unit
) : ListAdapter<Medicine, MedicineAdapter.MedicineViewHolder>(MedicineDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MedicineViewHolder(private val binding: ItemMedicineBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(medicine: Medicine) {
            binding.tvMedicineName.text = medicine.name
            binding.tvDosage.text = "Dosage: ${medicine.dosage}"
            
            val times: List<String> = Gson().fromJson(medicine.times, object : TypeToken<List<String>>() {}.type)
            binding.tvTimes.text = "Times: ${times.joinToString(", ")}"
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(medicine)
            }
        }
    }

    class MedicineDiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
            return oldItem == newItem
        }
    }
}
