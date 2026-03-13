package com.carlosalarcongu.nextdrive.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String? = null,
    val model: String, // Único campo obligatorio
    val year: Int? = null,
    val currentKm: Int? = null,
    val fuelType: String? = null,
    val acquisitionCost: Double? = null,
    val isDailyUse: Boolean = true
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE // Si borras el coche, se borran sus gastos
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val dateMillis: Long,
    val totalCost: Double,
    val category: String, // "Pieza", "Consumible", "Repostaje"
    val groupName: String? = null, // "Motor", "Frenos"
    val sharedWithXPersons: Int = 1 // Para el calculador de gastos compartidos
)