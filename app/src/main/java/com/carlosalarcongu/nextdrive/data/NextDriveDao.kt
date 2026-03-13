package com.carlosalarcongu.nextdrive.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NextDriveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVehicle(vehicle: Vehicle): Long

    @Update
    fun updateVehicle(vehicle: Vehicle)

    @Delete
    fun deleteVehicle(vehicle: Vehicle)

    @Query("SELECT * FROM vehicles ORDER BY isFavorite DESC, id DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    fun getVehicleById(id: Long): Flow<Vehicle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId ORDER BY dateMillis DESC")
    fun getExpensesForVehicle(vehicleId: Long): Flow<List<Expense>>

    // ¡NUEVA! Obtiene los gastos históricos para el autocompletado
    @Query("SELECT * FROM expenses GROUP BY title ORDER BY dateMillis DESC")
    fun getUniqueExpensesHistory(): Flow<List<Expense>>
}