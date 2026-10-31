package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderLog
import com.example.data.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {
    @Query("SELECT * FROM reminder_logs ORDER BY scheduledTimestamp DESC")
    fun getAllLogs(): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE scheduledDateIso = :dateIso ORDER BY scheduledTime ASC")
    fun getLogsForDate(dateIso: String): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE scheduledDateIso = :dateIso AND status = :status ORDER BY scheduledTime ASC")
    fun getLogsForDateAndStatus(dateIso: String, status: ReminderStatus): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE category = :category ORDER BY scheduledTimestamp DESC")
    fun getLogsByCategory(category: ReminderCategory): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE scheduledDateIso BETWEEN :startDateIso AND :endDateIso ORDER BY scheduledTimestamp ASC")
    fun getLogsBetweenDates(startDateIso: String, endDateIso: String): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): ReminderLog?

    @Query("SELECT * FROM reminder_logs WHERE referenceId = :referenceId AND scheduledDateIso = :dateIso AND scheduledTime = :time LIMIT 1")
    suspend fun findLog(referenceId: Long, dateIso: String, time: String): ReminderLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReminderLog): Long

    @Update
    suspend fun updateLog(log: ReminderLog)

    @Delete
    suspend fun deleteLog(log: ReminderLog)

    @Query("DELETE FROM reminder_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM reminder_logs")
    suspend fun clearAllLogs()
}
