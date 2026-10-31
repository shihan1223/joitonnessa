package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FoodReminder
import com.example.data.model.Medicine
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderLog
import com.example.data.model.ReminderStatus
import com.example.data.model.WaterConfig
import com.example.data.model.WaterLog
import com.example.data.repository.FoodReminderRepository
import com.example.data.repository.MedicineRepository
import com.example.data.repository.ReminderLogRepository
import com.example.data.repository.UserPreferences
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.WaterRepository
import com.example.reminder.AlarmScheduler
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayScheduleItem(
    val id: Long,
    val category: ReminderCategory,
    val title: String,
    val subtitle: String,
    val time: String,
    val status: ReminderStatus,
    val daysOfWeek: List<Int>,
    val referenceId: Long
)

data class DashboardUiState(
    val preferences: UserPreferences = UserPreferences(),
    val todayIso: String = DateTimeUtils.todayIso(),
    val medicines: List<Medicine> = emptyList(),
    val foodReminders: List<FoodReminder> = emptyList(),
    val todayItems: List<TodayScheduleItem> = emptyList(),
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val missedCount: Int = 0,
    val progressFraction: Float = 0f,
    val waterConfig: WaterConfig = WaterConfig(),
    val todayWaterIntakeMl: Int = 0
)

class DashboardViewModel(
    private val medicineRepository: MedicineRepository,
    private val foodReminderRepository: FoodReminderRepository,
    private val waterRepository: WaterRepository,
    private val reminderLogRepository: ReminderLogRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val todayIso = DateTimeUtils.todayIso()
    private val currentDayOfWeek = DateTimeUtils.currentDayOfWeek()

    private val waterCombinedFlow = combine(
        waterRepository.waterConfigFlow,
        waterRepository.getTotalWaterForDate(todayIso)
    ) { cfg, sum ->
        Pair(cfg ?: WaterConfig(), sum ?: 0)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        medicineRepository.activeMedicines,
        foodReminderRepository.activeFoodReminders,
        reminderLogRepository.getLogsForDate(todayIso),
        waterCombinedFlow
    ) { prefs: UserPreferences, meds: List<Medicine>, meals: List<FoodReminder>, logs: List<ReminderLog>, waterPair: Pair<WaterConfig, Int> ->
        val cfg = waterPair.first
        val totalWater = waterPair.second

        // Build combined today schedule items
        val items = mutableListOf<TodayScheduleItem>()

        // Add medicines matching today
        meds.forEach { med ->
            if (med.daysOfWeek.contains(currentDayOfWeek)) {
                med.reminderTimes.forEach { time ->
                    val log = logs.find { it.category == ReminderCategory.MEDICINE && it.referenceId == med.id && it.scheduledTime == time }
                    val status = log?.status ?: ReminderStatus.PENDING
                    items.add(
                        TodayScheduleItem(
                            id = med.id * 1000 + time.hashCode(),
                            category = ReminderCategory.MEDICINE,
                            title = med.name,
                            subtitle = "${med.dosage} • ${if (prefs.language == "bn") med.foodRelation.labelBn else med.foodRelation.labelEn}",
                            time = time,
                            status = status,
                            daysOfWeek = med.daysOfWeek,
                            referenceId = med.id
                        )
                    )
                }
            }
        }

        // Add food reminders matching today
        meals.forEach { meal ->
            if (meal.daysOfWeek.contains(currentDayOfWeek)) {
                val log = logs.find { it.category == ReminderCategory.FOOD && it.referenceId == meal.id && it.scheduledTime == meal.time }
                val status = log?.status ?: ReminderStatus.PENDING
                items.add(
                    TodayScheduleItem(
                        id = 50000 + meal.id,
                        category = ReminderCategory.FOOD,
                        title = meal.displayName,
                        subtitle = if (meal.notes.isNotBlank()) meal.notes else "Scheduled meal",
                        time = meal.time,
                        status = status,
                        daysOfWeek = meal.daysOfWeek,
                        referenceId = meal.id
                    )
                )
            }
        }

        // Sort chronologically
        items.sortBy { it.time }

        val completed = items.count { it.status == ReminderStatus.TAKEN }
        val missed = items.count { it.status == ReminderStatus.MISSED }
        val pending = items.count { it.status == ReminderStatus.PENDING }
        val total = items.size
        val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

        DashboardUiState(
            preferences = prefs,
            todayIso = todayIso,
            medicines = meds,
            foodReminders = meals,
            todayItems = items,
            completedCount = completed,
            pendingCount = pending,
            missedCount = missed,
            progressFraction = progress,
            waterConfig = cfg,
            todayWaterIntakeMl = totalWater
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun markItemTaken(item: TodayScheduleItem) {
        viewModelScope.launch {
            reminderLogRepository.recordOrUpdateLog(
                category = item.category,
                referenceId = item.referenceId,
                title = item.title,
                subtitle = item.subtitle,
                scheduledDateIso = todayIso,
                scheduledTime = item.time,
                scheduledTimestamp = System.currentTimeMillis(),
                status = ReminderStatus.TAKEN
            )
        }
    }

    fun markItemSkipped(item: TodayScheduleItem) {
        viewModelScope.launch {
            reminderLogRepository.recordOrUpdateLog(
                category = item.category,
                referenceId = item.referenceId,
                title = item.title,
                subtitle = item.subtitle,
                scheduledDateIso = todayIso,
                scheduledTime = item.time,
                scheduledTimestamp = System.currentTimeMillis(),
                status = ReminderStatus.SKIPPED
            )
        }
    }

    fun quickAddWater(amountMl: Int = 250) {
        viewModelScope.launch {
            waterRepository.logWater(todayIso, amountMl)
        }
    }

    fun dismissBatteryNotice() {
        viewModelScope.launch {
            userPreferencesRepository.setBatteryNoticeDismissed(true)
        }
    }

    class Factory(
        private val medRepo: MedicineRepository,
        private val foodRepo: FoodReminderRepository,
        private val waterRepo: WaterRepository,
        private val logRepo: ReminderLogRepository,
        private val prefsRepo: UserPreferencesRepository,
        private val scheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(medRepo, foodRepo, waterRepo, logRepo, prefsRepo, scheduler) as T
        }
    }
}
