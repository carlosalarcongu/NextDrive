package com.carlosalarcongu.nextdrive.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NextDriveDao {

    // Vehículos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVehicle(vehicle: Vehicle): Long

    @Delete
    fun deleteVehicle(vehicle: Vehicle)

    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<Vehicle>> // Flow ya es asíncrono por naturaleza

    // Gastos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId ORDER BY dateMillis DESC")
    fun getExpensesForVehicle(vehicleId: Long): Flow<List<Expense>>
}