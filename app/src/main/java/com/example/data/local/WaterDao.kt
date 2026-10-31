package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WaterConfig
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_config WHERE id = 1 LIMIT 1")
    fun getWaterConfigFlow(): Flow<WaterConfig?>

    @Query("SELECT * FROM water_config WHERE id = 1 LIMIT 1")
    suspend fun getWaterConfig(): WaterConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWaterConfig(config: WaterConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog): Long

    @Query("SELECT * FROM water_logs WHERE dateIso = :dateIso ORDER BY timestamp DESC")
    fun getWaterLogsForDate(dateIso: String): Flow<List<WaterLog>>

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE dateIso = :dateIso")
    fun getTotalWaterForDate(dateIso: String): Flow<Int?>

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)

    @Query("DELETE FROM water_logs WHERE dateIso = :dateIso")
    suspend fun clearWaterLogsForDate(dateIso: String)
}
