package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val time24Format = SimpleDateFormat("HH:mm", Locale.US)
    private val time12Format = SimpleDateFormat("hh:mm a", Locale.US)
    private val readableDateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)

    fun todayIso(): String = isoDateFormat.format(Date())

    fun currentTimestamp(): Long = System.currentTimeMillis()

    fun formatIsoDate(date: Date): String = isoDateFormat.format(date)

    fun formatReadableDate(dateIso: String, language: String = "bn"): String {
        return try {
            val date = isoDateFormat.parse(dateIso) ?: Date()
            val base = readableDateFormat.format(date)
            if (language == "bn") toBengaliDigits(base) else base
        } catch (_: Exception) {
            dateIso
        }
    }

    fun format12Hour(time24: String, language: String = "bn"): String {
        return try {
            val date = time24Format.parse(time24) ?: return time24
            val formatted = time12Format.format(date)
            if (language == "bn") toBengaliDigits(formatted) else formatted
        } catch (_: Exception) {
            time24
        }
    }

    fun toBengaliDigits(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = java.lang.StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Converts Calendar DAY_OF_WEEK to ISO-8601 day of week:
     * Monday = 1, Tuesday = 2, ... Sunday = 7
     */
    fun currentDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        return when (dow) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    /**
     * Calculates the next epoch millis for an alarm at [time24] (HH:mm) given allowed [daysOfWeek] (1=Mon..7=Sun).
     */
    fun calculateNextTriggerMillis(
        time24: String,
        daysOfWeek: List<Int>,
        allowTodayPast: Boolean = false
    ): Long {
        val parts = time24.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // Set today's target time
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val validDays = if (daysOfWeek.isEmpty()) listOf(1, 2, 3, 4, 5, 6, 7) else daysOfWeek

        // If today's time has already passed and we don't allow past triggers, advance by 1 day
        if (cal.timeInMillis <= now && !allowTodayPast) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Loop forward up to 8 days to find the next day matching validDays
        for (i in 0..7) {
            val dayOfWeekIso = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
            if (validDays.contains(dayOfWeekIso) && cal.timeInMillis > now) {
                return cal.timeInMillis
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return cal.timeInMillis
    }

    fun getPastDaysIso(count: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0 until count) {
            list.add(isoDateFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return list.reversed()
    }
}
