package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.model.ReminderCategory
import com.example.reminder.receiver.NotificationActionReceiver
import com.example.ui.alarm.AlarmRingingActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_MEDICINE = "channel_medicine_reminders"
        const val CHANNEL_FOOD = "channel_food_reminders"
        const val CHANNEL_WATER = "channel_water_reminders"

        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_REF_ID = "extra_ref_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_DATE_ISO = "extra_date_iso"

        const val ACTION_TAKEN = "com.example.reminder.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.example.reminder.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.example.reminder.ACTION_SKIP"
        const val ACTION_DISMISS = "com.example.reminder.ACTION_DISMISS"
    }

    init {
        createChannels()
    }

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            // Medicine Channel (High Priority)
            val medicineChannel = NotificationChannel(
                CHANNEL_MEDICINE,
                "Medicine Reminders (ঔষধের রিমাইন্ডার)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timely alarms and notifications for scheduled medicines"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Food Channel
            val foodChannel = NotificationChannel(
                CHANNEL_FOOD,
                "Food & Meal Reminders (খাদ্য ও নাস্তা)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for breakfast, lunch, dinner and snacks"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(defaultSoundUri, audioAttributes)
            }

            // Water Channel
            val waterChannel = NotificationChannel(
                CHANNEL_WATER,
                "Water Hydration (পানি পান)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Hydration interval prompts and daily goal reminders"
            }

            notificationManager.createNotificationChannel(medicineChannel)
            notificationManager.createNotificationChannel(foodChannel)
            notificationManager.createNotificationChannel(waterChannel)
        }
    }

    fun showReminderNotification(
        notificationId: Int,
        category: ReminderCategory,
        refId: Long,
        title: String,
        subtitle: String,
        time: String,
        dateIso: String,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) {
        val channelId = when (category) {
            ReminderCategory.MEDICINE -> CHANNEL_MEDICINE
            ReminderCategory.FOOD -> CHANNEL_FOOD
            ReminderCategory.WATER -> CHANNEL_WATER
        }

        // Full Screen or Click Intent to open interactive AlarmRingingActivity
        val fullScreenIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_CATEGORY, category.name)
            putExtra(EXTRA_REF_ID, refId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_SUBTITLE, subtitle)
            putExtra(EXTRA_TIME, time)
            putExtra(EXTRA_DATE_ISO, dateIso)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Taken
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_CATEGORY, category.name)
            putExtra(EXTRA_REF_ID, refId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_SUBTITLE, subtitle)
            putExtra(EXTRA_TIME, time)
            putExtra(EXTRA_DATE_ISO, dateIso)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze (10 mins)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_CATEGORY, category.name)
            putExtra(EXTRA_REF_ID, refId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_SUBTITLE, subtitle)
            putExtra(EXTRA_TIME, time)
            putExtra(EXTRA_DATE_ISO, dateIso)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skip
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SKIP
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_CATEGORY, category.name)
            putExtra(EXTRA_REF_ID, refId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_SUBTITLE, subtitle)
            putExtra(EXTRA_TIME, time)
            putExtra(EXTRA_DATE_ISO, dateIso)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 3,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headerPrefix = when (category) {
            ReminderCategory.MEDICINE -> "💊 "
            ReminderCategory.FOOD -> "🍽️ "
            ReminderCategory.WATER -> "💧 "
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("$headerPrefix$title")
            .setContentText(subtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$subtitle\nScheduled: $time"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(false)
            .addAction(0, "Taken (খেয়েছি)", takenPendingIntent)
            .addAction(0, "Snooze 10m", snoozePendingIntent)
            .addAction(0, "Skip (বাদ)", skipPendingIntent)

        if (!vibrationEnabled) {
            builder.setVibrate(longArrayOf(0))
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission not yet granted
        }
    }

    fun cancelNotification(notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
