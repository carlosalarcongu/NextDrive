package com.carlosalarcongu.nextdrive.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String? = null,
    val model: String,
    val year: Int? = null,
    val currentKm: Int? = null,
    val fuelType: String? = null,
    val acquisitionCost: Double? = null,
    val isDailyUse: Boolean = true,
    val isSecondHand: Boolean = true,
    val type: String = "Turismo",
    val isFavorite: Boolean = false,
    val licensePlate: String? = null,
    val vin: String? = null, // ¡NUEVO! Número de bastidor
    val imageUri: String? = null // ¡NUEVO! Para la foto de portada
)

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(entity = Vehicle::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE)]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val dateMillis: Long,
    val totalCost: Double,
    val category: String,
    val groupName: String? = null,
    val comment: String? = null,
    val sharedWithXPersons: Int = 1,
    val hasReminder: Boolean = false, // ¡NUEVO! Recordatorio
    val reminderType: String? = null // ¡NUEVO! "NOTIFICACION", "CALENDARIO" o "AMBOS"
)