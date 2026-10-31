package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.FoodRelation
import com.example.data.model.Medicine
import com.example.data.model.MedicineType
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderLog
import com.example.data.model.ReminderStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun readStringFromContext() {
        val appName = context.getString(R.string.app_name)
        assertEquals("joitonnessa", appName)
    }

    @Test
    fun testRoomMedicineInsertAndQuery() = runBlocking {
        val medicine = Medicine(
            name = "Paracetamol 500mg",
            type = MedicineType.TABLET,
            dosage = "1 Tablet",
            instructions = "Drink plenty of water",
            foodRelation = FoodRelation.AFTER_FOOD,
            reminderTimes = listOf("08:00", "20:00")
        )
        val id = db.medicineDao().insertMedicine(medicine)
        val fetched = db.medicineDao().getMedicineById(id)

        assertNotNull(fetched)
        assertEquals("Paracetamol 500mg", fetched?.name)
        assertEquals(2, fetched?.reminderTimes?.size)
    }

    @Test
    fun testRoomTenMealScheduleInsertionAndToggle() = runBlocking {
        val schedule = AppDatabase.createDefaultTenMealSchedule()
        db.foodReminderDao().insertAll(schedule)

        val allMeals = db.foodReminderDao().getAllFoodReminders().first()
        assertEquals(10, allMeals.size)

        // Check first and last meal times
        assertEquals("07:00", allMeals.first().time)
        assertEquals("01:00", allMeals.last().time)
        assertTrue(allMeals.last().isNextDay)

        // Toggle individual meal active state
        val firstMeal = allMeals.first()
        db.foodReminderDao().setReminderActive(firstMeal.id, false)

        val activeMeals = db.foodReminderDao().getActiveFoodReminders().first()
        assertEquals(9, activeMeals.size)
        assertFalse(activeMeals.any { it.id == firstMeal.id })
    }

    @Test
    fun testRoomReminderLogRecording() = runBlocking {
        val log = ReminderLog(
            category = ReminderCategory.FOOD,
            referenceId = 4L,
            title = "Lunch (Meal 4)",
            subtitle = "Completed scheduled lunch",
            scheduledDateIso = "2026-10-31",
            scheduledTime = "13:00",
            scheduledTimestamp = System.currentTimeMillis(),
            status = ReminderStatus.TAKEN
        )
        val id = db.reminderLogDao().insertLog(log)
        val fetched = db.reminderLogDao().getLogById(id)

        assertNotNull(fetched)
        assertEquals(ReminderStatus.TAKEN, fetched?.status)
    }

    @Test
    fun testMealEntityAndDao() = runBlocking {
        val meal = com.example.data.model.Meal(
            name = "Morning Breakfast",
            mealType = com.example.data.model.MealType.BREAKFAST,
            scheduledTime = "07:00",
            orderIndex = 1,
            isNextDay = false,
            daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
            recurrenceType = "DAILY",
            isActive = true,
            soundEnabled = true,
            vibrationEnabled = true,
            completionStatus = ReminderStatus.PENDING
        )
        val id = db.mealDao().insertMeal(meal)
        val fetched = db.mealDao().getMealById(id)
        assertNotNull(fetched)
        assertEquals("Morning Breakfast", fetched?.name)
        assertEquals(1, fetched?.orderIndex)

        // Mark as completed
        db.mealDao().updateCompletionStatus(id, ReminderStatus.TAKEN, "2026-10-31")
        val updated = db.mealDao().getMealById(id)
        assertEquals(ReminderStatus.TAKEN, updated?.completionStatus)
        assertEquals("2026-10-31", updated?.lastCompletedDateIso)
    }

    @Test
    fun testWaterRecordEntityAndDao() = runBlocking {
        val today = java.util.Date()
        val record = com.example.data.model.WaterRecord(
            dateIso = "2026-10-31",
            recordDate = today,
            scheduledTime = "10:00",
            intervalMinutes = 120,
            targetAmountMl = 2500,
            amountMl = 250,
            completionStatus = ReminderStatus.TAKEN,
            isGoalReached = false
        )
        val id = db.waterRecordDao().insertRecord(record)
        val fetched = db.waterRecordDao().getRecordById(id)
        assertNotNull(fetched)
        assertEquals(250, fetched?.amountMl)
        assertEquals(ReminderStatus.TAKEN, fetched?.completionStatus)

        val totalToday = db.waterRecordDao().getTotalIntakeForDate("2026-10-31").first()
        assertEquals(250, totalToday)
    }
}
