package com.dhlee.mindpath

import android.app.Application
import com.dhlee.mindpath.data.AppDatabase
import com.dhlee.mindpath.data.MeditationRepository
import com.dhlee.mindpath.data.SettingsRepository

class MyApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy { MeditationRepository(database.meditationDao()) }

    val settingsRepository by lazy { SettingsRepository(this) }
}