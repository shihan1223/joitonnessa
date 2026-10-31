package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mealType: MealType = MealType.OTHER,
    val scheduledTime: String = "07:00", // HH:mm format
    val orderIndex: Int = 1, // 1 to 10 for daily 10-meal schedule
    val isNextDay: Boolean = false, // True when time crosses midnight (e.g. 01:00 AM)
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon .. 7=Sun
    val recurrenceType: String = "DAILY", // "DAILY", "WEEKDAYS", "CUSTOM"
    val isActive: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val completionStatus: ReminderStatus = ReminderStatus.PENDING,
    val lastCompletedDateIso: String? = null,
    val completedAt: Date? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
