package com.dhlee.mindpath.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MeditationSessionEntity::class, TouchRecordEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meditationDao(): MeditationDao

    companion object {
        // v1 -> v2 (앱 1.1.0): 세션에 자유 모드 여부 컬럼 추가
        // 이게 없으면 기존 사용자가 업데이트하는 순간 Room 스키마 불일치로 앱이 바로 종료됨
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE meditation_sessions ADD COLUMN isFreeMode INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindpath-database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}