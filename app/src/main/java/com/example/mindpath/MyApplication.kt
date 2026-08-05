package com.example.mindpath

import android.app.Application
import com.example.mindpath.local.AppDatabase
import com.example.mindpath.local.MeditationRepository
import com.example.mindpath.local.SettingsRepository

class MyApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy { MeditationRepository(database.meditationDao()) }

    val settingsRepository by lazy { SettingsRepository(this) }
}