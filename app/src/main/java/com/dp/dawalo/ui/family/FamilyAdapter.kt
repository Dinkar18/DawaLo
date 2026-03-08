package com.dp.dawalo.ui.family

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dp.dawalo.data.local.entity.FamilyMember
import com.dp.dawalo.databinding.ItemFamilyMemberBinding

class FamilyAdapter(
    private val onDelete: (FamilyMember) -> Unit,
    private val onToggleNotify: (FamilyMember) -> Unit
) : ListAdapter<FamilyMember, FamilyAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFamilyMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: ItemFamilyMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: FamilyMember) {
            binding.tvName.text = member.name
            binding.tvPhone.text = member.phone
            binding.tvRelation.text = member.relation
            binding.switchNotify.isChecked = member.notifyOnMissed
            
            binding.switchNotify.setOnCheckedChangeListener { _, _ -> onToggleNotify(member) }
            binding.btnDelete.setOnClickListener { onDelete(member) }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<FamilyMember>() {
        override fun areItemsTheSame(oldItem: FamilyMember, newItem: FamilyMember) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FamilyMember, newItem: FamilyMember) = oldItem == newItem
    }
}
