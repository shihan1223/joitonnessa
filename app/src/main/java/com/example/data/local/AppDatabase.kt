package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FoodReminder
import com.example.data.model.FoodScheduleConfig
import com.example.data.model.MealType
import com.example.data.model.Medicine
import com.example.data.model.ReminderLog
import com.example.data.model.WaterConfig
import com.example.data.model.WaterLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.model.Meal
import com.example.data.model.WaterRecord

@Database(
    entities = [
        Medicine::class,
        FoodReminder::class,
        FoodScheduleConfig::class,
        Meal::class,
        WaterConfig::class,
        WaterLog::class,
        WaterRecord::class,
        ReminderLog::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun foodReminderDao(): FoodReminderDao
    abstract fun mealDao(): MealDao
    abstract fun waterDao(): WaterDao
    abstract fun waterRecordDao(): WaterRecordDao
    abstract fun reminderLogDao(): ReminderLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "joitonnessa_database"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                seedDefaults(database)
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                checkAndSeedTenMealSchedule(database)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun checkAndSeedTenMealSchedule(database: AppDatabase) {
            val foodDao = database.foodReminderDao()
            if (foodDao.getCount() == 0) {
                seedDefaults(database)
            }
        }

        suspend fun seedDefaults(database: AppDatabase) {
            val foodDao = database.foodReminderDao()
            val waterDao = database.waterDao()

            if (foodDao.getCount() == 0) {
                val tenMealSchedule = createDefaultTenMealSchedule()
                foodDao.insertAll(tenMealSchedule)
                foodDao.saveScheduleConfig(
                    FoodScheduleConfig(
                        id = 1,
                        startTime = "07:00",
                        intervalHours = 2,
                        totalReminders = 10,
                        isEnabled = true
                    )
                )
            }

            if (waterDao.getWaterConfig() == null) {
                waterDao.saveWaterConfig(WaterConfig())
            }
        }

        fun createDefaultTenMealSchedule(): List<FoodReminder> {
            val defaultTimes = listOf(
                Pair("07:00", "Meal 1 (07:00 AM - সকালের প্রথম খাবার)"),
                Pair("09:00", "Meal 2 (09:00 AM - সকালের নাস্তা)"),
                Pair("11:00", "Meal 3 (11:00 AM - দুপুর পূর্ব খাবার)"),
                Pair("13:00", "Meal 4 (01:00 PM - দুপুরের প্রধান খাবার)"),
                Pair("15:00", "Meal 5 (03:00 PM - দুপুর পরবর্তী খাবার)"),
                Pair("17:00", "Meal 6 (05:00 PM - বিকালের নাস্তা)"),
                Pair("19:00", "Meal 7 (07:00 PM - সন্ধ্যার খাবার)"),
                Pair("21:00", "Meal 8 (09:00 PM - রাতের প্রধান খাবার)"),
                Pair("23:00", "Meal 9 (11:00 PM - ঘুমানোর আগের খাবার)"),
                Pair("01:00", "Meal 10 (01:00 AM - দেরি রাতের খাবার / পরবর্তী দিন)")
            )

            return defaultTimes.mapIndexed { index, pair ->
                FoodReminder(
                    mealType = when (index) {
                        0 -> MealType.BREAKFAST
                        1 -> MealType.BREAKFAST
                        2 -> MealType.SNACKS
                        3 -> MealType.LUNCH
                        4 -> MealType.SNACKS
                        5 -> MealType.SNACKS
                        6 -> MealType.DINNER
                        7 -> MealType.DINNER
                        8 -> MealType.SNACKS
                        else -> MealType.OTHER
                    },
                    customName = pair.second,
                    time = pair.first,
                    orderIndex = index + 1,
                    isNextDay = index == 9, // 01:00 AM crosses midnight
                    daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                    isActive = true,
                    soundEnabled = true,
                    vibrationEnabled = true,
                    notes = "Scheduled eating reminder #${index + 1}"
                )
            }
        }
    }
}
