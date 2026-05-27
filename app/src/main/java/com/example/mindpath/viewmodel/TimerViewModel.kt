package com.example.mindpath.viewmodel

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
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // 1. 전체 설정 시간을 저장하는 상태 추가 (기본값 60초)
    private val _totalTime = MutableStateFlow(60)
    val totalTime: StateFlow<Int> = _totalTime.asStateFlow()

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(onFinish: () -> Unit = {}) {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        val startTime = System.currentTimeMillis()
        // 2. 전체 시간은 totalTime 상태값을 기준으로 함
        val totalTicks = _totalTime.value

        // 타이머 시작 시 남은 시간을 전체 시간으로 초기화
        _timeLeft.value = totalTicks

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
            onFinish()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
    }

    fun setTime(seconds: Int) {
        _totalTime.value = seconds
        // 타이머가 돌고 있지 않을 때는 설정 시간을 변경하면 남은 시간도 동기화
        if (!_isTimerRunning.value) {
            _timeLeft.value = seconds
        }
    }
}
