package com.dp.dawalo.ui.nutrition

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dp.dawalo.data.local.entity.DailyFoodLog
import com.dp.dawalo.databinding.ItemFoodLogBinding
import java.text.SimpleDateFormat
import java.util.*

class FoodLogAdapter(
    private val onDeleteClick: (DailyFoodLog) -> Unit
) : ListAdapter<DailyFoodLog, FoodLogAdapter.ViewHolder>(FoodLogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFoodLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(foodLog: DailyFoodLog) {
            binding.tvFoodName.text = foodLog.foodName
            binding.tvQuantity.text = "${foodLog.quantity} ${foodLog.unit}"
            binding.tvProtein.text = "Protein: ${foodLog.protein}g"
            binding.tvCalories.text = "Calories: ${foodLog.calories} kcal"
            binding.tvMealType.text = foodLog.mealType.name
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.tvTime.text = timeFormat.format(Date(foodLog.time))
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(foodLog)
            }
        }
    }
}

class FoodLogDiffCallback : DiffUtil.ItemCallback<DailyFoodLog>() {
    override fun areItemsTheSame(oldItem: DailyFoodLog, newItem: DailyFoodLog): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DailyFoodLog, newItem: DailyFoodLog): Boolean {
        return oldItem == newItem
    }
}
