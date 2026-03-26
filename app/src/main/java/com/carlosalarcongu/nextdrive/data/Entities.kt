package com.carlosalarcongu.nextdrive.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String? = null, val model: String, val year: Int? = null,
    val currentKm: Int? = null, val fuelType: String? = null, val acquisitionCost: Double? = null,
    val isDailyUse: Boolean = true, val isSecondHand: Boolean = true, val type: String = "Turismo",
    val isFavorite: Boolean = false, val licensePlate: String? = null, val vin: String? = null,
    val imageUri: String? = null, val nickname: String? = null, val engineName: String? = null,
    val horsepower: Int? = null, val colorHex: String? = null,
    val isDeleted: Boolean = false
)

@Entity(tableName = "expenses", foreignKeys = [ForeignKey(entity = Vehicle::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE)])
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long, val title: String, val dateMillis: Long, val totalCost: Double,
    val category: String, val groupName: String? = null, val comment: String? = null,
    val sharedWithXPersons: Int = 1, val hasReminder: Boolean = false, val reminderType: String? = null,
    val reminderKm: Int? = null, val reminderDateMillis: Long? = null, val reminderTimePeriod: Int? = null,
    val reminderTimeUnit: String? = null, val iconName: String = "Herramientas",
    val pricePerLiter: Double? = null, val liters: Double? = null,
    val laborCost: Double? = null, val partsCost: Double? = null,
    val isItemized: Boolean = false, val workshop: String? = null,
    val attachedDocumentsUris: String? = null,
    val registeredKm: Int? = null,
    val isDeleted: Boolean = false,
    val isAttended: Boolean = false
)

@Entity(tableName = "document_folders", foreignKeys = [ForeignKey(entity = Vehicle::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE)])
data class DocumentFolder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long, val name: String
)

@Entity(tableName = "documents", foreignKeys = [ForeignKey(entity = Vehicle::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE)])
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long, val folderId: Long? = null,
    val name: String, val uriString: String, val mimeType: String
)

// NUEVO: Tabla para guardar los intervalos personalizados por vehículo
@Entity(tableName = "service_intervals", foreignKeys = [ForeignKey(entity = Vehicle::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE)])
data class ServiceInterval(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val serviceName: String,
    val defaultKm: Int,
    val defaultMonths: Int
)

// Clase temporal para la lista estática (solo en memoria)
data class ServiceDef(val name: String, val defaultKm: Int, val defaultMonths: Int)

val predefinedServices = listOf(
    ServiceDef("Mantenimiento periódico", 15000, 12),
    ServiceDef("Cambio de llantas", 40000, 48),
    ServiceDef("Aceite con filtro", 15000, 12),
    ServiceDef("Filtro de aire del motor", 20000, 12),
    ServiceDef("Filtro de aire de cabina", 15000, 12),
    ServiceDef("Pastillas de freno", 30000, 24),
    ServiceDef("Discos y pastillas", 60000, 48),
    ServiceDef("Líquido de frenos", 40000, 24),
    ServiceDef("Bujías", 60000, 48),
    ServiceDef("Limpiaparabrisas", 10000, 12),
    ServiceDef("Ruedas", 40000, 48),
    ServiceDef("Motor", 200000, 120),
    ServiceDef("Suspensión", 80000, 60),
    ServiceDef("Caja de cambios", 100000, 60),
    ServiceDef("Líquidos generales", 50000, 36),
    ServiceDef("Filtro de combustible", 30000, 24),
    ServiceDef("Correa de distribución", 120000, 72),
    ServiceDef("Correa de accesorios", 80000, 60),
    ServiceDef("Anticongelante", 60000, 48),
    ServiceDef("Lavado", 1000, 1),
    ServiceDef("Reparación neumáticos", 0, 0),
    ServiceDef("Eléctrico", 50000, 36),
    ServiceDef("Reparación corporal", 0, 0),
    ServiceDef("Evacuación / Grúa", 100000, 72),
    ServiceDef("Actualización KM", 10000, 6),
    ServiceDef("Otro servicio", 20000, 12)
)