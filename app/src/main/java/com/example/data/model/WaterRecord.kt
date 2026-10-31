package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "water_records")
data class WaterRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateIso: String, // YYYY-MM-DD
    val recordDate: Date = Date(),
    val scheduledTime: String = "08:00", // HH:mm
    val intervalMinutes: Int = 90,
    val targetAmountMl: Int = 2500,
    val amountMl: Int = 250,
    val completionStatus: ReminderStatus = ReminderStatus.PENDING,
    val isGoalReached: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
