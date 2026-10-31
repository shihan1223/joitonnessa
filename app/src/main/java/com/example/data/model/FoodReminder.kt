package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType(val labelEn: String, val labelBn: String) {
    BREAKFAST("Breakfast", "সকালের নাস্তা"),
    LUNCH("Lunch", "দুপুরের খাবার"),
    SNACKS("Snacks", "নাস্তা / হালকা খাবার"),
    DINNER("Dinner", "রাতের খাবার"),
    OTHER("Scheduled Meal", "খাদ্য তালিকা")
}

@Entity(tableName = "food_reminders")
data class FoodReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealType: MealType = MealType.OTHER,
    val customName: String = "",
    val time: String = "07:00", // HH:mm format (e.g. 07:00, 09:00, ..., 23:00, 01:00)
    val orderIndex: Int = 1, // 1 to 10 for chronological daily sequence
    val isNextDay: Boolean = false, // True for reminders crossing midnight (e.g. 01:00 AM)
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    val isActive: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notes: String = ""
) {
    val displayName: String
        get() = if (customName.isNotBlank()) customName else "${mealType.labelEn} #$orderIndex"
}

@Entity(tableName = "food_schedule_config")
data class FoodScheduleConfig(
    @PrimaryKey
    val id: Int = 1,
    val startTime: String = "07:00", // 07:00 AM
    val intervalHours: Int = 2, // Every 2 hours
    val totalReminders: Int = 10, // 10 per day
    val isEnabled: Boolean = true
)
