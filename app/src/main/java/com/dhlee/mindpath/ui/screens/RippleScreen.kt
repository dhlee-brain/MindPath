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
import com.dhlee.mindpath.ui.components.FinishMeditationDialog
import com.dhlee.mindpath.ui.components.vibrateEndAlert
import com.dhlee.mindpath.ui.components.FeelingInputDialog
import com.dhlee.mindpath.ui.components.MeditationTimerBar
import com.dhlee.mindpath.viewmodel.MeditationViewModel
import com.dhlee.mindpath.viewmodel.SessionMode
import com.dhlee.mindpath.viewmodel.SettingsViewModel
import com.dhlee.mindpath.viewmodel.TimerViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
    val sessionMode by timerViewModel.sessionMode.collectAsState()

    val touchCount by meditationViewModel.currentTouchCount.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    // 마치기 창에 쓰던 소감 (창을 닫았다 다시 열거나, 쓰는 도중 타이머가 끝나도 내용 유지)
    var feelingDraft by remember { mutableStateOf("") }
    var showFeelingDialog by remember { mutableStateOf(false) }

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

    // 3) 앱이 백그라운드로 가면(전화, 다른 앱 전환 등) 싱잉볼 소리가 나지 않도록 함
    var isAppInForeground by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> isAppInForeground = true
                Lifecycle.Event.ON_STOP -> {
                    isAppInForeground = false
                    // 백그라운드로 가는 순간 이미 재생 중이던 소리도 즉시 멈춤
                    bowlPlayer?.let { player -> if (player.isPlaying) player.pause() }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 1. 타이머 시작
    // 자유 모드는 시간 제한이 없으므로 카운트다운 자체를 시작하지 않음
    // (안 그러면 마지막에 설정했던 totalTime이 지나는 순간 몰래 자동 종료되어 버림)
    LaunchedEffect(Unit) {
        meditationViewModel.startMeditation()
        if (sessionMode == SessionMode.TIMED) {
            timerViewModel.startTimer()
        }
    }

    // 2-1. 백 버튼 또는 스와이프 제스쳐를 통한 종료
    BackHandler {
        showExitDialog = true
    }

    // 시간 모드 중도 종료 / 자유 모드 종료 모두 "마치기 + 소감" 창 하나로 처리
    if (showExitDialog) {
        FinishMeditationDialog(
            feeling = feelingDraft,
            onFeelingChange = { feelingDraft = it },
            onFinish = {
                showExitDialog = false
                val feeling = feelingDraft.takeIf { it.isNotBlank() } // 비워두고 마치면 소감 없음으로 저장
                if (sessionMode == SessionMode.TIMED) {
                    timerViewModel.stopTimer() // 타이머 중지
                    // 정한 시간을 다 채우지 않았으므로 isCompleted = false (중도 종료)
                    meditationViewModel.finishMeditation(feeling, false, totalTime - timeLeft)
                } else {
                    // 자유 모드는 원래 끝나는 시간이 없으니 직접 마치는 것이 정상 완료
                    // completedDurationSeconds를 null로 넘기면 '지금 이 순간'을 종료 시각으로 써서
                    // 시작부터 실제로 흐른 시간이 그대로 기록됨
                    meditationViewModel.finishMeditation(feeling, true, null, isFreeMode = true)
                }
                onNavigateBack()// 메인으로 돌아가기
            },
            onContinue = {
                showExitDialog = false // 창만 닫고 명상 계속 (쓰던 소감은 feelingDraft에 남아 있음)
            }
        )
    }

    // 2-2. 타이머가 시간이 다 되어 종료되었을 때의 종료
    LaunchedEffect(Unit) {
        timerViewModel.timerFinishEvent.collect {
            // 마치기 창을 띄워둔 채 시간이 다 된 경우: 그 창은 닫고 정상 완료 소감 창으로 넘어감
            showExitDialog = false
            showFeelingDialog = true

            // 설정 화면의 종료 알림 방식 (끝나는 순간의 저장값을 직접 읽음)
            val endAlert = settingsViewModel.currentEndAlert()
            // 앱이 백그라운드 상태(예: 통화 중)라면 소리를 내지 않음
            if (endAlert.hasSound && isAppInForeground) {
                bowlPlayer?.start()
            }
            // 진동은 통화를 방해하지 않으므로 백그라운드여도 울림 (시스템이 막는 경우는 무시됨)
            if (endAlert.hasVibration) {
                vibrateEndAlert(context)
            }
        }
    }

    if (showFeelingDialog) {
        FeelingInputDialog(
            onConfirm = { inputFeeling ->
                showFeelingDialog = false
                meditationViewModel.finishMeditation(inputFeeling, true, totalTime) // 입력한 소감으로 DB 저장
                onNavigateBack() // 메인으로 돌아가기
            },
            onDismiss = {
                // 소감을 안 적고 닫은 경우: 완료 처리는 하되 feelingRecord는 비워둠
                showFeelingDialog = false
                meditationViewModel.finishMeditation(null, true, totalTime)
                onNavigateBack()
            },
            enabled = true,
            initialFeeling = feelingDraft, // 마치기 창에 쓰던 내용 이어받기
        )
    }

    // 타이머 바 전용 독립 애니메이션 상태 (초기값 1.0 = 100%)
    val progressAnim = remember { Animatable(1f) }

    // 3. 타이머 On/Off 신호에 맞춰 한 번의 롱테이크 애니메이션 실행
    // 자유 모드는 깎아낼 '전체 시간'이라는 개념이 없으므로, 100%로 채운 채 그대로 둠
    LaunchedEffect(Unit) {
        // 타이머 켜짐: 시작 전에 100%로 꽉 채운 후
        progressAnim.snapTo(1f)
        if (sessionMode == SessionMode.TIMED) {
            // 전체 설정 시간(ms) 동안 0%를 향해 한 번에 스무스하게 깎아내림!
            progressAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = totalTime * 1000, // ex: 2초면 2000ms 동안 쭉 줄어듦
                    easing = LinearEasing
                )
            )
        }
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
                text = if (sessionMode == SessionMode.TIMED) "시간 모드" else "자유 모드",
                color = Color(0xFFE0F7FA),
                fontSize = 14.sp,
                lineHeight = 14.sp
            )

            // 뒤로가기(버튼/스와이프 공통 용어) 안내를 맨 아래에 배치
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "뒤로가기로 마칠 수 있어요",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // 1. 여기서 타이머 바와 텍스트가 공유할 '80% 너비'를 확정 짓습니다.
                    .padding(vertical = 5.dp)
            ) {
                // 2. 시간 텍스트 (우측 정렬) - 자유 모드에선 시간 대신 무한대 기호
                Text(
                    text = if (sessionMode == SessionMode.TIMED) {
                        String.format("%02d:%02d", timeLeft / 60, timeLeft % 60)
                    } else {
                        "∞" // ∞
                    },
                    fontSize = 18.sp, // 직접 크기 지정
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium, // 굵기 지정
                    color = Color.White, // 💡 주의: 현재 배경이 어두운 블루 계열이라 안 보일 수 있습니다.
                    modifier = Modifier.align(Alignment.End) // 이 속성으로 우측 끝에 붙입니다.
                )

                Spacer(modifier = Modifier.height(4.dp)) // 텍스트와 선 사이의 미세한 간격

                // 3. 타이머 바 (부모의 80% 너비를 100% 꽉 채움)
                // 자유 모드에선 progressAnim이 깎이지 않고 1f(=100%)로 고정된 채 유지됨
                MeditationTimerBar(
                    progress = progressAnim.value,
                    modifier = Modifier.fillMaxWidth() // 내부에 있던 0.8f 제약은 빼고 꽉 채우기만 합니다.
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text( // 나중에 노트에 적힌 문구로 바꾸기.
                text = "생각에 빠졌음을 알아차렸을 때\n화면을 터치하여 돌아와 보세요",
                color = Color(0xFFE0F7FA),
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}