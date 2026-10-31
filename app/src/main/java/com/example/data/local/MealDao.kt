package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Meal
import com.example.data.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY orderIndex ASC, scheduledTime ASC")
    fun getAllMeals(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE isActive = 1 ORDER BY orderIndex ASC, scheduledTime ASC")
    fun getActiveMeals(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getMealById(id: Long): Meal?

    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    fun getMealByIdFlow(id: Long): Flow<Meal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<Meal>)

    @Update
    suspend fun updateMeal(meal: Meal)

    @Query("UPDATE meals SET completionStatus = :status, lastCompletedDateIso = :dateIso WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, status: ReminderStatus, dateIso: String)

    @Query("UPDATE meals SET isActive = :isActive WHERE id = :id")
    suspend fun setMealActive(id: Long, isActive: Boolean)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()
}
