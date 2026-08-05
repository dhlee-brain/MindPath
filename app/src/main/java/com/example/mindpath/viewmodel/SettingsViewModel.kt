package com.example.mindpath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindpath.MyApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // 🌟 MyApplication(중앙 본부)에서 settingsRepository 가져오기
    private val repository = (application as MyApplication).settingsRepository

    // 1. 명상 음악(BGM) 음소거 상태 (UI에서 실시간 관찰)
    val isBgmMuted: StateFlow<Boolean> = repository.isBgmMutedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // 2. 종료 소리(싱잉볼) 음소거 상태 (UI에서 실시간 관찰)
    val isBowlMuted: StateFlow<Boolean> = repository.isBowlMutedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // 3. RippleScreen의 우측 상단 음표 아이콘을 누를 때 호출할 함수 (BGM만 끄고 켬)
    fun toggleBgmMute() {
        viewModelScope.launch {
            repository.saveBgmMuteSetting(!isBgmMuted.value)
        }
    }

    // 4. 나중에 '설정 화면'에서 싱잉볼 토글 버튼을 누를 때 호출할 함수
    fun toggleBowlMute() {
        viewModelScope.launch {
            repository.saveBowlMuteSetting(!isBowlMuted.value)
        }
    }
}