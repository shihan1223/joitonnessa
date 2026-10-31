package com.example.data.repository

import com.example.data.local.MedicineDao
import com.example.data.model.Medicine
import kotlinx.coroutines.flow.Flow

class MedicineRepository(private val medicineDao: MedicineDao) {
    val allMedicines: Flow<List<Medicine>> = medicineDao.getAllMedicines()
    val activeMedicines: Flow<List<Medicine>> = medicineDao.getActiveMedicines()
    val activeCount: Flow<Int> = medicineDao.getActiveMedicineCount()

    suspend fun getMedicineById(id: Long): Medicine? = medicineDao.getMedicineById(id)

    fun getMedicineByIdFlow(id: Long): Flow<Medicine?> = medicineDao.getMedicineByIdFlow(id)

    suspend fun insertMedicine(medicine: Medicine): Long = medicineDao.insertMedicine(medicine)

    suspend fun updateMedicine(medicine: Medicine) = medicineDao.updateMedicine(medicine)

    suspend fun deleteMedicine(medicine: Medicine) = medicineDao.deleteMedicine(medicine)

    suspend fun deleteMedicineById(id: Long) = medicineDao.deleteMedicineById(id)
}
