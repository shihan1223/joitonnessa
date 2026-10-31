package com.example

import com.example.data.local.AppDatabase
import com.example.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBengaliDigitConversion() {
        val input = "1234567890"
        val expected = "১২৩৪৫৬৭৮৯০"
        assertEquals(expected, DateTimeUtils.toBengaliDigits(input))
    }

    @Test
    fun test12HourTimeFormatting() {
        val time24 = "14:30"
        val formatted = DateTimeUtils.format12Hour(time24, "en")
        assertTrue(formatted.contains("02:30") || formatted.contains("2:30"))
        assertTrue(formatted.contains("PM", ignoreCase = true))
    }

    @Test
    fun testNextTriggerCalculationIsFuture() {
        val nextTrigger = DateTimeUtils.calculateNextTriggerMillis(
            time24 = "23:59",
            daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7)
        )
        assertTrue(nextTrigger >= System.currentTimeMillis())
    }

    @Test
    fun testTenMealScheduleTimesAndMidnightCrossing() {
        val schedule = AppDatabase.createDefaultTenMealSchedule()
        assertEquals(10, schedule.size)

        // Verify start time is 07:00 AM
        assertEquals("07:00", schedule[0].time)
        assertEquals(1, schedule[0].orderIndex)

        // Verify each step is 2 hours
        val expectedTimes = listOf(
            "07:00", // 1. 07:00 AM
            "09:00", // 2. 09:00 AM
            "11:00", // 3. 11:00 AM
            "13:00", // 4. 01:00 PM
            "15:00", // 5. 03:00 PM
            "17:00", // 6. 05:00 PM
            "19:00", // 7. 07:00 PM
            "21:00", // 8. 09:00 PM
            "23:00", // 9. 11:00 PM
            "01:00"  // 10. 01:00 AM (next day)
        )

        for (i in 0 until 10) {
            assertEquals(expectedTimes[i], schedule[i].time)
            assertEquals(i + 1, schedule[i].orderIndex)
        }

        // Verify the 10th item crosses midnight
        assertTrue(schedule[9].isNextDay)
    }
}
