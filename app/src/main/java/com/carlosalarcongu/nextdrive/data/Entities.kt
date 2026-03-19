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
    val horsepower: Int? = null, val colorHex: String? = null
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
    val registeredKm: Int? = null
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