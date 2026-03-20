package com.example.mindpath.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MeditationDao {
    @Insert
    suspend fun insertMeditationSession(session: MeditationSessionEntity): Long

    @Insert
    suspend fun insertDistractionRecords(records: List<TouchRecordEntity>)

    @Query("SELECT * FROM meditation_sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<MeditationSessionEntity>

    @Query("SELECT * FROM touch_records WHERE sessionId = :sessionId")
    suspend fun getTouchRecordsForSession(sessionId: Long): List<TouchRecordEntity>
}