package com.example.mindpath

import android.app.Application
import com.example.mindpath.local.AppDatabase
import com.example.mindpath.local.MeditationRepository

class MyApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy { MeditationRepository(database.meditationDao()) }
}