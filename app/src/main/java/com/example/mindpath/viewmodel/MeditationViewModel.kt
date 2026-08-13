package com.example.mindpath.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mindpath.MyApplication
import com.example.mindpath.local.MeditationRepository
import com.example.mindpath.local.MeditationSessionEntity
import com.example.mindpath.local.TouchRecordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MeditationViewModel(private val repository: MeditationRepository) : ViewModel() {
    private var startTime: Long = 0
    private val currentTouchRecords = mutableListOf<Long>()

    private val _currentTouchCount = MutableStateFlow(0)
    val currentTouchCount: StateFlow<Int> = _currentTouchCount.asStateFlow()

    // 💡 Room Flow를 그대로 StateFlow로 변환. 저장/삭제 시 자동 갱신됨
    val allSessions: StateFlow<List<MeditationSessionEntity>> =
        repository.getAllSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedSessionTouchRecords = MutableStateFlow<List<TouchRecordEntity>>(emptyList())
    val selectedSessionTouchRecords: StateFlow<List<TouchRecordEntity>> = _selectedSessionTouchRecords

    /** 총 명상 시간 (ms) */
    val totalMeditationMillis: StateFlow<Long> = allSessions
        .map { sessions -> sessions.sumOf { (it.endTime - it.startTime).coerceAtLeast(0L) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    /** 명상한 날짜 집합 — 달력 점 표시용 */
    val meditatedDates: StateFlow<Set<LocalDate>> = allSessions
        .map { sessions -> sessions.map { it.startTime.toLocalDate() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** 총 명상 일수 */
    val totalMeditationDays: StateFlow<Int> = meditatedDates
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun startMeditation() {
        startTime = System.currentTimeMillis()
        currentTouchRecords.clear()
        _currentTouchCount.value = 0
    }

    fun addTouchRecord() {
        currentTouchRecords.add(System.currentTimeMillis())
        _currentTouchCount.value = currentTouchRecords.size
    }

    fun finishMeditation(feeling: String, completedDurationSeconds: Int? = null) {
        val endTime = if (completedDurationSeconds != null) {
            startTime + (completedDurationSeconds * 1000L)
        } else {
            System.currentTimeMillis()
        }
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
            // 💡 updateAllSessions() 호출 삭제 — Room Flow가 알아서 갱신
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
                val application = checkNotNull(extras[APPLICATION_KEY])
                val repository = (application as MyApplication).repository
                return MeditationViewModel(repository) as T
            }
        }
    }
}

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()