package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SecurityEvent::class], version = 1, exportSchema = false)
abstract class PowerGuardDatabase : RoomDatabase() {
    abstract fun securityEventDao(): SecurityEventDao

    companion object {
        @Volatile
        private var INSTANCE: PowerGuardDatabase? = null

        fun getInstance(context: Context): PowerGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PowerGuardDatabase::class.java,
                    "powerguard_security.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
