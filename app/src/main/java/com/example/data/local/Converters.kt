package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.FoodRelation
import com.example.data.model.MealType
import com.example.data.model.MedicineType
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderStatus
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return value?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromMedicineType(value: MedicineType?): String = value?.name ?: MedicineType.TABLET.name

    @TypeConverter
    fun toMedicineType(value: String?): MedicineType = try {
        MedicineType.valueOf(value ?: MedicineType.TABLET.name)
    } catch (_: Exception) {
        MedicineType.TABLET
    }

    @TypeConverter
    fun fromFoodRelation(value: FoodRelation?): String = value?.name ?: FoodRelation.AFTER_FOOD.name

    @TypeConverter
    fun toFoodRelation(value: String?): FoodRelation = try {
        FoodRelation.valueOf(value ?: FoodRelation.AFTER_FOOD.name)
    } catch (_: Exception) {
        FoodRelation.AFTER_FOOD
    }

    @TypeConverter
    fun fromMealType(value: MealType?): String = value?.name ?: MealType.BREAKFAST.name

    @TypeConverter
    fun toMealType(value: String?): MealType = try {
        MealType.valueOf(value ?: MealType.BREAKFAST.name)
    } catch (_: Exception) {
        MealType.BREAKFAST
    }

    @TypeConverter
    fun fromReminderCategory(value: ReminderCategory?): String = value?.name ?: ReminderCategory.MEDICINE.name

    @TypeConverter
    fun toReminderCategory(value: String?): ReminderCategory = try {
        ReminderCategory.valueOf(value ?: ReminderCategory.MEDICINE.name)
    } catch (_: Exception) {
        ReminderCategory.MEDICINE
    }

    @TypeConverter
    fun fromReminderStatus(value: ReminderStatus?): String = value?.name ?: ReminderStatus.PENDING.name

    @TypeConverter
    fun toReminderStatus(value: String?): ReminderStatus = try {
        ReminderStatus.valueOf(value ?: ReminderStatus.PENDING.name)
    } catch (_: Exception) {
        ReminderStatus.PENDING
    }
}
