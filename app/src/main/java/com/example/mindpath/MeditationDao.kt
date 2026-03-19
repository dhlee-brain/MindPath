package com.example.mindpath

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MeditationDao {
    @Insert
    suspend fun insertMeditationSession(session: MeditationSession): Long

    @Insert
    suspend fun insertDistractionRecords(records: List<DistractionRecord>)

    @Query("SELECT * FROM meditation_sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<MeditationSession>

    @Query("SELECT * FROM distraction_records WHERE sessionId = :sessionId")
    suspend fun getDistractionRecordsForSession(sessionId: Long): List<DistractionRecord>
}
