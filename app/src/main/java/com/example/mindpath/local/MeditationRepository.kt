package com.example.mindpath.local

import com.example.mindpath.local.MeditationDao

class MeditationRepository(private val meditationDao: MeditationDao) {
    suspend fun insertMeditationSession(session: MeditationSession): Long {
        return meditationDao.insertMeditationSession(session)
    }

    suspend fun insertDistractionRecords(records: List<DistractionRecord>) {
        meditationDao.insertDistractionRecords(records)
    }

    suspend fun getAllSessions(): List<MeditationSession> {
        return meditationDao.getAllSessions()
    }

    suspend fun getDistractionRecordsForSession(sessionId: Long): List<DistractionRecord> {
        return meditationDao.getDistractionRecordsForSession(sessionId)
    }
}