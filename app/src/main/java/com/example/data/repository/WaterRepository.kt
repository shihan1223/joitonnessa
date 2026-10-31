package com.example.data.repository

import com.example.data.local.WaterDao
import com.example.data.model.WaterConfig
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

class WaterRepository(private val waterDao: WaterDao) {
    val waterConfigFlow: Flow<WaterConfig?> = waterDao.getWaterConfigFlow()

    suspend fun getWaterConfig(): WaterConfig = waterDao.getWaterConfig() ?: WaterConfig()

    suspend fun saveWaterConfig(config: WaterConfig) = waterDao.saveWaterConfig(config)

    suspend fun logWater(dateIso: String, amountMl: Int): Long {
        return waterDao.insertWaterLog(
            WaterLog(
                dateIso = dateIso,
                timestamp = System.currentTimeMillis(),
                amountMl = amountMl
            )
        )
    }

    fun getWaterLogsForDate(dateIso: String): Flow<List<WaterLog>> = waterDao.getWaterLogsForDate(dateIso)

    fun getTotalWaterForDate(dateIso: String): Flow<Int?> = waterDao.getTotalWaterForDate(dateIso)

    suspend fun deleteWaterLog(id: Long) = waterDao.deleteWaterLogById(id)

    suspend fun clearWaterLogsForDate(dateIso: String) = waterDao.clearWaterLogsForDate(dateIso)
}
