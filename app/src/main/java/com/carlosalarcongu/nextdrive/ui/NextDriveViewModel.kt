package com.carlosalarcongu.nextdrive.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carlosalarcongu.nextdrive.data.Document
import com.carlosalarcongu.nextdrive.data.Expense
import com.carlosalarcongu.nextdrive.data.NextDriveDao
import com.carlosalarcongu.nextdrive.data.Vehicle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Clase auxiliar para agrupar todo el backup
data class DatabaseBackup(
    val vehicles: List<Vehicle>,
    val expenses: List<Expense>,
    val documents: List<Document>
)

class NextDriveViewModel(private val dao: NextDriveDao) : ViewModel() {

    val allVehicles: StateFlow<List<Vehicle>> = dao.getAllVehicles()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

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

    fun deleteAllData() = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteAllDocuments()
        dao.deleteAllExpenses()
        dao.deleteAllVehicles()
    }

    fun exportDatabaseToJson(onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val vehicles = dao.getAllVehiclesSync()
            val expenses = dao.getAllExpensesSync()
            val documents = dao.getAllDocumentsSync()

            val backup = DatabaseBackup(vehicles, expenses, documents)
            val jsonString = Gson().toJson(backup)
            onResult(jsonString)
        }
    }

    fun importDatabaseFromJson(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backupType = object : TypeToken<DatabaseBackup>() {}.type
                val backup: DatabaseBackup = Gson().fromJson(jsonString, backupType)

                dao.deleteAllDocuments()
                dao.deleteAllExpenses()
                dao.deleteAllVehicles()

                dao.insertAllVehicles(backup.vehicles)
                dao.insertAllExpenses(backup.expenses)
                dao.insertAllDocuments(backup.documents)

                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}

class NextDriveViewModelFactory(private val dao: NextDriveDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NextDriveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return NextDriveViewModel(dao) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}