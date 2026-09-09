package com.dhlee.mindpath.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,           // 명상 시작 시간 (Timestamp)
    val endTime: Long,             // 명상 종료 시간 (Timestamp)
    val feelingRecord: String?,    // 명상 후 느낌 (사용자 입력만 담김, 없으면 null)
    val isCompleted: Boolean = true // 타이머가 끝까지 진행됐는지 여부 (중도 종료면 false)
)