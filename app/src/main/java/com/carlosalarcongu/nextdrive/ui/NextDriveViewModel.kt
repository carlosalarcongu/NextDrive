package com.carlosalarcongu.nextdrive.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    // 1. LECTURA (Reativa): Convierte el Flow de Room en un StateFlow para Jetpack Compose.
    // Si añades un coche en la base de datos, esta variable se actualiza sola en la pantalla.
    val allVehicles: StateFlow<List<Vehicle>> = dao.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Optimiza la batería
            initialValue = emptyList()
        )

    // 2. ESCRITURA: Lanzamos las inserciones en un hilo secundario (Dispatchers.IO)
    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertVehicle(vehicle)
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteVehicle(vehicle)
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertExpense(expense)
        }
    }

    // 3. CONSULTAS ESPECÍFICAS: Obtener el historial de un coche concreto
    fun getExpensesForVehicle(vehicleId: Long): Flow<List<Expense>> {
        return dao.getExpensesForVehicle(vehicleId)
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateVehicle(vehicle)
        }
    }

    // Obtener un solo coche para el Dashboard
    fun getVehicleById(id: Long): Flow<Vehicle> {
        return dao.getVehicleById(id)
    }

    fun getUniqueExpensesHistory(): Flow<List<Expense>> {
        return dao.getUniqueExpensesHistory()
    }
}

// 4. FACTORY: Como nuestro ViewModel necesita recibir el 'dao' por parámetro,
// Android exige esta "Fábrica" para saber cómo construirlo correctamente.
class NextDriveViewModelFactory(private val dao: NextDriveDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NextDriveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NextDriveViewModel(dao) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}