package com.example.data.repository

import com.example.data.local.FoodReminderDao
import com.example.data.model.FoodReminder
import com.example.data.model.FoodScheduleConfig
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FoodReminderRepository(private val foodReminderDao: FoodReminderDao) {
    val allFoodReminders: Flow<List<FoodReminder>> = foodReminderDao.getAllFoodReminders()
    val activeFoodReminders: Flow<List<FoodReminder>> = foodReminderDao.getActiveFoodReminders()
    val scheduleConfigFlow: Flow<FoodScheduleConfig?> = foodReminderDao.getScheduleConfigFlow()

    suspend fun getFoodReminderById(id: Long): FoodReminder? = foodReminderDao.getFoodReminderById(id)

    fun getFoodReminderByIdFlow(id: Long): Flow<FoodReminder?> = foodReminderDao.getFoodReminderByIdFlow(id)

    suspend fun insertFoodReminder(reminder: FoodReminder): Long = foodReminderDao.insertFoodReminder(reminder)

    suspend fun updateFoodReminder(reminder: FoodReminder) = foodReminderDao.updateFoodReminder(reminder)

    suspend fun setReminderActive(id: Long, isActive: Boolean) = foodReminderDao.setReminderActive(id, isActive)

    suspend fun deleteFoodReminder(reminder: FoodReminder) = foodReminderDao.deleteFoodReminder(reminder)

    suspend fun deleteFoodReminderById(id: Long) = foodReminderDao.deleteFoodReminderById(id)

    suspend fun getScheduleConfig(): FoodScheduleConfig = foodReminderDao.getScheduleConfig() ?: FoodScheduleConfig()

    suspend fun saveScheduleConfig(config: FoodScheduleConfig) = foodReminderDao.saveScheduleConfig(config)

    /**
     * Generates and replaces the daily eating schedule starting at [startTime] (e.g. "07:00"),
     * repeating every [intervalMinutes] (e.g. 120 minutes / 2 hours) for [totalReminders] (e.g. 10).
     * Handles crossing midnight automatically (e.g. 11:00 PM + 2h = 01:00 AM next day).
     */
    suspend fun regenerateSchedule(
        startTime: String = "07:00",
        intervalMinutes: Int = 120,
        totalReminders: Int = 10
    ): List<FoodReminder> {
        val parts = startTime.split(":")
        val startH = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val startM = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startH)
            set(Calendar.MINUTE, startM)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val generated = mutableListOf<FoodReminder>()

        for (i in 0 until totalReminders) {
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val m = cal.get(Calendar.MINUTE)
            val timeStr = String.format("%02d:%02d", h, m)
            val isNextDay = cal.get(Calendar.DAY_OF_YEAR) != startDayOfYear

            val mealNum = i + 1
            val mealLabel = when (mealNum) {
                1 -> "Meal 1 (07:00 AM - সকালের প্রথম খাবার)"
                2 -> "Meal 2 (09:00 AM - সকালের নাস্তা)"
                3 -> "Meal 3 (11:00 AM - দুপুর পূর্ব খাবার)"
                4 -> "Meal 4 (01:00 PM - দুপুরের প্রধান খাবার)"
                5 -> "Meal 5 (03:00 PM - দুপুর পরবর্তী খাবার)"
                6 -> "Meal 6 (05:00 PM - বিকালের নাস্তা)"
                7 -> "Meal 7 (07:00 PM - সন্ধ্যার খাবার)"
                8 -> "Meal 8 (09:00 PM - রাতের প্রধান খাবার)"
                9 -> "Meal 9 (11:00 PM - ঘুমানোর আগের খাবার)"
                10 -> "Meal 10 (01:00 AM - দেরি রাতের খাবার / পরবর্তী দিন)"
                else -> "Meal #$mealNum (${timeStr}${if (isNextDay) " Next Day" else ""})"
            }

            val mealType = when (mealNum) {
                1, 2 -> MealType.BREAKFAST
                4 -> MealType.LUNCH
                7, 8 -> MealType.DINNER
                else -> MealType.SNACKS
            }

            generated.add(
                FoodReminder(
                    mealType = mealType,
                    customName = mealLabel,
                    time = timeStr,
                    orderIndex = mealNum,
                    isNextDay = isNextDay,
                    daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                    isActive = true,
                    soundEnabled = true,
                    vibrationEnabled = true,
                    notes = "Scheduled eating reminder #$mealNum"
                )
            )

            cal.add(Calendar.MINUTE, intervalMinutes)
        }

        // Delete existing and insert new
        foodReminderDao.deleteAllFoodReminders()
        foodReminderDao.insertAll(generated)
        foodReminderDao.saveScheduleConfig(
            FoodScheduleConfig(
                id = 1,
                startTime = startTime,
                intervalHours = intervalMinutes / 60,
                totalReminders = totalReminders,
                isEnabled = true
            )
        )

        return foodReminderDao.getAllFoodReminders().let {
            foodReminderDao.getFoodReminderById(1L) // verify
            generated
        }
    }
}
