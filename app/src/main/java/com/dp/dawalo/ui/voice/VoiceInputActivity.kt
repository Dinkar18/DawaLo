package com.dp.dawalo.ui.voice

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.FontAssetDelegate
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.local.entity.Medicine
import com.dp.dawalo.databinding.ActivityVoiceInputBinding
import com.dp.dawalo.utils.AlarmScheduler
import com.dp.dawalo.utils.MedicineInput
import com.dp.dawalo.utils.PreferenceManager
import com.dp.dawalo.utils.VoiceInputHelper
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*

class VoiceInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceInputBinding
    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var prefs: PreferenceManager

    // Conversational state
    private enum class Step { NAME, DOSAGE, TIME, DONE }
    private var currentStep = Step.NAME
    private var medicineName = ""
    private var dosage = ""
    private var time = ""

    companion object {
        private const val REQUEST_RECORD_AUDIO = 100
    }

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (text != null) handleStepResult(text)
            else retryCurrentStep("No speech recognized.")
        } else {
            retryCurrentStep("Cancelled. Try again.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prevent crash on missing fonts in Lottie animations
        binding.lottieMic.setFontAssetDelegate(object : FontAssetDelegate() {
            override fun getFontPath(fontFamily: String?): String? = null
            override fun fetchFont(fontFamily: String?): Typeface = Typeface.DEFAULT
        })

        prefs = PreferenceManager(this)
        voiceHelper = VoiceInputHelper(this)

        binding.btnConfirm.isEnabled = false
        binding.btnStartVoice.setOnClickListener {
            if (checkPermissions()) startConversation()
        }
        binding.btnConfirm.setOnClickListener { confirmAndSave() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startConversation()
        }
    }

    private fun startConversation() {
        currentStep = Step.NAME
        medicineName = ""
        dosage = ""
        time = ""
        binding.cardResult.visibility = View.GONE
        binding.btnConfirm.isEnabled = false
        askCurrentStep()
    }

    private fun askCurrentStep() {
        val (prompt, hint) = when (currentStep) {
            Step.NAME -> Pair(getLocalizedString("name"), "e.g. Paracetamol, Crocin")
            Step.DOSAGE -> Pair(getLocalizedString("dosage"), "e.g. 1 tablet, 500mg")
            Step.TIME -> Pair(getLocalizedString("time"), "e.g. 9 AM, morning, sham 6 baje")
            Step.DONE -> return
        }

        binding.tvInstructions.text = prompt
        binding.tvStepHint.visibility = View.VISIBLE
        binding.tvStepHint.text = hint
        updateStepIndicator()

        // Speak the prompt then auto-listen
        voiceHelper.speak(prompt, prefs.languageCode) {
            runOnUiThread { startListeningForStep() }
        }
    }

    private fun getLocalizedString(field: String): String {
        return when (prefs.languageCode) {
            "hi" -> when (field) {
                "name" -> "दवाई का नाम बताइए"
                "dosage" -> "कितनी खुराक? जैसे 1 गोली"
                "time" -> "कब लेनी है? जैसे सुबह 9 बजे"
                else -> ""
            }
            else -> when (field) {
                "name" -> "What is the medicine name?"
                "dosage" -> "What is the dosage?"
                "time" -> "When should you take it?"
                else -> ""
            }
        }
    }

    private fun startListeningForStep() {
        binding.btnStartVoice.text = "🎤 Listening..."
        binding.btnStartVoice.isEnabled = false

        val langTag = when (prefs.languageCode) {
            "hi" -> "hi-IN"
            "bn" -> "bn-IN"
            "ta" -> "ta-IN"
            else -> "en-IN"
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            binding.btnStartVoice.isEnabled = true
            binding.btnStartVoice.text = "🎤 Retry"
            Toast.makeText(this, "Speech recognition not available.", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleStepResult(text: String) {
        binding.btnStartVoice.isEnabled = true
        binding.btnStartVoice.text = "🎤 Next"

        when (currentStep) {
            Step.NAME -> {
                medicineName = text.trim().replaceFirstChar { it.uppercase() }
                binding.tvStepName.text = "💊 $medicineName"
                binding.tvStepName.visibility = View.VISIBLE
                currentStep = Step.DOSAGE
                askCurrentStep()
            }
            Step.DOSAGE -> {
                dosage = normalizeDosage(text.trim())
                binding.tvStepDosage.text = "📦 $dosage"
                binding.tvStepDosage.visibility = View.VISIBLE
                currentStep = Step.TIME
                askCurrentStep()
            }
            Step.TIME -> {
                time = normalizeTime(text.trim())
                binding.tvStepTime.text = "⏰ $time"
                binding.tvStepTime.visibility = View.VISIBLE
                currentStep = Step.DONE
                showConfirmation()
            }
            Step.DONE -> {}
        }
    }

    private fun retryCurrentStep(msg: String) {
        binding.btnStartVoice.isEnabled = true
        binding.btnStartVoice.text = "🎤 Retry"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun updateStepIndicator() {
        val step = when (currentStep) {
            Step.NAME -> "Step 1/3"
            Step.DOSAGE -> "Step 2/3"
            Step.TIME -> "Step 3/3"
            Step.DONE -> "Done ✓"
        }
        binding.tvStepIndicator.text = step
    }

    private fun showConfirmation() {
        binding.cardResult.visibility = View.VISIBLE
        binding.tvMedicineName.text = "Medicine: $medicineName"
        binding.tvDosage.text = "Dosage: $dosage"
        binding.tvTime.text = "Time: $time"
        binding.btnConfirm.isEnabled = true
        binding.btnStartVoice.text = "🔄 Start Over"
        binding.tvInstructions.text = "Please confirm or start over"
        binding.tvStepHint.visibility = View.GONE

        val confirmMsg = if (prefs.languageCode == "hi")
            "$medicineName, $dosage, $time बजे। पुष्टि करें।"
        else "$medicineName, $dosage, at $time. Tap confirm to save."
        voiceHelper.speak(confirmMsg, prefs.languageCode, null)
    }

    private fun normalizeDosage(text: String): String {
        // If user just says a number, append "tablet"
        if (text.matches(Regex("\\d+"))) return "$text tablet"
        return text
    }

    private fun normalizeTime(text: String): String {
        val lower = text.lowercase()
        // Try parsing direct time formats
        Regex("(\\d{1,2})\\s*:?\\s*(\\d{2})?\\s*(am|pm)", RegexOption.IGNORE_CASE).find(lower)?.let { m ->
            val h = m.groupValues[1].toIntOrNull() ?: return text
            val min = m.groupValues[2].toIntOrNull() ?: 0
            val pm = m.groupValues[3].lowercase() == "pm"
            val h24 = if (pm && h < 12) h + 12 else if (!pm && h == 12) 0 else h
            return String.format("%02d:%02d", h24, min)
        }
        // Hindi "X baje"
        Regex("(\\d{1,2})\\s*baje").find(lower)?.let { m ->
            val h = m.groupValues[1].toIntOrNull() ?: return text
            val h24 = when {
                lower.contains("subah") || lower.contains("morning") -> h
                lower.contains("sham") || lower.contains("evening") -> if (h < 12) h + 12 else h
                lower.contains("raat") || lower.contains("night") -> if (h < 12) h + 12 else h
                h in 1..6 -> h + 12
                else -> h
            }
            return String.format("%02d:00", h24)
        }
        // Named times
        return when {
            lower.contains("morning") || lower.contains("subah") -> "09:00"
            lower.contains("afternoon") || lower.contains("dopahar") -> "14:00"
            lower.contains("evening") || lower.contains("sham") -> "18:00"
            lower.contains("night") || lower.contains("raat") -> "21:00"
            else -> text // Return raw if we can't parse — user can see & confirm
        }
    }

    private fun confirmAndSave() {
        binding.btnConfirm.isEnabled = false
        lifecycleScope.launch {
            try {
                val app = application as MedNutriTrackApp
                val timeFormatted = if (time.matches(Regex("\\d{2}:\\d{2}"))) time else "09:00"

                val medicine = Medicine(
                    userId = prefs.userId,
                    name = medicineName,
                    dosage = dosage,
                    frequency = "Daily",
                    times = Gson().toJson(listOf(timeFormatted)),
                    startDate = System.currentTimeMillis(),
                    endDate = null,
                    isActive = true
                )

                val id = app.database.medicineDao().insert(medicine)
                scheduleAlarm(id, medicineName, timeFormatted)

                runOnUiThread {
                    Toast.makeText(this@VoiceInputActivity, "✅ $medicineName saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.btnConfirm.isEnabled = true
                    Toast.makeText(this@VoiceInputActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun scheduleAlarm(medicineId: Long, name: String, time: String) {
        val parts = time.split(":")
        if (parts.size == 2) {
            val h = parts[0].toIntOrNull() ?: return
            val m = parts[1].toIntOrNull() ?: return
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
            }
            AlarmScheduler.scheduleMedicineAlarm(this, medicineId, name, cal.timeInMillis, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.shutdown()
    }
}
