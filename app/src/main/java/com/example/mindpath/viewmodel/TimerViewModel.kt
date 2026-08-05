package com.example.mindpath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindpath.MyApplication
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// 🌟 1. ViewModel() 대신 AndroidViewModel(application)을 상속받아 Context를 안전하게 확보
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    // 🌟 2. MyApplication(중앙 본부)에서 미리 만들어둔 settingsRepository를 가져옴
    private val settingsRepository = (application as MyApplication).settingsRepository

    private val _timerFinishEvent = Channel<Unit>()
    val timerFinishEvent = _timerFinishEvent.receiveAsFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _totalTime = MutableStateFlow(60)
    val totalTime: StateFlow<Int> = _totalTime.asStateFlow()

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private var timerJob: Job? = null

    init {
        // 🌟 3. 뷰모델이 생성될 때(앱 켤 때) DataStore에서 마지막으로 설정한 시간을 불러옴
        viewModelScope.launch {
            settingsRepository.totalTimeFlow.collect { savedTime ->
                // 타이머가 작동 중이 아닐 때만 값을 갱신 (명상 중에 값이 바뀌는 오류 방지)
                if (!_isTimerRunning.value) {
                    _totalTime.value = savedTime
                    _timeLeft.value = savedTime
                }
            }
        }
    }

    fun startTimer() {
        _isTimerRunning.value = true
        val startTime = System.currentTimeMillis()
        val totalTicks = _totalTime.value

        // 타이머 시작 시 남은 시간을 전체 시간으로 초기화
        _timeLeft.value = _totalTime.value

        timerJob = viewModelScope.launch {
            for (tick in totalTicks downTo 1) {
                val targetTime = startTime + (totalTicks - tick + 1) * 1000L
                val currentTime = System.currentTimeMillis()
                val delayTime = targetTime - currentTime

                if (delayTime > 0) {
                    delay(delayTime)
                }
                _timeLeft.value = tick - 1
            }
            _isTimerRunning.value = false
            _timerFinishEvent.send(Unit)
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
    }

    fun setTime(seconds: Int) {
        _totalTime.value = seconds
        _timeLeft.value = seconds

        // 🌟 4. 유저가 화면에서 시간을 조절할 때마다 DataStore에 영구 저장
        viewModelScope.launch {
            settingsRepository.saveTotalTime(seconds)
        }
    }
}
