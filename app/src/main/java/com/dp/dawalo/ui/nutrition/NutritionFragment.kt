package com.dp.dawalo.ui.nutrition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.local.entity.DailyFoodLog
import com.dp.dawalo.data.local.entity.MealType
import com.dp.dawalo.databinding.FragmentNutritionBinding
import com.dp.dawalo.databinding.DialogAddFoodBinding
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.viewmodel.NutritionViewModel
import com.dp.dawalo.viewmodel.NutritionViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class NutritionFragment : Fragment() {
    
    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!
    
    private val prefs: PreferenceManager by lazy { PreferenceManager(requireContext()) }
    private lateinit var adapter: FoodLogAdapter
    
    private val viewModel: NutritionViewModel by viewModels {
        val app = requireActivity().application as MedNutriTrackApp
        NutritionViewModelFactory(prefs, app.database.dailyFoodLogDao())
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNutritionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        viewModel.loadTodayData()
    }
    
    private fun setupRecyclerView() {
        adapter = FoodLogAdapter { foodLog ->
            viewModel.deleteLog(foodLog.id)
        }
        
        binding.rvFoodLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFoodLogs.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.todayLogs.observe(viewLifecycleOwner) { logs ->
            adapter.submitList(logs)
            binding.tvEmptyState.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.summary.observe(viewLifecycleOwner) { summary ->
            val protein = summary["protein"] ?: 0f
            val calories = summary["calories"] ?: 0f
            
            binding.tvProteinConsumed.text = "${protein.toInt()}g"
            binding.tvCaloriesConsumed.text = "${calories.toInt()} kcal"
            
            loadUserTargets(protein, calories)
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            // Show sync status (optional - can add a TextView in layout)
            status?.let {
                android.util.Log.d("NutritionFragment", "Sync status: $it")
            }
        }
    }
    
    private fun loadUserTargets(consumedProtein: Float, consumedCalories: Float) {
        val app = requireActivity().application as MedNutriTrackApp
        val userDao = app.database.userDao()
        
        lifecycleScope.launch {
            val user = userDao.getUserById(prefs.userId)
            user?.let {
                val proteinTarget = it.dailyProteinTarget
                val calorieTarget = it.dailyCalorieTarget
                
                binding.tvProteinTarget.text = "/ ${proteinTarget.toInt()}g"
                binding.tvCaloriesTarget.text = "/ ${calorieTarget.toInt()} kcal"
                
                binding.progressProtein.max = proteinTarget.toInt()
                binding.progressProtein.progress = consumedProtein.toInt()
                
                binding.progressCalories.max = calorieTarget.toInt()
                binding.progressCalories.progress = consumedCalories.toInt()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddFood.setOnClickListener {
            showAddFoodDialog()
        }
    }
    
    private fun showAddFoodDialog() {
        val dialogBinding = DialogAddFoodBinding.inflate(layoutInflater)
        
        val mealTypes = MealType.values().map { it.name }
        val mealAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mealTypes)
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerMealType.adapter = mealAdapter
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Food")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val foodName = dialogBinding.etFoodName.text.toString()
                val quantity = dialogBinding.etQuantity.text.toString().toFloatOrNull() ?: 0f
                val unit = dialogBinding.etUnit.text.toString()
                val protein = dialogBinding.etProtein.text.toString().toFloatOrNull() ?: 0f
                val calories = dialogBinding.etCalories.text.toString().toFloatOrNull() ?: 0f
                val mealType = MealType.valueOf(dialogBinding.spinnerMealType.selectedItem.toString())
                
                if (foodName.isNotEmpty() && quantity > 0) {
                    val foodLog = DailyFoodLog(
                        userId = prefs.userId,
                        foodName = foodName,
                        quantity = quantity,
                        unit = unit,
                        protein = protein,
                        calories = calories,
                        mealType = mealType,
                        date = System.currentTimeMillis(),
                        time = System.currentTimeMillis()
                    )
                    
                    viewModel.logFood(foodLog) {
                        Toast.makeText(requireContext(), "Food logged successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
