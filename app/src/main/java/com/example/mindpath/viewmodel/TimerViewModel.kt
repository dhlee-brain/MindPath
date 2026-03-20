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

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(onFinish: () -> Unit = {}) {
        if (_isTimerRunning.value) return
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
            onFinish()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
    }

    fun setTime(seconds: Int) {
        _timeLeft.value = seconds
    }
}
