package com.example.mindpath.local

class MeditationRepository(private val meditationDao: MeditationDao) {
    suspend fun insertMeditationSession(session: MeditationSessionEntity): Long {
        return meditationDao.insertMeditationSession(session)
    }

    suspend fun insertTouchRecords(records: List<TouchRecordEntity>) {
        meditationDao.insertDistractionRecords(records)
    }

    suspend fun getAllSessions(): List<MeditationSessionEntity> {
        return meditationDao.getAllSessions()
    }

    suspend fun getTouchRecordsForSession(sessionId: Long): List<TouchRecordEntity> {
        return meditationDao.getTouchRecordsForSession(sessionId)
    }
}