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

    suspend fun deleteSession(sessionId: Long) {
        meditationDao.deleteSessionById(sessionId)
    }

    /** 삭제 되돌리기 — 원래 id 그대로 세션과 터치 기록을 다시 넣음 */
    suspend fun restoreSession(
        session: MeditationSessionEntity,
        touchRecords: List<TouchRecordEntity>
    ) {
        meditationDao.insertMeditationSession(session)
        if (touchRecords.isNotEmpty()) {
            meditationDao.insertDistractionRecords(touchRecords)
        }
    }
}