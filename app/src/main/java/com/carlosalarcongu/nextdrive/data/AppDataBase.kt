package com.carlosalarcongu.nextdrive.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ¡OJO! Hemos subido la version a 2
@Database(entities = [Vehicle::class, Expense::class], version = 4, exportSchema = false)
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
                    .fallbackToDestructiveMigration() // ESTO ES CLAVE: Si hay cambios, borra y recrea la DB sin dar error
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}