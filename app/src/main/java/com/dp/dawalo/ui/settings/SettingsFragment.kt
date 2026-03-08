package com.dp.dawalo.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.databinding.FragmentSettingsBinding
import com.dp.dawalo.sync.SyncManager
import com.dp.dawalo.ui.auth.LoginActivity
import com.dp.dawalo.ui.profile.EditProfileActivity
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferenceManager
    private lateinit var syncManager: SyncManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager(requireContext())
        syncManager = SyncManager(requireContext())

        loadSettings()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }

    private fun loadSettings() {
        binding.switchSimplifiedUI.isChecked = prefs.isSimplifiedMode
        binding.switchVoiceGuidance.isChecked = prefs.isVoiceGuidanceEnabled
        binding.switchNotifications.isChecked = true
        binding.switchVoiceAlerts.isChecked = true

        lifecycleScope.launch {
            val app = requireActivity().application as MedNutriTrackApp
            val user = app.database.userDao().getUserById(prefs.userId)
            user?.let {
                binding.tvLanguage.text = it.languageCode.uppercase()
            }
        }
    }

    private fun setupListeners() {
        binding.cardEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.cardViewProfile.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(com.dp.dawalo.R.id.fragmentContainer, com.dp.dawalo.ui.profile.ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardChangeLanguage.setOnClickListener {
            showLanguagePicker()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, _ -> }

        binding.switchVoiceAlerts.setOnCheckedChangeListener { _, _ -> }

        binding.switchSimplifiedUI.setOnCheckedChangeListener { _, isChecked ->
            prefs.isSimplifiedMode = isChecked
            requireActivity().recreate()
        }

        binding.switchVoiceGuidance.setOnCheckedChangeListener { _, isChecked ->
            prefs.isVoiceGuidanceEnabled = isChecked
        }

        binding.btnLogout.setOnClickListener {
            prefs.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showLanguagePicker() {
        val languages = arrayOf("English", "Hindi")
        val codes = arrayOf("en", "hi")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                updateUserLocal { it.copy(languageCode = codes[which], isSynced = false) }
                binding.tvLanguage.text = codes[which].uppercase()
            }
            .show()
    }

    /** Save to Room instantly, then trigger background sync */
    private fun updateUserLocal(transform: (com.dp.dawalo.data.local.entity.User) -> com.dp.dawalo.data.local.entity.User) {
        lifecycleScope.launch {
            val app = requireActivity().application as MedNutriTrackApp
            val user = app.database.userDao().getUserById(prefs.userId) ?: return@launch
            app.database.userDao().update(transform(user))
            // async sync to backend
            syncManager.syncAll()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
