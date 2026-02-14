package com.example.mindpath

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    // 1. 타이머 상태 관리
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timeLeft = MutableStateFlow(120) // 초기 120초 설정
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private var timerJob: Job? = null

    // 2. 타이머 시작 함수
    fun startTimer() {
        if (_isTimerRunning.value) return // 이미 실행 중이면 무시
        _isTimerRunning.value = true
        val startTime = System.currentTimeMillis()
        val totalTicks = _timeLeft.value

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
            // 여기에 타이머 종료 후 작업 (예: 알람) 추가 가능
            val elapsedTime = System.currentTimeMillis() - startTime
            Log.d("end", "Timer finished. Expected: ${totalTicks * 1000}ms, Actual: ${elapsedTime}ms")
        }
    }

    // 3. 타이머 초기화 (필요시)
    fun resetTimer() {
        timerJob?.cancel()
        _timeLeft.value = 60
        _isTimerRunning.value = false
    }
}
