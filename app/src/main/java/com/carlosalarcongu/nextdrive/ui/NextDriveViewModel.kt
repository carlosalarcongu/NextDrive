// NextDriveViewModel.kt
package com.carlosalarcongu.nextdrive.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carlosalarcongu.nextdrive.data.Document
import com.carlosalarcongu.nextdrive.data.Expense
import com.carlosalarcongu.nextdrive.data.NextDriveDao
import com.carlosalarcongu.nextdrive.data.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NextDriveViewModel(private val dao: NextDriveDao) : ViewModel() {

    val allVehicles: StateFlow<List<Vehicle>> = dao.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addVehicle(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) { dao.insertVehicle(vehicle) }
    fun updateVehicle(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) { dao.updateVehicle(vehicle) }
    fun deleteVehicle(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) { dao.deleteVehicle(vehicle) }
    fun getVehicleById(id: Long): Flow<Vehicle> = dao.getVehicleById(id)

    fun addExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) { dao.insertExpense(expense) }
    fun updateExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) { dao.updateExpense(expense) }
    fun deleteExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) { dao.deleteExpense(expense) }
    fun getExpenseById(id: Long): Flow<Expense> = dao.getExpenseById(id)
    fun getExpensesForVehicle(vehicleId: Long): Flow<List<Expense>> = dao.getExpensesForVehicle(vehicleId)
    fun getUniqueExpensesHistory(): Flow<List<Expense>> = dao.getUniqueExpensesHistory()

    fun addDocument(document: Document) = viewModelScope.launch(Dispatchers.IO) { dao.insertDocument(document) }
    fun deleteDocument(document: Document) = viewModelScope.launch(Dispatchers.IO) { dao.deleteDocument(document) }
    fun getDocumentsForVehicle(vehicleId: Long): Flow<List<Document>> = dao.getDocumentsForVehicle(vehicleId)
}

class NextDriveViewModelFactory(private val dao: NextDriveDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NextDriveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NextDriveViewModel(dao) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}