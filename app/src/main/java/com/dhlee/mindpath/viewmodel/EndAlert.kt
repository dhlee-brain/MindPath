package com.dhlee.mindpath.viewmodel

/**
 * 명상이 끝날 때(타이머 종료) 알려주는 방식.
 * DataStore에는 name(문자열)으로 저장 -> 순서를 바꾸거나 항목을 추가해도 기존 값이 안 꼬임
 */
enum class EndAlert(
    val label: String,
    val hasSound: Boolean,
    val hasVibration: Boolean,
) {
    SOUND("싱잉볼 소리", hasSound = true, hasVibration = false),
    VIBRATE("진동", hasSound = false, hasVibration = true),
    SOUND_AND_VIBRATE("싱잉볼 소리 + 진동", hasSound = true, hasVibration = true),
    NONE("없음", hasSound = false, hasVibration = false),
}
