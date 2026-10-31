package com.example.data.repository

import com.example.data.local.ReminderLogDao
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderLog
import com.example.data.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

class ReminderLogRepository(private val reminderLogDao: ReminderLogDao) {
    val allLogs: Flow<List<ReminderLog>> = reminderLogDao.getAllLogs()

    fun getLogsForDate(dateIso: String): Flow<List<ReminderLog>> = reminderLogDao.getLogsForDate(dateIso)

    fun getLogsForDateAndStatus(dateIso: String, status: ReminderStatus): Flow<List<ReminderLog>> =
        reminderLogDao.getLogsForDateAndStatus(dateIso, status)

    fun getLogsBetweenDates(startDateIso: String, endDateIso: String): Flow<List<ReminderLog>> =
        reminderLogDao.getLogsBetweenDates(startDateIso, endDateIso)

    fun getLogsByCategory(category: ReminderCategory): Flow<List<ReminderLog>> =
        reminderLogDao.getLogsByCategory(category)

    suspend fun getLogById(id: Long): ReminderLog? = reminderLogDao.getLogById(id)

    suspend fun recordOrUpdateLog(
        category: ReminderCategory,
        referenceId: Long,
        title: String,
        subtitle: String,
        scheduledDateIso: String,
        scheduledTime: String,
        scheduledTimestamp: Long,
        status: ReminderStatus,
        notes: String = ""
    ): Long {
        val existing = reminderLogDao.findLog(referenceId, scheduledDateIso, scheduledTime)
        return if (existing != null) {
            val updated = existing.copy(
                status = status,
                actionTimestamp = System.currentTimeMillis(),
                notes = if (notes.isNotBlank()) notes else existing.notes
            )
            reminderLogDao.updateLog(updated)
            existing.id
        } else {
            val newLog = ReminderLog(
                category = category,
                referenceId = referenceId,
                title = title,
                subtitle = subtitle,
                scheduledDateIso = scheduledDateIso,
                scheduledTime = scheduledTime,
                scheduledTimestamp = scheduledTimestamp,
                actionTimestamp = System.currentTimeMillis(),
                status = status,
                notes = notes
            )
            reminderLogDao.insertLog(newLog)
        }
    }

    suspend fun updateLog(log: ReminderLog) = reminderLogDao.updateLog(log)

    suspend fun deleteLog(log: ReminderLog) = reminderLogDao.deleteLog(log)

    suspend fun deleteLogById(id: Long) = reminderLogDao.deleteLogById(id)

    suspend fun clearAll() = reminderLogDao.clearAllLogs()
}
