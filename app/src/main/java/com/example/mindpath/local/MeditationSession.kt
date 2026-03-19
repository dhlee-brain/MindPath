package com.example.mindpath.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,       // 명상 시작 시간 (Timestamp)
    val endTime: Long,         // 명상 종료 시간 (Timestamp)
    val feelingRecord: String? // 명상 후 느낌
)