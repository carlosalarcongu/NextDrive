package com.carlosalarcongu.nextdrive.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Subimos la versión a 12 y añadimos ServiceInterval::class
@Database(entities = [Vehicle::class, Expense::class, Document::class, DocumentFolder::class, ServiceInterval::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nextDriveDao(): NextDriveDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "nextdrive_local_database")
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}