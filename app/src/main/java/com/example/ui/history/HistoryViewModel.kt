package com.example.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderLog
import com.example.data.model.ReminderStatus
import com.example.data.repository.ReminderLogRepository
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayReport(
    val dateIso: String,
    val dayLabel: String,
    val takenCount: Int,
    val totalCount: Int,
    val adherencePercent: Int
)

data class ReportsData(
    val weeklyReports: List<DayReport> = emptyList(),
    val totalTaken: Int = 0,
    val totalSkipped: Int = 0,
    val totalMissed: Int = 0,
    val overallAdherence: Int = 0
)

class HistoryViewModel(
    private val reminderLogRepository: ReminderLogRepository
) : ViewModel() {

    private val _selectedDateIso = MutableStateFlow(DateTimeUtils.todayIso())
    val selectedDateIso: StateFlow<String> = _selectedDateIso.asStateFlow()

    private val _statusFilter = MutableStateFlow<ReminderStatus?>(null)
    val statusFilter: StateFlow<ReminderStatus?> = _statusFilter.asStateFlow()

    val logsForSelectedDate: StateFlow<List<ReminderLog>> = _selectedDateIso
        .flatMapLatest { dateIso ->
            reminderLogRepository.getLogsForDate(dateIso)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<ReminderLog>> = reminderLogRepository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(dateIso: String) {
        _selectedDateIso.value = dateIso
    }

    fun setStatusFilter(status: ReminderStatus?) {
        _statusFilter.value = status
    }

    fun updateLogStatus(log: ReminderLog, newStatus: ReminderStatus) {
        viewModelScope.launch {
            val updated = log.copy(status = newStatus, actionTimestamp = System.currentTimeMillis())
            reminderLogRepository.updateLog(updated)
        }
    }

    fun deleteLog(log: ReminderLog) {
        viewModelScope.launch {
            reminderLogRepository.deleteLog(log)
        }
    }

    fun getWeeklyReports(language: String): StateFlow<ReportsData> {
        val past7Days = DateTimeUtils.getPastDaysIso(7)
        return allLogs.combine(_selectedDateIso) { logs, _ ->
            var sumTaken = 0
            var sumSkipped = 0
            var sumMissed = 0

            val dayReports = past7Days.map { dateIso ->
                val dayLogs = logs.filter { it.scheduledDateIso == dateIso }
                val taken = dayLogs.count { it.status == ReminderStatus.TAKEN }
                val skipped = dayLogs.count { it.status == ReminderStatus.SKIPPED }
                val missed = dayLogs.count { it.status == ReminderStatus.MISSED }
                val total = dayLogs.size

                sumTaken += taken
                sumSkipped += skipped
                sumMissed += missed

                val percent = if (total > 0) ((taken.toFloat() / total.toFloat()) * 100).toInt() else 0
                val dayName = try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    val date = sdf.parse(dateIso) ?: java.util.Date()
                    val outSdf = java.text.SimpleDateFormat("EEE", java.util.Locale.US)
                    outSdf.format(date)
                } catch (_: Exception) {
                    dateIso.takeLast(2)
                }

                DayReport(
                    dateIso = dateIso,
                    dayLabel = dayName,
                    takenCount = taken,
                    totalCount = total,
                    adherencePercent = percent
                )
            }

            val grandTotal = sumTaken + sumSkipped + sumMissed
            val adherence = if (grandTotal > 0) ((sumTaken.toFloat() / grandTotal.toFloat()) * 100).toInt() else 0

            ReportsData(
                weeklyReports = dayReports,
                totalTaken = sumTaken,
                totalSkipped = sumSkipped,
                totalMissed = sumMissed,
                overallAdherence = adherence
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReportsData()
        )
    }

    class Factory(
        private val repo: ReminderLogRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repo) as T
        }
    }
}
