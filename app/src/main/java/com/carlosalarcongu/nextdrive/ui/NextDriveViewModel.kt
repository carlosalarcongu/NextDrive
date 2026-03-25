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
import com.carlosalarcongu.nextdrive.data.DocumentFolder

data class DatabaseBackup(
    val vehicles: List<Vehicle>,
    val expenses: List<Expense>,
    val documents: List<Document>
)

class NextDriveViewModel(private val dao: NextDriveDao) : ViewModel() {

    val allExpenses: StateFlow<List<Expense>> = dao.getAllExpensesSyncFlow().stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())
    val allVehicles: StateFlow<List<Vehicle>> = dao.getAllVehicles().stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // PAPELERA
    val deletedVehicles: StateFlow<List<Vehicle>> = dao.getDeletedVehicles().stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())
    val deletedExpenses: StateFlow<List<Expense>> = dao.getDeletedExpenses().stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun addVehicle(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) { dao.insertVehicle(vehicle) }
    fun updateVehicle(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) { dao.updateVehicle(vehicle) }

    // Ahora mandamos a la papelera en lugar de destruir
    fun softDeleteVehicle(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) { dao.updateVehicle(vehicle.copy(isDeleted = true)) }
    fun restoreVehicle(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) { dao.updateVehicle(vehicle.copy(isDeleted = false)) }
    fun hardDeleteVehicle(vehicle: Vehicle) = viewModelScope.launch(Dispatchers.IO) { dao.deleteVehicle(vehicle) }

    fun getVehicleById(id: Long): Flow<Vehicle> = dao.getVehicleById(id)

    // Gastos Múltiples a la Papelera
    fun softDeleteMultipleExpenses(ids: List<Long>) = viewModelScope.launch(Dispatchers.IO) { dao.softDeleteExpensesByIds(ids) }
    fun softDeleteExpenseById(id: Long) = viewModelScope.launch(Dispatchers.IO) { dao.softDeleteExpensesByIds(listOf(id)) }

    fun addExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) { dao.insertExpense(expense) }
    fun updateExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) { dao.updateExpense(expense) }

    // Papelera individual de Gastos
    fun softDeleteExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) { dao.updateExpense(expense.copy(isDeleted = true)) }
    fun restoreExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) { dao.updateExpense(expense.copy(isDeleted = false)) }
    fun hardDeleteExpense(expense: Expense) = viewModelScope.launch(Dispatchers.IO) { dao.deleteExpense(expense) }

    // Función rápida para Atender desde Próximos
    fun markExpenseAsAttended(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        dao.updateExpense(expense.copy(isAttended = true))
    }

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

    fun emptyTrash() = viewModelScope.launch(Dispatchers.IO) {
        val vels = deletedVehicles.value
        val exps = deletedExpenses.value
        vels.forEach { dao.deleteVehicle(it) }
        exps.forEach { dao.deleteExpense(it) }
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

    fun updateDocument(document: Document) = viewModelScope.launch(Dispatchers.IO) { dao.updateDocument(document) }
    fun addFolder(folder: DocumentFolder) = viewModelScope.launch(Dispatchers.IO) { dao.insertFolder(folder) }
    fun deleteFolder(folder: DocumentFolder) = viewModelScope.launch(Dispatchers.IO) { dao.deleteFolder(folder) }
    fun getFoldersForVehicle(vehicleId: Long): Flow<List<DocumentFolder>> = dao.getFoldersForVehicle(vehicleId)
    /**
     * Calcula la previsión de gasto por años.
     * @param selectedVehicleIds Set con los IDs de los coches a incluir. Si está vacío, incluye todos.
     */
    fun calculateForecast(expenses: List<Expense>, selectedVehicleIds: Set<Long>): List<YearForecast> {
        // 1. Filtrar por vehículos seleccionados (si hay alguno)
        val filteredExpenses = if (selectedVehicleIds.isNotEmpty()) {
            expenses.filter { it.vehicleId in selectedVehicleIds }
        } else {
            expenses
        }

        if (filteredExpenses.isEmpty()) return emptyList()

        // 2. Variables de tiempo actuales
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val currentDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val daysInYear = calendar.getActualMaximum(java.util.Calendar.DAY_OF_YEAR)
        val daysRemaining = daysInYear - currentDayOfYear

        // 3. Agrupar gastos por año
        val expensesByYear = filteredExpenses.groupBy {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = it.dateMillis
            cal.get(java.util.Calendar.YEAR)
        }

        // 4. Calcular el Gasto Medio Diario Histórico (para hacer la proyección)
        val firstExpenseDate = filteredExpenses.minOfOrNull { it.dateMillis } ?: System.currentTimeMillis()
        // Usamos un mínimo de 30 días para no falsear la media si el primer gasto fue ayer
        val daysSinceFirst = maxOf(30, ((System.currentTimeMillis() - firstExpenseDate) / (1000 * 60 * 60 * 24)).toInt())
        val totalHistoricalSpent = filteredExpenses.sumOf { it.totalCost }
        val historicalDailyAvg = totalHistoricalSpent / daysSinceFirst

        // 5. Construir los resultados año a año
        val results = mutableListOf<YearForecast>()
        val minYear = expensesByYear.keys.minOrNull() ?: currentYear

        for (year in minYear..currentYear) {
            val spentThisYear = expensesByYear[year]?.sumOf { it.totalCost } ?: 0.0

            if (year == currentYear) {
                // Proyección hasta fin de año: Media diaria * Días que faltan
                val projectedExtra = historicalDailyAvg * daysRemaining
                results.add(
                    YearForecast(
                        year = year,
                        actualSpent = spentThisYear,
                        projectedExtra = projectedExtra,
                        totalEstimated = spentThisYear + projectedExtra
                    )
                )
            } else {
                // Años pasados no tienen proyección extra
                results.add(
                    YearForecast(
                        year = year,
                        actualSpent = spentThisYear,
                        projectedExtra = 0.0,
                        totalEstimated = spentThisYear
                    )
                )
            }
        }

        // Devolvemos la lista ordenada del año más reciente al más antiguo
        return results.sortedByDescending { it.year }
    }
}


// Añade esta clase de datos fuera del ViewModel o al principio del archivo
data class YearForecast(
    val year: Int,
    val actualSpent: Double,
    val projectedExtra: Double,
    val totalEstimated: Double
)

class NextDriveViewModelFactory(private val dao: NextDriveDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NextDriveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return NextDriveViewModel(dao) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}