package com.dp.dawalo.ui.family

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.local.entity.FamilyMember
import com.dp.dawalo.data.repository.FamilyRepository
import com.dp.dawalo.databinding.FragmentFamilyBinding
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.viewmodel.FamilyViewModel
import com.dp.dawalo.viewmodel.FamilyViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FamilyFragment : Fragment() {
    
    private var _binding: FragmentFamilyBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FamilyViewModel
    private lateinit var adapter: FamilyAdapter
    private lateinit var prefs: PreferenceManager
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFamilyBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefs = PreferenceManager(requireContext())
        val app = requireActivity().application as MedNutriTrackApp
        val repository = FamilyRepository(app.database.familyMemberDao())
        viewModel = ViewModelProvider(this, FamilyViewModelFactory(repository))[FamilyViewModel::class.java]
        
        setupRecyclerView()
        setupFAB()
        observeData()
    }
    
    private fun setupRecyclerView() {
        adapter = FamilyAdapter(
            onDelete = { member -> viewModel.deleteFamilyMember(member) },
            onToggleNotify = { member -> 
                viewModel.updateFamilyMember(member.copy(notifyOnMissed = !member.notifyOnMissed))
            }
        )
        binding.rvFamily.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFamily.adapter = adapter
    }
    
    private fun setupFAB() {
        binding.fabAddFamily.setOnClickListener { showAddDialog() }
    }
    
    private fun observeData() {
        viewModel.getFamilyMembers(prefs.userId).observe(viewLifecycleOwner) { members ->
            adapter.submitList(members)
            binding.tvEmptyState.visibility = if (members.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    private fun showAddDialog() {
        val view = layoutInflater.inflate(com.dp.dawalo.R.layout.dialog_add_family, null)
        val etName = view.findViewById<com.google.android.material.textfield.TextInputEditText>(com.dp.dawalo.R.id.etFamilyName)
        val etPhone = view.findViewById<com.google.android.material.textfield.TextInputEditText>(com.dp.dawalo.R.id.etFamilyPhone)
        val etRelation = view.findViewById<com.google.android.material.textfield.TextInputEditText>(com.dp.dawalo.R.id.etRelation)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Family Member")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val phone = etPhone.text.toString()
                val relation = etRelation.text.toString()
                
                if (name.isNotBlank() && phone.isNotBlank()) {
                    viewModel.addFamilyMember(FamilyMember(
                        userId = prefs.userId,
                        name = name,
                        phone = phone,
                        relation = relation
                    ))
                    Toast.makeText(requireContext(), "Added", Toast.LENGTH_SHORT).show()
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
