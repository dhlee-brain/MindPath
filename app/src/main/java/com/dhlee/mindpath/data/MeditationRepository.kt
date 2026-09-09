package com.dhlee.mindpath.data

import kotlinx.coroutines.flow.Flow

class MeditationRepository(private val meditationDao: MeditationDao) {
    suspend fun insertMeditationSession(session: MeditationSessionEntity): Long {
        return meditationDao.insertMeditationSession(session)
    }

    suspend fun insertTouchRecords(records: List<TouchRecordEntity>) {
        meditationDao.insertDistractionRecords(records)
    }

    fun getAllSessions(): Flow<List<MeditationSessionEntity>>
    = meditationDao.getAllSessions()
    fun getTouchRecordsForSessions(sessionIds: List<Long>): Flow<List<TouchRecordEntity>> =
        meditationDao.getTouchRecordsForSessions(sessionIds)
}