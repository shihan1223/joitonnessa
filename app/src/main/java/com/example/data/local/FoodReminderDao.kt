package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FoodReminder
import com.example.data.model.FoodScheduleConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodReminderDao {
    @Query("SELECT * FROM food_reminders ORDER BY orderIndex ASC, time ASC")
    fun getAllFoodReminders(): Flow<List<FoodReminder>>

    @Query("SELECT * FROM food_reminders WHERE isActive = 1 ORDER BY orderIndex ASC, time ASC")
    fun getActiveFoodReminders(): Flow<List<FoodReminder>>

    @Query("SELECT * FROM food_reminders WHERE id = :id LIMIT 1")
    suspend fun getFoodReminderById(id: Long): FoodReminder?

    @Query("SELECT * FROM food_reminders WHERE id = :id LIMIT 1")
    fun getFoodReminderByIdFlow(id: Long): Flow<FoodReminder?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodReminder(reminder: FoodReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<FoodReminder>)

    @Update
    suspend fun updateFoodReminder(reminder: FoodReminder)

    @Query("UPDATE food_reminders SET isActive = :isActive WHERE id = :id")
    suspend fun setReminderActive(id: Long, isActive: Boolean)

    @Delete
    suspend fun deleteFoodReminder(reminder: FoodReminder)

    @Query("DELETE FROM food_reminders WHERE id = :id")
    suspend fun deleteFoodReminderById(id: Long)

    @Query("DELETE FROM food_reminders")
    suspend fun deleteAllFoodReminders()

    @Query("SELECT COUNT(*) FROM food_reminders")
    suspend fun getCount(): Int

    // Food Schedule Generator Configuration
    @Query("SELECT * FROM food_schedule_config WHERE id = 1 LIMIT 1")
    fun getScheduleConfigFlow(): Flow<FoodScheduleConfig?>

    @Query("SELECT * FROM food_schedule_config WHERE id = 1 LIMIT 1")
    suspend fun getScheduleConfig(): FoodScheduleConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScheduleConfig(config: FoodScheduleConfig)
}
