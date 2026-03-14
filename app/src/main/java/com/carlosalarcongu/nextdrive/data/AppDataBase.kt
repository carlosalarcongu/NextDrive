// AppDatabase.kt
package com.carlosalarcongu.nextdrive.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// VERSIÓN 7 - Aplicará DestructiveMigration automáticamente para acomodar iconName si no quieres lidiar con versiones manuales.
@Database(entities = [Vehicle::class, Expense::class, Document::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun nextDriveDao(): NextDriveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nextdrive_local_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}