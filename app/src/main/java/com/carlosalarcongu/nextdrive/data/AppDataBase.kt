package com.carlosalarcongu.nextdrive.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN pricePerLiter REAL")
        db.execSQL("ALTER TABLE expenses ADD COLUMN liters REAL")
        db.execSQL("ALTER TABLE expenses ADD COLUMN laborCost REAL")
        db.execSQL("ALTER TABLE expenses ADD COLUMN partsCost REAL")
        db.execSQL("ALTER TABLE expenses ADD COLUMN isItemized INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE expenses ADD COLUMN workshop TEXT")
        db.execSQL("ALTER TABLE expenses ADD COLUMN attachedDocumentsUris TEXT")
        db.execSQL("CREATE TABLE IF NOT EXISTS `document_folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `vehicleId` INTEGER NOT NULL, `name` TEXT NOT NULL, FOREIGN KEY(`vehicleId`) REFERENCES `vehicles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("ALTER TABLE documents ADD COLUMN folderId INTEGER")
    }
}

@Database(entities = [Vehicle::class, Expense::class, Document::class, DocumentFolder::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nextDriveDao(): NextDriveDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "nextdrive_local_database")
                    .addMigrations(MIGRATION_9_10).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}