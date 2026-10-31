package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.FoodReminder
import com.example.data.model.Medicine
import com.example.data.model.ReminderCategory
import com.example.data.model.WaterConfig
import com.example.reminder.receiver.AlarmReceiver
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }
    }

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (alarmManager == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
                    // Fallback to inexact if exact permission not granted on Android 12+
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.w("AlarmScheduler", "SecurityException scheduling alarm: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (_: Exception) {}
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error setting alarm", e)
        }
    }

    fun scheduleMedicine(medicine: Medicine) {
        if (!medicine.isActive) {
            cancelMedicine(medicine)
            return
        }

        medicine.reminderTimes.forEachIndexed { index, timeStr ->
            val nextMillis = DateTimeUtils.calculateNextTriggerMillis(
                time24 = timeStr,
                daysOfWeek = medicine.daysOfWeek
            )
            val requestCode = (medicine.id * 100 + index).toInt()

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(NotificationHelper.EXTRA_CATEGORY, ReminderCategory.MEDICINE.name)
                putExtra(NotificationHelper.EXTRA_REF_ID, medicine.id)
                putExtra(NotificationHelper.EXTRA_TITLE, medicine.name)
                val sub = "${medicine.dosage} • ${medicine.foodRelation.labelEn} • ${medicine.instructions}".trim(' ', '•')
                putExtra(NotificationHelper.EXTRA_SUBTITLE, sub)
                putExtra(NotificationHelper.EXTRA_TIME, timeStr)
                putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setAlarm(nextMillis, pendingIntent)
        }
    }

    fun cancelMedicine(medicine: Medicine) {
        medicine.reminderTimes.forEachIndexed { index, _ ->
            val requestCode = (medicine.id * 100 + index).toInt()
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager?.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun scheduleFood(food: FoodReminder) {
        if (!food.isActive) {
            cancelFood(food)
            return
        }

        val nextMillis = DateTimeUtils.calculateNextTriggerMillis(
            time24 = food.time,
            daysOfWeek = food.daysOfWeek
        )
        val requestCode = (50000 + food.id).toInt()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_CATEGORY, ReminderCategory.FOOD.name)
            putExtra(NotificationHelper.EXTRA_REF_ID, food.id)
            putExtra(NotificationHelper.EXTRA_TITLE, food.displayName)
            putExtra(NotificationHelper.EXTRA_SUBTITLE, if (food.notes.isNotBlank()) food.notes else "Time for your scheduled meal")
            putExtra(NotificationHelper.EXTRA_TIME, food.time)
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(nextMillis, pendingIntent)
    }

    fun cancelFood(food: FoodReminder) {
        val requestCode = (50000 + food.id).toInt()
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleWater(waterConfig: WaterConfig) {
        cancelWater()
        if (!waterConfig.isEnabled) return

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)

        // Schedule next interval reminder between startHour and endHour
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (currentHour < waterConfig.startHour) {
            targetCal.set(Calendar.HOUR_OF_DAY, waterConfig.startHour)
            targetCal.set(Calendar.MINUTE, 0)
        } else if (currentHour >= waterConfig.endHour) {
            targetCal.add(Calendar.DAY_OF_YEAR, 1)
            targetCal.set(Calendar.HOUR_OF_DAY, waterConfig.startHour)
            targetCal.set(Calendar.MINUTE, 0)
        } else {
            targetCal.add(Calendar.MINUTE, waterConfig.intervalMinutes)
        }

        val requestCode = 99999
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_CATEGORY, ReminderCategory.WATER.name)
            putExtra(NotificationHelper.EXTRA_REF_ID, 1L)
            putExtra(NotificationHelper.EXTRA_TITLE, "Drink Water (পানি পান করুন)")
            putExtra(NotificationHelper.EXTRA_SUBTITLE, "Target: ${waterConfig.glassSizeMl}ml to reach daily goal ${waterConfig.dailyGoalMl}ml")
            putExtra(NotificationHelper.EXTRA_TIME, "${targetCal.get(Calendar.HOUR_OF_DAY)}:${targetCal.get(Calendar.MINUTE)}")
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(targetCal.timeInMillis, pendingIntent)
    }

    fun cancelWater() {
        val requestCode = 99999
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleSnooze(
        category: ReminderCategory,
        refId: Long,
        title: String,
        subtitle: String,
        minutes: Int = 10
    ) {
        val triggerAtMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val requestCode = (80000 + (refId % 10000)).toInt()

        val cal = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        val timeStr = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_CATEGORY, category.name)
            putExtra(NotificationHelper.EXTRA_REF_ID, refId)
            putExtra(NotificationHelper.EXTRA_TITLE, title)
            putExtra(NotificationHelper.EXTRA_SUBTITLE, "$subtitle (Snoozed $minutes mins)")
            putExtra(NotificationHelper.EXTRA_TIME, timeStr)
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(triggerAtMillis, pendingIntent)
    }

    suspend fun rescheduleAll(database: AppDatabase) {
        // Reschedule active medicines
        val activeMeds = database.medicineDao().getActiveMedicines().firstOrNull() ?: emptyList()
        activeMeds.forEach { scheduleMedicine(it) }

        // Reschedule active meals
        val activeMeals = database.foodReminderDao().getActiveFoodReminders().firstOrNull() ?: emptyList()
        activeMeals.forEach { scheduleFood(it) }

        // Reschedule water
        val waterConfig = database.waterDao().getWaterConfig()
        if (waterConfig != null && waterConfig.isEnabled) {
            scheduleWater(waterConfig)
        }
    }
}
