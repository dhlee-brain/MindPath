package com.dhlee.mindpath.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhlee.mindpath.MyApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // 🌟 MyApplication(중앙 본부)에서 settingsRepository 가져오기
    private val repository = (application as MyApplication).settingsRepository

    // 1. 종료 알림 방식 (설정 화면에서 실시간 관찰)
    val endAlert: StateFlow<EndAlert> = repository.endAlertFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EndAlert.SOUND
    )

    // 2. "지금 이 순간" 저장된 종료 알림 방식을 확실히 읽어옴
    //    StateFlow는 DataStore를 읽기 전엔 initialValue(SOUND)를 주기 때문에,
    //    알림을 울리는 그 순간 판단해야 하는 곳(RippleScreen 타이머 종료)에선 이걸 써야
    //    "없음"으로 설정했는데 소리가 나는 문제를 막을 수 있음
    suspend fun currentEndAlert(): EndAlert = repository.endAlertFlow.first()

    // 3. 설정 화면에서 종료 알림 방식 선택
    fun setEndAlert(alert: EndAlert) {
        viewModelScope.launch {
            repository.saveEndAlert(alert)
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