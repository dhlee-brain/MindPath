package com.dhlee.mindpath.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,           // 명상 시작 시간 (Timestamp)
    val endTime: Long,             // 명상 종료 시간 (Timestamp)
    val feelingRecord: String?,    // 명상 후 느낌 (사용자 입력만 담김, 없으면 null)
    val isCompleted: Boolean = true, // 타이머가 끝까지 진행됐는지 여부 (중도 종료면 false)
    // 자유 모드 세션인지 여부 (DB v2에서 추가, v1에서 넘어온 기존 기록은 모두 시간 모드 = 0)
    // defaultValue는 MIGRATION_1_2의 DEFAULT 0과 반드시 일치해야 Room 스키마 검증을 통과함
    @ColumnInfo(defaultValue = "0") val isFreeMode: Boolean = false
)