package com.example.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.AppDatabase
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderStatus
import com.example.reminder.AlarmScheduler
import com.example.reminder.NotificationHelper
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 0)
        val categoryStr = intent.getStringExtra(NotificationHelper.EXTRA_CATEGORY) ?: ReminderCategory.MEDICINE.name
        val category = try { ReminderCategory.valueOf(categoryStr) } catch (_: Exception) { ReminderCategory.MEDICINE }
        val refId = intent.getLongExtra(NotificationHelper.EXTRA_REF_ID, 0L)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: ""
        val subtitle = intent.getStringExtra(NotificationHelper.EXTRA_SUBTITLE) ?: ""
        val time = intent.getStringExtra(NotificationHelper.EXTRA_TIME) ?: "08:00"
        val dateIso = intent.getStringExtra(NotificationHelper.EXTRA_DATE_ISO) ?: DateTimeUtils.todayIso()

        val notificationHelper = NotificationHelper(context)
        notificationHelper.cancelNotification(notificationId)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                when (action) {
                    NotificationHelper.ACTION_TAKEN -> {
                        val existing = db.reminderLogDao().findLog(refId, dateIso, time)
                        if (existing != null) {
                            db.reminderLogDao().updateLog(
                                existing.copy(
                                    status = ReminderStatus.TAKEN,
                                    actionTimestamp = System.currentTimeMillis()
                                )
                            )
                        } else {
                            db.reminderLogDao().insertLog(
                                com.example.data.model.ReminderLog(
                                    category = category,
                                    referenceId = refId,
                                    title = title,
                                    subtitle = subtitle,
                                    scheduledDateIso = dateIso,
                                    scheduledTime = time,
                                    scheduledTimestamp = System.currentTimeMillis(),
                                    actionTimestamp = System.currentTimeMillis(),
                                    status = ReminderStatus.TAKEN
                                )
                            )
                        }
                    }

                    NotificationHelper.ACTION_SKIP -> {
                        val existing = db.reminderLogDao().findLog(refId, dateIso, time)
                        if (existing != null) {
                            db.reminderLogDao().updateLog(
                                existing.copy(
                                    status = ReminderStatus.SKIPPED,
                                    actionTimestamp = System.currentTimeMillis()
                                )
                            )
                        } else {
                            db.reminderLogDao().insertLog(
                                com.example.data.model.ReminderLog(
                                    category = category,
                                    referenceId = refId,
                                    title = title,
                                    subtitle = subtitle,
                                    scheduledDateIso = dateIso,
                                    scheduledTime = time,
                                    scheduledTimestamp = System.currentTimeMillis(),
                                    actionTimestamp = System.currentTimeMillis(),
                                    status = ReminderStatus.SKIPPED
                                )
                            )
                        }
                    }

                    NotificationHelper.ACTION_SNOOZE -> {
                        val scheduler = AlarmScheduler(context)
                        scheduler.scheduleSnooze(
                            category = category,
                            refId = refId,
                            title = title,
                            subtitle = subtitle,
                            minutes = 10
                        )
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
