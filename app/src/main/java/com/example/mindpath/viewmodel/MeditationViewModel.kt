package com.example.mindpath.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mindpath.MyApplication
import com.example.mindpath.local.MeditationRepository
import com.example.mindpath.local.MeditationSessionEntity
import com.example.mindpath.local.TouchRecordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MeditationViewModel(private val repository: MeditationRepository) : ViewModel() {
    private var startTime: Long = 0
    private val currentTouchRecords = mutableListOf<Long>()

    private val _allSessions = MutableStateFlow<List<MeditationSessionEntity>>(emptyList())
    val allSessions: StateFlow<List<MeditationSessionEntity>> = _allSessions

    private val _selectedSessionTouchRecords = MutableStateFlow<List<TouchRecordEntity>>(emptyList())
    val selectedSessionTouchRecords: StateFlow<List<TouchRecordEntity>> = _selectedSessionTouchRecords

    fun startMeditation() {
        startTime = System.currentTimeMillis()
        currentTouchRecords.clear()
    }

    fun addTouchRecord() {
        currentTouchRecords.add(System.currentTimeMillis())
    }

    fun finishMeditation(feeling: String) {
        val endTime = System.currentTimeMillis()
        viewModelScope.launch {
            val session = MeditationSessionEntity(
                startTime = startTime,
                endTime = endTime,
                feelingRecord = feeling
            )
            val sessionId = repository.insertMeditationSession(session)
            val records = currentTouchRecords.map { time ->
                TouchRecordEntity(sessionId = sessionId, touchedTime = time)
            }
            repository.insertTouchRecords(records)
            loadAllSessions() // 저장 후 목록 갱신
        }
    }

    fun loadAllSessions() {
        viewModelScope.launch {
            _allSessions.value = repository.getAllSessions()
        }
    }

    fun loadTouchRecords(sessionId: Long) {
        viewModelScope.launch {
            _selectedSessionTouchRecords.value = repository.getTouchRecordsForSession(sessionId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY])
                val repository = (application as MyApplication).repository
                return MeditationViewModel(repository) as T
            }
        }
    }
}
