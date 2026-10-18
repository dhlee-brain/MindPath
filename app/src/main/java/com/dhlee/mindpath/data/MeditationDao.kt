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

    @Query("SELECT * FROM touch_records WHERE sessionId IN (:sessionIds)")
    fun getTouchRecordsForSessions(sessionIds: List<Long>): Flow<List<TouchRecordEntity>>

    // 세션 삭제. touch_records는 FK CASCADE로 함께 지워짐
    @Query("DELETE FROM meditation_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)
}