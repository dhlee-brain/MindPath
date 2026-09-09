package com.dhlee.mindpath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationDao {
    @Insert
    suspend fun insertMeditationSession(session: MeditationSessionEntity): Long

    @Insert
    suspend fun insertDistractionRecords(records: List<TouchRecordEntity>)

    @Query("SELECT * FROM meditation_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<MeditationSessionEntity>>

    @Query("SELECT * FROM touch_records WHERE sessionId = :sessionId")
    suspend fun getTouchRecordsForSession(sessionId: Long): List<TouchRecordEntity>

    @Query("SELECT * FROM touch_records WHERE sessionId IN (:sessionIds)")
    fun getTouchRecordsForSessions(sessionIds: List<Long>): Flow<List<TouchRecordEntity>>
}