package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ReminderStatus
import com.example.data.model.WaterRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterRecordDao {
    @Query("SELECT * FROM water_records ORDER BY timestamp DESC")
    fun getAllWaterRecords(): Flow<List<WaterRecord>>

    @Query("SELECT * FROM water_records WHERE dateIso = :dateIso ORDER BY timestamp DESC")
    fun getRecordsForDate(dateIso: String): Flow<List<WaterRecord>>

    @Query("SELECT SUM(amountMl) FROM water_records WHERE dateIso = :dateIso")
    fun getTotalIntakeForDate(dateIso: String): Flow<Int?>

    @Query("SELECT * FROM water_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): WaterRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WaterRecord): Long

    @Update
    suspend fun updateRecord(record: WaterRecord)

    @Query("UPDATE water_records SET completionStatus = :status, isGoalReached = :isGoalReached WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, status: ReminderStatus, isGoalReached: Boolean)

    @Delete
    suspend fun deleteRecord(record: WaterRecord)

    @Query("DELETE FROM water_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM water_records WHERE dateIso = :dateIso")
    suspend fun clearRecordsForDate(dateIso: String)
}
