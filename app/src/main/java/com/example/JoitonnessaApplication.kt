package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.FoodReminderRepository
import com.example.data.repository.MedicineRepository
import com.example.data.repository.ReminderLogRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.WaterRepository
import com.example.reminder.AlarmScheduler
import com.example.reminder.NotificationHelper

class JoitonnessaApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val medicineRepository: MedicineRepository by lazy {
        MedicineRepository(database.medicineDao())
    }

    val foodReminderRepository: FoodReminderRepository by lazy {
        FoodReminderRepository(database.foodReminderDao())
    }

    val waterRepository: WaterRepository by lazy {
        WaterRepository(database.waterDao())
    }

    val reminderLogRepository: ReminderLogRepository by lazy {
        ReminderLogRepository(database.reminderLogDao())
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    val alarmScheduler: AlarmScheduler by lazy {
        AlarmScheduler(this)
    }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(this)
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
    }
}
