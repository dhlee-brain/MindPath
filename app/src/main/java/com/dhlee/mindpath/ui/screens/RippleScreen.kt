package com.dhlee.mindpath.ui.screens

import com.dhlee.mindpath.R
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.media.MediaPlayer
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhlee.mindpath.ui.components.ExitMeditationDialog
import com.dhlee.mindpath.ui.components.FeelingInputDialog
import com.dhlee.mindpath.ui.components.MeditationTimerBar
import com.dhlee.mindpath.viewmodel.MeditationViewModel
import com.dhlee.mindpath.viewmodel.SettingsViewModel
import com.dhlee.mindpath.viewmodel.TimerViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dhlee.mindpath.ui.components.IMG_SHADER_SRC

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RippleScreen(
    timerViewModel: TimerViewModel,
    meditationViewModel: MeditationViewModel = viewModel(factory = MeditationViewModel.Factory),
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val timeLeft by timerViewModel.timeLeft.collectAsState()
    // StateFlow의 변화를 지속적으로 관찰함
    val totalTime by timerViewModel.totalTime.collectAsState()

    val touchCount by meditationViewModel.currentTouchCount.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var showFeelingDialog by remember { mutableStateOf(false) }

    val isBowlMuted by settingsViewModel.isBowlMuted.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val bowlPlayer = remember {
        MediaPlayer.create(context, R.raw.singing_bowl)
    }

    // 1) 명상 중 화면 꺼짐 방지
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // 2) 종료 소리(싱잉볼) 리소스 해제
    DisposableEffect(Unit) {
        onDispose {
            bowlPlayer?.release()
        }
    }

    // 🎵 종료 소리(싱잉볼) 실시간 볼륨 조절 (나중에 설정 창에서 바꿀 때 발동)
    LaunchedEffect(isBowlMuted) {
        val volume = if (isBowlMuted) 0f else 1f
        bowlPlayer?.setVolume(volume, volume)
    }

    // 1. 타이머 시작
    LaunchedEffect(Unit) {
        meditationViewModel.startMeditation()
        timerViewModel.startTimer()
    }

    // 2-1. 백 버튼 또는 스와이프 제스쳐를 통한 종료
    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        ExitMeditationDialog(
            onConfirm = {
                showExitDialog = false
                timerViewModel.stopTimer() // 타이머 중지
                meditationViewModel.finishMeditation("중도 종료", totalTime - timeLeft) // DB 저장
                onNavigateBack()// 메인으로 돌아가기
            },
            onDismiss = {
                showExitDialog = false // 창만 닫고 명상 계속
            }
        )
    }

    // 2-2. 타이머가 시간이 다 되어 종료되었을 때의 종료
    LaunchedEffect(Unit) {
        timerViewModel.timerFinishEvent.collect {
            showFeelingDialog = true
            bowlPlayer?.start()
        }
    }

    if (showFeelingDialog) {
        FeelingInputDialog(
            onConfirm = { inputFeeling ->
                showFeelingDialog = false
                meditationViewModel.finishMeditation(inputFeeling, totalTime) // 입력한 소감으로 DB 저장
                onNavigateBack() // 메인으로 돌아가기
            },
            onDismiss = {
                // 원한다면 소감을 안 적고 닫았을 때의 처리 (예: 빈칸으로 저장하고 닫기)
                showFeelingDialog = false
                meditationViewModel.finishMeditation("소감 생략", totalTime)
                onNavigateBack()
            },
            enabled = true,
        )
    }

    // 타이머 바 전용 독립 애니메이션 상태 (초기값 1.0 = 100%)
    val progressAnim = remember { Animatable(1f) }

    // 3. 타이머 On/Off 신호에 맞춰 한 번의 롱테이크 애니메이션 실행
    LaunchedEffect(Unit) {
            // 타이머 켜짐: 시작 전에 100%로 꽉 채운 후
            progressAnim.snapTo(1f)
            // 전체 설정 시간(ms) 동안 0%를 향해 한 번에 스무스하게 깎아내림!
            progressAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = totalTime * 1000, // ex: 2초면 2000ms 동안 쭉 줄어듦
                    easing = LinearEasing
                )
            )
    }

    val gradientColors = listOf(
        Color(0xFF00B4DB), // 상단
        Color(0xFF005C97)  // 하단
    )
    val shader = remember { RuntimeShader(IMG_SHADER_SRC) }
    val coroutineScope = rememberCoroutineScope()

    // 진동을 위한 hapticFeedback 객체 가져오기
    val haptic = LocalHapticFeedback.current

    val touchTime = remember { Animatable(-1f) }
    var touchOffset by remember { mutableStateOf(Offset.Unspecified) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // 1. 진동 발생 (짧고 가벼운 터치감)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // 2. 터치 기록 추가
                        meditationViewModel.addTouchRecord()
                        // 3. 파동 애니메이션 로직
                        touchOffset = offset
                        coroutineScope.launch {
                            touchTime.snapTo(0f)
                            touchTime.animateTo(
                                targetValue = 2f,
                                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
                            )
                        }
                    }
                )
            }
    ) {
        // 2. 셰이더가 적용되는 배경 레이어
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    shader.setFloatUniform("size", size.width, size.height)
                    // 애니메이션 되는 시간 값을 전달 (안 누른 상태면 -1.0 전달됨)
                    shader.setFloatUniform("touchTime", touchTime.value)

                    val touchX = if (touchOffset.isSpecified) touchOffset.x else size.width / 2f
                    val touchY = if (touchOffset.isSpecified) touchOffset.y else size.height / 2f
                    shader.setFloatUniform("touchPoint", touchX, touchY)

                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
                .background(Brush.verticalGradient(gradientColors))
        )

        // 🌟 [추가된 레이어] 알아차림 횟수 텍스트 (중앙에서 약간 상단)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-60).dp), // 중앙 기준에서 위로 60dp 끌어올림
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "알아차림 횟수",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$touchCount", // 뷰모델에서 가져온 실시간 횟수
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 66.sp, // 숫자는 크고 얇게 표현해 명상적인 분위기 연출
                fontWeight = FontWeight.Light
            )
        }

        // 3. 셰이더 영향을 받지 않는 깨끗한 텍스트 UI
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "설정 시간",
                color = Color(0xFFE0F7FA),
                fontSize = 14.sp,
                lineHeight = 14.sp
            )
            Text(
                text = "Total Session Time",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // 1. 여기서 타이머 바와 텍스트가 공유할 '80% 너비'를 확정 짓습니다.
                    .padding(vertical = 5.dp)
            ) {
                // 2. 시간 텍스트 (우측 정렬)
                Text(
                    text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                    fontSize = 18.sp, // 직접 크기 지정
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium, // 굵기 지정
                    color = Color.White, // 💡 주의: 현재 배경이 어두운 블루 계열이라 안 보일 수 있습니다.
                    modifier = Modifier.align(Alignment.End) // 이 속성으로 우측 끝에 붙입니다.
                )

                Spacer(modifier = Modifier.height(4.dp)) // 텍스트와 선 사이의 미세한 간격

                // 3. 타이머 바 (부모의 80% 너비를 100% 꽉 채움)
                MeditationTimerBar(
                    progress = progressAnim.value,
                    modifier = Modifier.fillMaxWidth() // 내부에 있던 0.8f 제약은 빼고 꽉 채우기만 합니다.
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text( // 나중에 노트에 적힌 문구로 바꾸기.
                text = "생각에 빠졌음을 알아차릴 때\n화면을 터치하여 돌아오세요",
                color = Color(0xFFE0F7FA),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}