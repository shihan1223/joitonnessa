package com.example.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FoodReminder
import com.example.data.model.FoodScheduleConfig
import com.example.data.model.MealType
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderStatus
import com.example.data.repository.FoodReminderRepository
import com.example.data.repository.ReminderLogRepository
import com.example.reminder.AlarmScheduler
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditFoodState(
    val id: Long = 0,
    val mealType: MealType = MealType.OTHER,
    val customName: String = "",
    val time: String = "07:00",
    val orderIndex: Int = 1,
    val isNextDay: Boolean = false,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    val isActive: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notes: String = "",
    val errorMessage: String? = null
)

class FoodViewModel(
    private val foodReminderRepository: FoodReminderRepository,
    private val reminderLogRepository: ReminderLogRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val allFoodReminders: StateFlow<List<FoodReminder>> = foodReminderRepository.allFoodReminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val scheduleConfig: StateFlow<FoodScheduleConfig> = foodReminderRepository.scheduleConfigFlow
        .map { it ?: FoodScheduleConfig() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodScheduleConfig()
        )

    private val _addEditState = MutableStateFlow(AddEditFoodState())
    val addEditState: StateFlow<AddEditFoodState> = _addEditState.asStateFlow()

    fun loadFoodReminder(id: Long) {
        if (id <= 0) {
            _addEditState.value = AddEditFoodState()
            return
        }
        viewModelScope.launch {
            val food = foodReminderRepository.getFoodReminderById(id)
            if (food != null) {
                _addEditState.value = AddEditFoodState(
                    id = food.id,
                    mealType = food.mealType,
                    customName = food.customName,
                    time = food.time,
                    orderIndex = food.orderIndex,
                    isNextDay = food.isNextDay,
                    daysOfWeek = food.daysOfWeek,
                    isActive = food.isActive,
                    soundEnabled = food.soundEnabled,
                    vibrationEnabled = food.vibrationEnabled,
                    notes = food.notes
                )
            }
        }
    }

    fun updateMealType(type: MealType) {
        _addEditState.value = _addEditState.value.copy(mealType = type)
    }

    fun updateCustomName(name: String) {
        _addEditState.value = _addEditState.value.copy(customName = name)
    }

    fun updateTime(timeStr: String) {
        _addEditState.value = _addEditState.value.copy(time = timeStr)
    }

    fun updateNotes(notes: String) {
        _addEditState.value = _addEditState.value.copy(notes = notes)
    }

    fun toggleSound(enabled: Boolean) {
        _addEditState.value = _addEditState.value.copy(soundEnabled = enabled)
    }

    fun toggleVibration(enabled: Boolean) {
        _addEditState.value = _addEditState.value.copy(vibrationEnabled = enabled)
    }

    fun toggleDay(day: Int) {
        val days = _addEditState.value.daysOfWeek.toMutableList()
        if (days.contains(day)) {
            if (days.size > 1) days.remove(day)
        } else {
            days.add(day)
            days.sort()
        }
        _addEditState.value = _addEditState.value.copy(daysOfWeek = days)
    }

    fun toggleReminderActive(reminder: FoodReminder, isActive: Boolean) {
        viewModelScope.launch {
            val updated = reminder.copy(isActive = isActive)
            foodReminderRepository.updateFoodReminder(updated)
            if (isActive) {
                alarmScheduler.scheduleFood(updated)
            } else {
                alarmScheduler.cancelFood(reminder)
            }
        }
    }

    fun markCompleted(reminder: FoodReminder) {
        viewModelScope.launch {
            reminderLogRepository.recordOrUpdateLog(
                category = ReminderCategory.FOOD,
                referenceId = reminder.id,
                title = reminder.displayName,
                subtitle = if (reminder.notes.isNotBlank()) reminder.notes else "Completed scheduled meal",
                scheduledDateIso = DateTimeUtils.todayIso(),
                scheduledTime = reminder.time,
                scheduledTimestamp = System.currentTimeMillis(),
                status = ReminderStatus.TAKEN
            )
        }
    }

    fun snooze(reminder: FoodReminder, minutes: Int = 10) {
        alarmScheduler.scheduleSnooze(
            category = ReminderCategory.FOOD,
            refId = reminder.id,
            title = reminder.displayName,
            subtitle = if (reminder.notes.isNotBlank()) reminder.notes else "Meal reminder",
            minutes = minutes
        )
    }

    /**
     * Regenerates the 10 eating reminders based on start time (e.g. 07:00 AM) and interval (e.g. 2 hours),
     * automatically handling crossing midnight, and updates alarms.
     */
    fun regenerateSchedule(
        startTime: String = "07:00",
        intervalHours: Int = 2,
        totalReminders: Int = 10,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val generated = foodReminderRepository.regenerateSchedule(
                startTime = startTime,
                intervalMinutes = intervalHours * 60,
                totalReminders = totalReminders
            )
            // Schedule all active alarms
            generated.forEach { reminder ->
                if (reminder.isActive) {
                    alarmScheduler.scheduleFood(reminder)
                }
            }
            onComplete?.invoke()
        }
    }

    fun saveFoodReminder(onSuccess: () -> Unit) {
        val state = _addEditState.value
        val nameToSave = if (state.customName.isNotBlank()) state.customName.trim() else state.mealType.labelEn

        viewModelScope.launch {
            val food = FoodReminder(
                id = state.id,
                mealType = state.mealType,
                customName = nameToSave,
                time = state.time,
                orderIndex = state.orderIndex,
                isNextDay = state.isNextDay,
                daysOfWeek = state.daysOfWeek,
                isActive = state.isActive,
                soundEnabled = state.soundEnabled,
                vibrationEnabled = state.vibrationEnabled,
                notes = state.notes.trim()
            )

            val id = if (state.id == 0L) {
                foodReminderRepository.insertFoodReminder(food)
            } else {
                foodReminderRepository.updateFoodReminder(food)
                state.id
            }

            val savedFood = food.copy(id = id)
            if (savedFood.isActive) {
                alarmScheduler.scheduleFood(savedFood)
            } else {
                alarmScheduler.cancelFood(savedFood)
            }
            onSuccess()
        }
    }

    fun deleteFoodReminder(reminder: FoodReminder, onDeleted: () -> Unit) {
        viewModelScope.launch {
            alarmScheduler.cancelFood(reminder)
            foodReminderRepository.deleteFoodReminder(reminder)
            onDeleted()
        }
    }

    class Factory(
        private val foodRepo: FoodReminderRepository,
        private val logRepo: ReminderLogRepository,
        private val scheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoodViewModel(foodRepo, logRepo, scheduler) as T
        }
    }
}
