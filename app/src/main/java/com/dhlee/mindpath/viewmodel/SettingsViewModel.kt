package com.dhlee.mindpath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhlee.mindpath.MyApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // 🌟 MyApplication(중앙 본부)에서 settingsRepository 가져오기
    private val repository = (application as MyApplication).settingsRepository

    // 1. 종료 소리(싱잉볼) 음소거 상태 (UI에서 실시간 관찰)
    val isBowlMuted: StateFlow<Boolean> = repository.isBowlMutedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // 2. 나중에 '설정 화면'에서 싱잉볼 토글 버튼을 누를 때 호출할 함수
    fun toggleBowlMute() {
        viewModelScope.launch {
            repository.saveBowlMuteSetting(!isBowlMuted.value)
        }
    }

    // 3. 온보딩 완료 여부. 초기값을 false가 아닌 null로 두어
    //    "DataStore를 아직 못 읽음(null)"과 "읽었는데 미완료(false)"를 구분함.
    //    false로 두면 첫 프레임에 온보딩이 깜빡 보였다 사라지는 문제가 생김.
    val isOnboardingCompleted: StateFlow<Boolean?> = repository.isOnboardingCompletedFlow
        .map { it as Boolean? }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 4. 온보딩 '건너뛰기'/'시작하기' 둘 다 여기로 옴
    fun completeOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding()
        }
    }

    // 5. 디버그 전용: 온보딩 다시 보기
    fun resetOnboarding() {
        viewModelScope.launch {
            repository.resetOnboarding()
        }
    }
}