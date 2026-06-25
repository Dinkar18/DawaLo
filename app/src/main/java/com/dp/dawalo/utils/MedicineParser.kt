package com.dp.dawalo.utils

import android.util.Log
import java.util.regex.Pattern

object MedicineParser {
    
    private const val TAG = "MedicineParser"
    
    fun parseVoiceInput(text: String): MedicineInput? {
        Log.d(TAG, "Parsing: '$text'")
        val lowerText = text.lowercase().trim()
        
        val dosage = extractDosage(lowerText)
        val time = extractTime(lowerText)
        val medicineName = extractMedicineName(lowerText)
        
        Log.d(TAG, "Parsed -> name='$medicineName', dosage='$dosage', time='$time'")
        
        return if (medicineName.isNotEmpty()) {
            MedicineInput(medicineName, dosage, time)
        } else {
            null
        }
    }
    
    private fun extractMedicineName(text: String): String {
        // Remove dosage and time patterns first to isolate the medicine name
        var cleaned = text
        
        // Remove dosage patterns
        val dosagePatterns = listOf(
            Regex("\\d+\\s*(tablet|pill|capsule|mg|ml|goli|goliya|goliyan)s?"),
            Regex("(ek|do|teen|char|paanch|one|two|three|four|five)\\s*(tablet|pill|capsule|goli|goliya|goliyan)s?"),
            Regex("\\d+\\s*mg"),
            Regex("\\d+\\s*ml")
        )
        dosagePatterns.forEach { cleaned = cleaned.replace(it, " ") }
        
        // Remove time patterns
        val timePatterns = listOf(
            Regex("\\d{1,2}\\s*:?\\s*\\d{0,2}\\s*(am|pm|baje)"),
            Regex("\\b(morning|evening|night|afternoon|subah|sham|raat|dopahar|savere)\\b"),
            Regex("\\b(at|in the|every|daily|roz|har din)\\b")
        )
        timePatterns.forEach { cleaned = cleaned.replace(it, " ") }
        
        // Remove common filler words
        val fillerWords = setOf(
            "take", "lena", "lo", "le", "khana", "kha", "please", "set", "reminder",
            "for", "of", "the", "a", "an", "meri", "mera", "ka", "ki", "ke",
            "dawai", "dawa", "medicine", "remind", "me", "ko", "hai"
        )
        
        val words = cleaned.split(Regex("\\s+")).filter { word ->
            word.length >= 2 && !filerWords(word, fillerWords) && !word.matches(Regex("\\d+"))
        }
        
        // Take first 1-3 meaningful words as medicine name
        val name = words.take(3).joinToString(" ").trim()
        return if (name.isNotEmpty()) {
            name.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        } else ""
    }
    
    private fun filerWords(word: String, fillerWords: Set<String>): Boolean {
        return fillerWords.contains(word)
    }
    
    private fun extractDosage(text: String): String {
        // Pattern: "1 tablet", "500mg", "ek goli", "2 pills", "500 mg"
        val patterns = listOf(
            Regex("(\\d+)\\s*mg"),
            Regex("(\\d+)\\s*ml"),
            Regex("(\\d+)\\s*(tablet|pill|capsule|goli|goliya|goliyan)s?"),
            Regex("(ek|do|teen|char|paanch)\\s*(tablet|goli|goliya|goliyan|capsule)s?"),
            Regex("(one|two|three|four|five)\\s*(tablet|pill|capsule)s?"),
            Regex("(half|aadhi|aadha)\\s*(tablet|goli|capsule)s?")
        )
        
        patterns.forEach { pattern ->
            pattern.find(text)?.let { 
                return it.value.trim()
            }
        }
        
        return "1 tablet"
    }
    
    private fun extractTime(text: String): String {
        // Pattern: "9 AM", "9:30 PM", "8 baje", "subah 8 baje"
        
        // Check for specific time with AM/PM
        val timeAmPm = Regex("(\\d{1,2})\\s*:?\\s*(\\d{2})?\\s*(am|pm)", RegexOption.IGNORE_CASE)
        timeAmPm.find(text)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return@let
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val period = match.groupValues[3].lowercase()
            
            val hour24 = when {
                period == "pm" && hour < 12 -> hour + 12
                period == "am" && hour == 12 -> 0
                else -> hour
            }
            return String.format("%02d:%02d", hour24, minute)
        }
        
        // Check for "X baje" (Hindi for "X o'clock")
        val timeBaje = Regex("(\\d{1,2})\\s*baje")
        timeBaje.find(text)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return@let
            // Determine AM/PM from context
            val hour24 = when {
                text.contains("subah") || text.contains("savere") || text.contains("morning") -> 
                    if (hour <= 12) hour else hour
                text.contains("sham") || text.contains("evening") -> 
                    if (hour < 12) hour + 12 else hour
                text.contains("raat") || text.contains("night") -> 
                    if (hour < 12) hour + 12 else hour
                hour in 1..6 -> hour + 12  // Assume PM for 1-6 without context
                else -> hour
            }
            return String.format("%02d:00", hour24)
        }
        
        // Check for just a number that looks like a time
        val justNumber = Regex("\\b(\\d{1,2})\\s*:?\\s*(\\d{2})\\b")
        justNumber.find(text)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return@let
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            if (hour in 0..23 && minute in 0..59) {
                return String.format("%02d:%02d", hour, minute)
            }
        }
        
        // Check for time of day words
        return when {
            text.contains("morning") || text.contains("subah") || text.contains("savere") -> "09:00"
            text.contains("afternoon") || text.contains("dopahar") -> "14:00"
            text.contains("evening") || text.contains("sham") -> "18:00"
            text.contains("night") || text.contains("raat") -> "21:00"
            else -> "09:00"
        }
    }
}
