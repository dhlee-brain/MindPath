package com.example.mindpath.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "distraction_records",
    foreignKeys = [
        ForeignKey(
            entity = MeditationSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.Companion.CASCADE // 세션 삭제 시 터치 기록도 삭제
        )
    ]
)
data class DistractionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,       // 해당 명상 세션의 ID
    val touchedTime: Long      // 터치한 순간의 시간 (Timestamp)
)