package com.example.mindpath.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MeditationSessionEntity::class, TouchRecordEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meditationDao(): MeditationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindpath-database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}