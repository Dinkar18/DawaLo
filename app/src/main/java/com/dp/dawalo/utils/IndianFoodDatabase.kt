package com.dp.dawalo.utils

import com.dp.dawalo.data.local.entity.FoodItem

object IndianFoodDatabase {
    fun getDefaultFoods(): List<FoodItem> = listOf(
        // Dairy
        FoodItem(0, "Milk (1 glass)", "दूध (1 गिलास)", "দুধ (১ গ্লাস)", "பால் (1 கிளாஸ்)", 8f, "glass", "Dairy"),
        FoodItem(0, "Paneer (100g)", "पनीर (100g)", "পনির (১০০গ্রাম)", "பன்னீர் (100g)", 18f, "100g", "Dairy"),
        FoodItem(0, "Curd (1 bowl)", "दही (1 कटोरी)", "দই (১ বাটি)", "தயிர் (1 கிண்ணம்)", 6f, "bowl", "Dairy"),
        
        // Pulses
        FoodItem(0, "Dal (1 bowl)", "दाल (1 कटोरी)", "ডাল (১ বাটি)", "பருப்பு (1 கிண்ணம்)", 7f, "bowl", "Pulses"),
        FoodItem(0, "Rajma (1 bowl)", "राजमा (1 कटोरी)", "রাজমা (১ বাটি)", "ராஜ்மா (1 கிண்ணம்)", 9f, "bowl", "Pulses"),
        FoodItem(0, "Chana (1 bowl)", "चना (1 कटोरी)", "ছোলা (১ বাটি)", "கொண்டைக்கடலை (1 கிண்ணம்)", 8f, "bowl", "Pulses"),
        
        // Grains
        FoodItem(0, "Roti (1 piece)", "रोटी (1 नग)", "রুটি (১টি)", "ரொட்டி (1 துண்டு)", 3f, "piece", "Grains"),
        FoodItem(0, "Rice (1 bowl)", "चावल (1 कटोरी)", "ভাত (১ বাটি)", "சாதம் (1 கிண்ணம்)", 4f, "bowl", "Grains"),
        
        // Eggs & Meat
        FoodItem(0, "Egg (1 piece)", "अंडा (1 नग)", "ডিম (১টি)", "முட்டை (1 துண்டு)", 6f, "piece", "Protein"),
        FoodItem(0, "Chicken (100g)", "चिकन (100g)", "মুরগি (১০০গ্রাম)", "கோழி (100g)", 25f, "100g", "Protein"),
        FoodItem(0, "Fish (100g)", "मछली (100g)", "মাছ (১০০গ্রাম)", "மீன் (100g)", 20f, "100g", "Protein"),
        
        // Snacks
        FoodItem(0, "Roasted Chana (50g)", "भुना चना (50g)", "ভাজা ছোলা (৫০গ্রাম)", "வறுத்த கடலை (50g)", 6f, "50g", "Snacks"),
        FoodItem(0, "Peanuts (50g)", "मूंगफली (50g)", "চিনাবাদাম (৫০গ্রাম)", "கடலை (50g)", 13f, "50g", "Snacks"),
        
        // Soy
        FoodItem(0, "Soya Chunks (50g)", "सोया चंक्स (50g)", "সয়া চাঙ্ক (৫০গ্রাম)", "சோயா துண்டுகள் (50g)", 26f, "50g", "Soy")
    )
}
