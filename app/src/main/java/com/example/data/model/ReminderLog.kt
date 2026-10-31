package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReminderCategory(val labelEn: String, val labelBn: String) {
    MEDICINE("Medicine", "ঔষধ"),
    FOOD("Food / Meal", "খাবার"),
    WATER("Water", "পানি")
}

enum class ReminderStatus(val labelEn: String, val labelBn: String) {
    TAKEN("Taken", "নেওয়া হয়েছে"),
    SKIPPED("Skipped", "বাদ দেওয়া হয়েছে"),
    MISSED("Missed", "মিস হয়েছে"),
    PENDING("Pending", "অপেক্ষমান")
}

@Entity(tableName = "reminder_logs")
data class ReminderLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: ReminderCategory,
    val referenceId: Long,
    val title: String,
    val subtitle: String = "",
    val scheduledDateIso: String, // "YYYY-MM-DD"
    val scheduledTime: String, // "HH:mm"
    val scheduledTimestamp: Long,
    val actionTimestamp: Long? = null,
    val status: ReminderStatus = ReminderStatus.PENDING,
    val notes: String = ""
)
