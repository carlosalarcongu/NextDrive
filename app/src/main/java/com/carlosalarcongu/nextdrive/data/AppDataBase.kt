package com.carlosalarcongu.nextdrive.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN registeredKm INTEGER")
    }
}

@Database(entities = [Vehicle::class, Expense::class, Document::class, DocumentFolder::class], version = 11, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nextDriveDao(): NextDriveDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "nextdrive_local_database")
                    .addMigrations(MIGRATION_10_11).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}