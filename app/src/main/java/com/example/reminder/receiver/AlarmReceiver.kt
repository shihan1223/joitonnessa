package com.example.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.data.local.AppDatabase
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderStatus
import com.example.reminder.AlarmScheduler
import com.example.reminder.NotificationHelper
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "joitonnessa:AlarmReceiverWakeLock"
        )
        wakeLock?.acquire(10000L) // 10 seconds timeout

        val categoryStr = intent.getStringExtra(NotificationHelper.EXTRA_CATEGORY) ?: ReminderCategory.MEDICINE.name
        val category = try { ReminderCategory.valueOf(categoryStr) } catch (_: Exception) { ReminderCategory.MEDICINE }
        val refId = intent.getLongExtra(NotificationHelper.EXTRA_REF_ID, 0L)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Reminder"
        val subtitle = intent.getStringExtra(NotificationHelper.EXTRA_SUBTITLE) ?: ""
        val time = intent.getStringExtra(NotificationHelper.EXTRA_TIME) ?: "08:00"
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, (System.currentTimeMillis() % 100000).toInt())
        val todayIso = DateTimeUtils.todayIso()

        // 1. Show notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showReminderNotification(
            notificationId = notificationId,
            category = category,
            refId = refId,
            title = title,
            subtitle = subtitle,
            time = time,
            dateIso = todayIso
        )

        // 2. Database recording and rescheduling next occurrences
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)

                // Log as PENDING today if not already recorded
                val existing = db.reminderLogDao().findLog(refId, todayIso, time)
                if (existing == null && refId > 0) {
                    db.reminderLogDao().insertLog(
                        com.example.data.model.ReminderLog(
                            category = category,
                            referenceId = refId,
                            title = title,
                            subtitle = subtitle,
                            scheduledDateIso = todayIso,
                            scheduledTime = time,
                            scheduledTimestamp = System.currentTimeMillis(),
                            status = ReminderStatus.PENDING
                        )
                    )
                }

                // Reschedule next cycle
                val scheduler = AlarmScheduler(context)
                when (category) {
                    ReminderCategory.MEDICINE -> {
                        val med = db.medicineDao().getMedicineById(refId)
                        if (med != null && med.isActive) {
                            scheduler.scheduleMedicine(med)
                        }
                    }
                    ReminderCategory.FOOD -> {
                        val food = db.foodReminderDao().getFoodReminderById(refId)
                        if (food != null && food.isActive) {
                            scheduler.scheduleFood(food)
                        }
                    }
                    ReminderCategory.WATER -> {
                        val waterConfig = db.waterDao().getWaterConfig()
                        if (waterConfig != null && waterConfig.isEnabled) {
                            scheduler.scheduleWater(waterConfig)
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                wakeLock?.release()
                pendingResult.finish()
            }
        }
    }
}
