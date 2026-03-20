package com.example.mindpath.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mindpath.local.TouchRecordEntity
import com.example.mindpath.local.MeditationRepository
import com.example.mindpath.local.MeditationSessionEntity
import com.example.mindpath.MyApplication
import kotlinx.coroutines.launch

class MeditationViewModel(private val repository: MeditationRepository) : ViewModel() {
    private var startTime: Long = 0
    private val touchRecords = mutableListOf<Long>()

    fun startMeditation() {
        startTime = System.currentTimeMillis()
        touchRecords.clear() // 시작할 때 이전 기록 초기화
    }

    fun addTouchRecord() {
        touchRecords.add(System.currentTimeMillis())
    }

    fun finishMeditation(feeling: String) {
        val endTime = System.currentTimeMillis()

        viewModelScope.launch {
            // 1. 세션 엔티티 생성 및 저장
            val session = MeditationSessionEntity(
                startTime = startTime,
                endTime = endTime,
                feelingRecord = feeling
            )

            // repository를 통해 세션을 저장하고 생성된 ID를 받아옴
            // (UserRepository에 meditation 관련 메서드가 추가되어야 함)
            val sessionId = repository.insertMeditationSession(session)

            // 2. 터치 기록(DistractionRecord) 생성 및 저장
            val distractionRecords = touchRecords.map { time ->
                TouchRecordEntity(
                    sessionId = sessionId,
                    touchedTime = time
                )
            }

            // 생성된 모든 터치 기록을 저장
            repository.insertTouchRecords(distractionRecords)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY])
                val repository = (application as MyApplication).repository
                return MeditationViewModel(repository) as T
            }
        }
    }
}