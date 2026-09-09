package com.dhlee.mindpath.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. DataStore 싱글톤 인스턴스 생성 (앱 전체에서 공유할 설정 파일 이름)
val Context.dataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        // 2. 저장할 설정들의 Key(이름표)를 각각 독립적으로 생성
        val IS_BGM_MUTED_KEY = booleanPreferencesKey("is_bgm_muted")     // 명상 음악 (RippleScreen 제어용)
        val IS_BOWL_MUTED_KEY = booleanPreferencesKey("is_bowl_muted")   // 종료 소리 (설정 화면 제어용)
        val TOTAL_TIME_KEY = intPreferencesKey("total_time")             // 타이머 설정 시간
    }

    // ==========================================
    // [ 읽기 (Read) - 상태 변화를 실시간 감지하는 Flow ]
    // ==========================================

    // 1. 명상 음악(BGM) 음소거 상태 (기본값: false - 소리 켜짐)
    val isBgmMutedFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_BGM_MUTED_KEY] ?: false
        }

    // 2. 종료 소리(싱잉볼) 음소거 상태 (기본값: false - 소리 켜짐)
    val isBowlMutedFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_BOWL_MUTED_KEY] ?: false
        }

    // 3. 타이머 설정 시간 (기본값: 60초)
    val totalTimeFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[TOTAL_TIME_KEY] ?: 60
        }


    // ==========================================
    // [ 쓰기 (Write) - 상태를 영구 저장하는 함수 ]
    // ==========================================

    // 1. 명상 음악(BGM) 음소거 켜기/끄기 저장
    suspend fun saveBgmMuteSetting(isMuted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BGM_MUTED_KEY] = isMuted
        }
    }

    // 2. 종료 소리(싱잉볼) 음소거 켜기/끄기 저장 (나중에 설정 화면에서 사용)
    suspend fun saveBowlMuteSetting(isMuted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BOWL_MUTED_KEY] = isMuted
        }
    }

    // 3. 타이머 시간 저장
    suspend fun saveTotalTime(time: Int) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_TIME_KEY] = time
        }
    }
}