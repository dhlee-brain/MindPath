package com.dhlee.mindpath

import android.app.Application
import com.dhlee.mindpath.local.AppDatabase
import com.dhlee.mindpath.local.MeditationRepository
import com.dhlee.mindpath.local.SettingsRepository

class MyApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy { MeditationRepository(database.meditationDao()) }

    val settingsRepository by lazy { SettingsRepository(this) }
}