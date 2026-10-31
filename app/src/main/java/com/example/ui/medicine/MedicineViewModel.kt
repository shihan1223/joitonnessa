package com.example.ui.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FoodRelation
import com.example.data.model.Medicine
import com.example.data.model.MedicineType
import com.example.data.repository.MedicineRepository
import com.example.data.repository.ReminderLogRepository
import com.example.reminder.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditMedicineState(
    val id: Long = 0,
    val name: String = "",
    val type: MedicineType = MedicineType.TABLET,
    val dosage: String = "1 Tablet",
    val instructions: String = "",
    val foodRelation: FoodRelation = FoodRelation.AFTER_FOOD,
    val reminderTimes: List<String> = listOf("08:00", "20:00"),
    val repeatType: String = "DAILY",
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val imageUri: String? = null,
    val isActive: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class MedicineViewModel(
    private val medicineRepository: MedicineRepository,
    private val reminderLogRepository: ReminderLogRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val allMedicines: StateFlow<List<Medicine>> = medicineRepository.allMedicines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _addEditState = MutableStateFlow(AddEditMedicineState())
    val addEditState: StateFlow<AddEditMedicineState> = _addEditState.asStateFlow()

    fun loadMedicine(medicineId: Long) {
        if (medicineId <= 0) {
            _addEditState.value = AddEditMedicineState()
            return
        }
        viewModelScope.launch {
            val med = medicineRepository.getMedicineById(medicineId)
            if (med != null) {
                _addEditState.value = AddEditMedicineState(
                    id = med.id,
                    name = med.name,
                    type = med.type,
                    dosage = med.dosage,
                    instructions = med.instructions,
                    foodRelation = med.foodRelation,
                    reminderTimes = med.reminderTimes,
                    repeatType = med.repeatType,
                    daysOfWeek = med.daysOfWeek,
                    startDate = med.startDate,
                    endDate = med.endDate,
                    imageUri = med.imageUri,
                    isActive = med.isActive
                )
            }
        }
    }

    fun updateName(name: String) {
        _addEditState.value = _addEditState.value.copy(name = name, errorMessage = null)
    }

    fun updateType(type: MedicineType) {
        _addEditState.value = _addEditState.value.copy(type = type)
    }

    fun updateDosage(dosage: String) {
        _addEditState.value = _addEditState.value.copy(dosage = dosage)
    }

    fun updateInstructions(instructions: String) {
        _addEditState.value = _addEditState.value.copy(instructions = instructions)
    }

    fun updateFoodRelation(relation: FoodRelation) {
        _addEditState.value = _addEditState.value.copy(foodRelation = relation)
    }

    fun addReminderTime(timeStr: String) {
        val current = _addEditState.value.reminderTimes.toMutableList()
        if (!current.contains(timeStr)) {
            current.add(timeStr)
            current.sort()
            _addEditState.value = _addEditState.value.copy(reminderTimes = current)
        }
    }

    fun removeReminderTime(timeStr: String) {
        val current = _addEditState.value.reminderTimes.toMutableList()
        current.remove(timeStr)
        _addEditState.value = _addEditState.value.copy(reminderTimes = current)
    }

    fun toggleDayOfWeek(day: Int) {
        val current = _addEditState.value.daysOfWeek.toMutableList()
        if (current.contains(day)) {
            if (current.size > 1) { // Keep at least one day
                current.remove(day)
            }
        } else {
            current.add(day)
            current.sort()
        }
        _addEditState.value = _addEditState.value.copy(daysOfWeek = current)
    }

    fun setAllDays() {
        _addEditState.value = _addEditState.value.copy(daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7))
    }

    fun setWeekdaysOnly() {
        _addEditState.value = _addEditState.value.copy(daysOfWeek = listOf(1, 2, 3, 4, 5))
    }

    fun saveMedicine(onSuccess: () -> Unit) {
        val state = _addEditState.value
        if (state.name.isBlank()) {
            _addEditState.value = state.copy(errorMessage = "Please enter medicine name (ঔষধের নাম লিখুন)")
            return
        }
        if (state.reminderTimes.isEmpty()) {
            _addEditState.value = state.copy(errorMessage = "Please add at least one reminder time (কমপক্ষে একটি সময় নির্ধারণ করুন)")
            return
        }

        viewModelScope.launch {
            _addEditState.value = state.copy(isSaving = true)
            val medicine = Medicine(
                id = state.id,
                name = state.name.trim(),
                type = state.type,
                dosage = state.dosage.trim(),
                instructions = state.instructions.trim(),
                foodRelation = state.foodRelation,
                reminderTimes = state.reminderTimes,
                repeatType = state.repeatType,
                daysOfWeek = state.daysOfWeek,
                startDate = state.startDate,
                endDate = state.endDate,
                imageUri = state.imageUri,
                isActive = state.isActive
            )

            val id = if (state.id == 0L) {
                medicineRepository.insertMedicine(medicine)
            } else {
                medicineRepository.updateMedicine(medicine)
                state.id
            }

            // Schedule/Re-schedule exact alarms
            val savedMedicine = medicine.copy(id = id)
            alarmScheduler.scheduleMedicine(savedMedicine)

            _addEditState.value = state.copy(isSaving = false, saveSuccess = true)
            onSuccess()
        }
    }

    fun deleteMedicine(medicine: Medicine, onDeleted: () -> Unit) {
        viewModelScope.launch {
            alarmScheduler.cancelMedicine(medicine)
            medicineRepository.deleteMedicine(medicine)
            onDeleted()
        }
    }

    class Factory(
        private val medRepo: MedicineRepository,
        private val logRepo: ReminderLogRepository,
        private val scheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MedicineViewModel(medRepo, logRepo, scheduler) as T
        }
    }
}
