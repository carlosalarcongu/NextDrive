// Entities.kt
package com.carlosalarcongu.nextdrive.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

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
    val vin: String? = null,
    val imageUri: String? = null
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
    val hasReminder: Boolean = false,
    val reminderType: String? = null,
    val reminderKm: Int? = null,
    val reminderDateMillis: Long? = null,
    val reminderTimePeriod: Int? = null,
    val reminderTimeUnit: String? = null,
    val iconName: String = "Herramientas" // ¡NUEVO! Icono personalizado
)

@Entity(
    tableName = "documents",
    foreignKeys = [ForeignKey(entity = Vehicle::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE)]
)
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val name: String,
    val uriString: String,
    val mimeType: String
)

// Mapa global de iconos para gastos
val ExpenseIconMap = mapOf(
    "Herramientas" to Icons.Default.Build,
    "Gasolinera" to Icons.Default.LocalGasStation,
    "Líquidos" to Icons.Default.Opacity,
    "Reparación" to Icons.Default.CarRepair,
    "Neumáticos" to Icons.Default.TireRepair,
    "Batería/Electricidad" to Icons.Default.ElectricalServices,
    "Lavado" to Icons.Default.LocalCarWash,
    "Multa/Tasas" to Icons.Default.AttachMoney,
    "Aparcamiento" to Icons.Default.LocalParking,
    "Otros" to Icons.Default.MoreHoriz
)