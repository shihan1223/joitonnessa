package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MedicineType(val labelEn: String, val labelBn: String) {
    TABLET("Tablet", "ট্যাবলেট"),
    CAPSULE("Capsule", "ক্যাপসুল"),
    SYRUP("Syrup", "সিরাপ"),
    DROPS("Drops", "ড্রপস"),
    INJECTION("Injection", "ইনজেকশন"),
    OTHER("Other", "অন্যান্য")
}

enum class FoodRelation(val labelEn: String, val labelBn: String) {
    BEFORE_FOOD("Before food", "খাবারের আগে"),
    AFTER_FOOD("After food", "খাবারের পরে"),
    WITH_FOOD("With food", "খাবারের সাথে"),
    NO_FOOD("No food restriction", "খাবারের নিয়ম নেই")
}

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: MedicineType = MedicineType.TABLET,
    val dosage: String,
    val instructions: String = "",
    val foodRelation: FoodRelation = FoodRelation.AFTER_FOOD,
    val reminderTimes: List<String> = emptyList(), // HH:mm format e.g. ["08:00", "14:00", "20:00"]
    val repeatType: String = "DAILY", // "DAILY", "WEEKDAYS", "CUSTOM"
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon .. 7=Sun
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val imageUri: String? = null,
    val isActive: Boolean = true,
    val completionStatus: ReminderStatus = ReminderStatus.PENDING,
    val lastTakenDateIso: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
