package com.dhlee.mindpath.ui.components

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

// 종료 알림 진동 패턴: 0ms 대기 -> 400ms 진동 -> 200ms 쉼 -> 400ms 진동
// 알아차림 터치 때의 짧은 햅틱("톡")과 확실히 구분되도록 길게 두 번
private val EndAlertPattern = longArrayOf(0, 400, 200, 400)

/**
 * 명상 종료 알림 진동 (설정 화면 미리보기에서도 같은 패턴 사용).
 * 햅틱(performHapticFeedback)과 달리 기기의 "터치 진동" 설정과 무관하게 울리며,
 * AndroidManifest의 VIBRATE 권한이 필요함 (일반 권한이라 사용자 팝업 없음).
 */
fun vibrateEndAlert(context: Context) {
    val vibrator = context.getSystemService(VibratorManager::class.java)?.defaultVibrator ?: return
    if (!vibrator.hasVibrator()) return // 진동 모터가 없는 기기(일부 태블릿 등)

    vibrator.vibrate(VibrationEffect.createWaveform(EndAlertPattern, -1)) // -1: 반복 없음
}
