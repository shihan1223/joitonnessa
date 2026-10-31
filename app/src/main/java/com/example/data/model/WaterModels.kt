package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_config")
data class WaterConfig(
    @PrimaryKey
    val id: Int = 1,
    val dailyGoalMl: Int = 2500,
    val intervalMinutes: Int = 90,
    val startHour: Int = 8,
    val endHour: Int = 22,
    val glassSizeMl: Int = 250,
    val isEnabled: Boolean = true
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateIso: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val amountMl: Int = 250
)
