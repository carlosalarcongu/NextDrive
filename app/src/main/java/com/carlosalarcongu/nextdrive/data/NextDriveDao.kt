package com.carlosalarcongu.nextdrive.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NextDriveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertVehicle(vehicle: Vehicle): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertAllVehicles(vehicles: List<Vehicle>)
    @Update fun updateVehicle(vehicle: Vehicle)
    @Delete fun deleteVehicle(vehicle: Vehicle)
    @Query("SELECT * FROM vehicles ORDER BY isFavorite DESC, id DESC") fun getAllVehicles(): Flow<List<Vehicle>>
    @Query("SELECT * FROM vehicles") fun getAllVehiclesSync(): List<Vehicle>
    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1") fun getVehicleById(id: Long): Flow<Vehicle>

    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertExpense(expense: Expense): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertAllExpenses(expenses: List<Expense>)
    @Update fun updateExpense(expense: Expense)
    @Delete fun deleteExpense(expense: Expense)
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1") fun getExpenseById(id: Long): Flow<Expense>
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC") fun getAllExpensesSync(): List<Expense>
    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId ORDER BY dateMillis DESC") fun getExpensesForVehicle(vehicleId: Long): Flow<List<Expense>>
    @Query("SELECT * FROM expenses GROUP BY title ORDER BY dateMillis DESC") fun getUniqueExpensesHistory(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertDocument(document: Document): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertAllDocuments(documents: List<Document>)
    @Update fun updateDocument(document: Document)
    @Delete fun deleteDocument(document: Document)
    @Query("SELECT * FROM documents ORDER BY id DESC") fun getAllDocumentsSync(): List<Document>
    @Query("SELECT * FROM documents WHERE vehicleId = :vehicleId ORDER BY id DESC") fun getDocumentsForVehicle(vehicleId: Long): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertFolder(folder: DocumentFolder): Long
    @Delete fun deleteFolder(folder: DocumentFolder)
    @Query("SELECT * FROM document_folders WHERE vehicleId = :vehicleId ORDER BY name ASC") fun getFoldersForVehicle(vehicleId: Long): Flow<List<DocumentFolder>>

    @Query("DELETE FROM vehicles") fun deleteAllVehicles()
    @Query("DELETE FROM expenses") fun deleteAllExpenses()
    @Query("DELETE FROM documents") fun deleteAllDocuments()
}