package com.example.ui.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.WaterConfig
import com.example.data.model.WaterLog
import com.example.data.repository.WaterRepository
import com.example.reminder.AlarmScheduler
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WaterUiState(
    val config: WaterConfig = WaterConfig(),
    val todayWaterMl: Int = 0,
    val logs: List<WaterLog> = emptyList(),
    val isEnabled: Boolean = true
)

class WaterViewModel(
    private val waterRepository: WaterRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val todayIso = DateTimeUtils.todayIso()

    val uiState: StateFlow<WaterUiState> = combine(
        waterRepository.waterConfigFlow,
        waterRepository.getTotalWaterForDate(todayIso),
        waterRepository.getWaterLogsForDate(todayIso)
    ) { cfg, total, logList ->
        val config = cfg ?: WaterConfig()
        WaterUiState(
            config = config,
            todayWaterMl = total ?: 0,
            logs = logList,
            isEnabled = config.isEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WaterUiState()
    )

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            waterRepository.logWater(todayIso, amountMl)
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            waterRepository.deleteWaterLog(id)
        }
    }

    fun updateGoal(newGoalMl: Int) {
        viewModelScope.launch {
            val current = waterRepository.getWaterConfig()
            val updated = current.copy(dailyGoalMl = newGoalMl)
            waterRepository.saveWaterConfig(updated)
            alarmScheduler.scheduleWater(updated)
        }
    }

    fun updateInterval(minutes: Int) {
        viewModelScope.launch {
            val current = waterRepository.getWaterConfig()
            val updated = current.copy(intervalMinutes = minutes)
            waterRepository.saveWaterConfig(updated)
            alarmScheduler.scheduleWater(updated)
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = waterRepository.getWaterConfig()
            val updated = current.copy(isEnabled = enabled)
            waterRepository.saveWaterConfig(updated)
            if (enabled) {
                alarmScheduler.scheduleWater(updated)
            } else {
                alarmScheduler.cancelWater()
            }
        }
    }

    class Factory(
        private val waterRepo: WaterRepository,
        private val scheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WaterViewModel(waterRepo, scheduler) as T
        }
    }
}
