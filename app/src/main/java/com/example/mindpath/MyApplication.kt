package com.example.mindpath

import android.app.Application

class MyApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy { MeditationRepository(database.meditationDao()) }
}