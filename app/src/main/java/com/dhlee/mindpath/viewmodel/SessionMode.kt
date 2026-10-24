package com.dhlee.mindpath.viewmodel

/** 명상 세션을 시작하는 방식 */
enum class SessionMode {
    TIMED, // 시간 모드 - 미리 정한 시간이 지나면 자동으로 끝남
    FREE   // 자유 모드 - 시간 제한 없이, 직접 끝낼 때까지 진행
}
