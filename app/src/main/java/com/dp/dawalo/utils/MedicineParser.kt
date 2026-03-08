package com.dp.dawalo.utils

import java.util.regex.Pattern

object MedicineParser {
    
    fun parseVoiceInput(text: String): MedicineInput? {
        val lowerText = text.lowercase().trim()
        
        val medicineName = extractMedicineName(lowerText)
        val dosage = extractDosage(lowerText)
        val time = extractTime(lowerText)
        
        return if (medicineName.isNotEmpty()) {
            MedicineInput(medicineName, dosage, time)
        } else {
            null
        }
    }
    
    private fun extractMedicineName(text: String): String {
        // Remove common words to isolate medicine name
        val commonWords = listOf("tablet", "pill", "capsule", "mg", "ml", "morning", "evening", 
            "night", "afternoon", "ek", "do", "teen", "one", "two", "three", "goli", "subah", "sham")
        
        // Split and take first meaningful word(s)
        val words = text.split(" ").filter { word ->
            word.length > 2 && !commonWords.contains(word) && !word.matches(Regex("\\d+.*"))
        }
        
        // Take first 1-2 words as medicine name
        return words.take(2).joinToString(" ").replaceFirstChar { it.uppercase() }
    }
    
    private fun extractDosage(text: String): String {
        // Pattern: "1 tablet", "500mg", "ek goli", "2 pills"
        val patterns = listOf(
            Regex("(\\d+)\\s*(tablet|pill|capsule|mg|ml|goli)s?"),
            Regex("(ek|do|teen|char|paanch)\\s*(tablet|goli|capsule)s?"),
            Regex("(one|two|three|four|five)\\s*(tablet|pill|capsule)s?")
        )
        
        patterns.forEach { pattern ->
            pattern.find(text)?.let { 
                return it.value.trim()
            }
        }
        
        return "1 tablet"
    }
    
    private fun extractTime(text: String): String {
        // Pattern: "9 AM", "morning", "subah 8 baje", "evening"
        
        // Check for specific time (9 AM, 8:30 PM, etc.)
        val timePattern = Regex("(\\d{1,2})\\s*:?\\s*(\\d{2})?\\s*(am|pm|baje)?")
        timePattern.find(text)?.let { match ->
            val hour = match.groupValues[1].toInt()
            val minute = match.groupValues[2].ifEmpty { "00" }
            val period = match.groupValues[3]
            
            val hour24 = when {
                period.contains("pm") && hour < 12 -> hour + 12
                period.contains("am") && hour == 12 -> 0
                else -> hour
            }
            
            return String.format("%02d:%02d", hour24, minute.toIntOrNull() ?: 0)
        }
        
        // Check for time of day
        return when {
            text.contains("morning") || text.contains("subah") -> "09:00"
            text.contains("afternoon") || text.contains("dopahar") -> "14:00"
            text.contains("evening") || text.contains("sham") -> "18:00"
            text.contains("night") || text.contains("raat") -> "21:00"
            else -> "09:00"
        }
    }
}
