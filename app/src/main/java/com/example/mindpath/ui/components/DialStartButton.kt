package com.example.mindpath.ui.components

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindpath.ui.screens.DialStartButton_Ritual
import com.example.mindpath.viewmodel.TimerViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2

@Composable
fun DialStartButton(
    modifier: Modifier = Modifier.Companion,
    startThresholdDegrees: Float = 360f,
    onStart: ()-> Unit
){
    var size by remember { mutableStateOf(IntSize.Companion.Zero) }
    val rotation = remember { Animatable(0f) }
    var running by remember { mutableStateOf(false) }

// 방향성을 가진 누적 회전량 (시계: +, 반시계: -)
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(if (running) Color(0xFF2E7D32) else Color(0xFF333333))
            .graphicsLayer { rotationZ = rotation.value }
            .onSizeChanged { size = it }
            .pointerInput(running) {
                if (running) return@pointerInput

                awaitEachGesture {
                    val down = awaitFirstDown()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    var lastAngle = angleDeg(down.position - center)
                    val pointerId = down.id

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId }
                        if (change == null || !change.pressed) break

                        val currentAngle = angleDeg(change.position - center)
                        Log.d("atan2", "currentAngle = $currentAngle, lastAngle = $lastAngle")
                        val delta = smallestAngleDeltaDeg(lastAngle, currentAngle)
                        lastAngle = currentAngle

                        Log.d(
                            "calculate res-1", "delta = $delta, rotation.value = ${rotation.value}"
                        )

                        // 1. 즉시 대입으로 시각적 피드백 제공
                        scope.launch {
                            val newRotation =
                                delta + rotation.value        // 스레드와 코루틴 이름을 확인하기 위한 로그
                            Log.d(
                                "calculate res-1",
                                "delta = $delta, rotation.value = ${rotation.value}, newRotation = $newRotation"
                            )
                            rotation.snapTo(newRotation)
                        }

                        // 2. 누적치 계산 (방향 유지)
                        accumulatedRotation += delta

                        // 3. 판정: 절댓값을 사용하여 시계/반시계 모두 허용
                        if (abs(accumulatedRotation) >= startThresholdDegrees) {
                            running = true
                            onStart()
                            break
                        }

                        if (change.positionChange() != Offset.Companion.Zero) change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Companion.Center
    ) {
        Text(
            text = if (running) "RUNNING" else "ROTATE TO START",
            color = Color.Companion.White
        )
    }

    LaunchedEffect(running) {
        if (!running) {
            accumulatedRotation = 0f
            rotation.snapTo(0f)
        }
    }
}

@Composable
fun DialStartButton_mutableFloatStateOfVersion(
    modifier: Modifier = Modifier.Companion,
    startThresholdDegrees: Float = 300f,
    onStart: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Companion.Zero) }
    // 시각적 회전을 위한 상태 (0~360도에 갇히지 않고 계속 증가/감소 가능)
    var rotationValue by remember { mutableFloatStateOf(0f) }
    var running by remember { mutableStateOf(false) }

    // 방향성을 가진 누적 회전량 (시계: +, 반시계: -)
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(if (running) Color(0xFF2E7D32) else Color(0xFF333333))
            // graphicsLayer 람다를 사용하여 Recomposition 없이 GPU에서 회전 처리
            .graphicsLayer { rotationZ = rotationValue }
            .onSizeChanged { size = it }
            .pointerInput(running) {
                if (running) return@pointerInput

                awaitEachGesture {
                    val down = awaitFirstDown()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    var lastAngle = angleDeg(down.position - center)

                    val pointerId = down.id

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId }
                        if (change == null || !change.pressed) break

                        val currentAngle = angleDeg(change.position - center)
                        val delta = smallestAngleDeltaDeg(lastAngle, currentAngle)
                        lastAngle = currentAngle

                        // 1. 즉시 대입으로 시각적 피드백 제공
                        rotationValue += delta

                        // 2. 누적치 계산 (방향 유지)
                        accumulatedRotation += delta

                        // 3. 판정: 절댓값을 사용하여 시계/반시계 모두 허용
                        if (abs(accumulatedRotation) >= startThresholdDegrees) {
                            running = true
                            onStart()
                            break
                        }

                        if (change.positionChange() != Offset.Companion.Zero) change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Companion.Center
    ) {
        Text(
            text = if (running) "RUNNING" else "SPIN 300° TO START",
            color = Color.Companion.White
        )
    }

    // 초기화: 실행 상태가 해제되면 모든 수치 리셋
    LaunchedEffect(running) {
        if (!running) {
            accumulatedRotation = 0f
            rotationValue = 0f
        }
    }
}


@Composable
fun DialStartButton_ProVersion(
    modifier: Modifier = Modifier.Companion,
    startThresholdDegrees: Float = 300f,
    onStart: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Companion.Zero) }
    var rotationValue by remember { mutableFloatStateOf(0f) }
    var running by remember { mutableStateOf(false) }
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }

    // 프레임 사이에 들어온 각도 변화량을 임시 저장할 변수
    var pendingDelta by remember { mutableFloatStateOf(0f) }

    // [프레임 동기화 루프]
    // UI가 실행되는 동안 화면 주사율에 맞춰서 pendingDelta를 rotationValue에 반영함
    LaunchedEffect(running) {
        if (running) return@LaunchedEffect
        while (true) {
            withFrameNanos { // 다음 V-Sync 신호까지 코루틴을 suspend 시킴
                if (pendingDelta != 0f) {
                    rotationValue += pendingDelta
                    accumulatedRotation += pendingDelta
                    pendingDelta = 0f // 소모 완료

                    if (abs(accumulatedRotation) >= startThresholdDegrees) {
                        running = true
                        onStart()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(if (running) Color(0xFF2E7D32) else Color(0xFF333333))
            .graphicsLayer { rotationZ = rotationValue }
            .onSizeChanged { size = it }
            .pointerInput(running) {
                if (running) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    var lastAngle = angleDeg(down.position - center)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || !change.pressed) break

                        val currentAngle = angleDeg(change.position - center)
                        val delta = smallestAngleDeltaDeg(lastAngle, currentAngle)
                        lastAngle = currentAngle

                        // 💡 직접 대입하지 않고 버퍼에 쌓아둠
                        pendingDelta += delta

                        change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Companion.Center
    ) {
        Text(text = if (running) "RUNNING" else "PERFECT SMOOTH")
    }
}

@Composable
fun DialStartButton_Ultimate(
    modifier: Modifier = Modifier.Companion,
    startThresholdDegrees: Float = 300f,
    onStart: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Companion.Zero) }
    var rotationValue by remember { mutableFloatStateOf(0f) }
    var running by remember { mutableStateOf(false) }
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }

    // 부드러운 움직임을 위한 타겟 각도 (목표치)
    var targetRotation by remember { mutableFloatStateOf(0f) }

    // [애니메이션 루프: 타겟을 부드럽게 추적]
    LaunchedEffect(running) {
        if (running) return@LaunchedEffect
        while (true) {
            withFrameNanos {
                // 💡 선형 보간(Lerp)과 유사한 로직:
                // 현재 각도에서 목표 각도까지의 차이 중 일부(예: 40%)만 매 프레임 반영
                // 이 수치가 낮을수록 묵직하고 부드러워지며, 높을수록 빠릿해짐
                val smoothingFactor = 0.4f
                val diff = targetRotation - rotationValue

                if (abs(diff) > 0.01f) {
                    val step = diff * smoothingFactor
                    rotationValue += step
                    accumulatedRotation += step

                    if (abs(accumulatedRotation) >= startThresholdDegrees) {
                        running = true
                        onStart()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(if (running) Color(0xFF2E7D32) else Color(0xFF333333))
            .graphicsLayer { rotationZ = rotationValue }
            .onSizeChanged { size = it }
            .pointerInput(running) {
                if (running) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    var lastAngle = angleDeg(down.position - center)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || !change.pressed) break

                        val currentPos = change.position
                        val dist = (currentPos - center).getDistance()

                        // 💡 1. 데드존: 중심에서 20px 이내는 계산 무시 (튀는 현상 방지)
                        if (dist > 20f) {
                            val currentAngle = angleDeg(currentPos - center)
                            val delta = smallestAngleDeltaDeg(lastAngle, currentAngle)

                            // 💡 2. 튀는 값 필터링: 한 프레임에 60도 이상 튀면 노이즈로 간주
                            if (abs(delta) < 60f) {
                                targetRotation += delta
                            }
                            lastAngle = currentAngle
                        }

                        change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Companion.Center
    ) {
        Text(text = if (running) "RUNNING" else "SILKY SMOOTH")
    }
}

@Composable
fun DialStartButton_Inertia(
    modifier: Modifier = Modifier.Companion,
    startThresholdDegrees: Float = 300f,
    onStart: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Companion.Zero) }
    var running by remember { mutableStateOf(false) }

    // 실제 다이얼의 각도 (물리 효과를 위해 Animatable 사용)
    val rotationAnimatable = remember { Animatable(0f) }
    // 누적값은 필터링 없이 정밀하게 측정
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }

    // 속도 측정기
    val velocityTracker = remember { VelocityTracker() }

    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(if (running) Color(0xFF2E7D32) else Color(0xFF333333))
            .graphicsLayer { rotationZ = rotationAnimatable.value }
            .onSizeChanged { size = it }
            .pointerInput(running) {
                if (running) return@pointerInput

                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)

                        val center = Offset(size.width / 2f, size.height / 2f)
                        var lastAngle = angleDeg(down.position - center)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) break

                            velocityTracker.addPosition(change.uptimeMillis, change.position)

                            val currentAngle = angleDeg(change.position - center)
                            val delta = smallestAngleDeltaDeg(lastAngle, currentAngle)
                            lastAngle = currentAngle

                            // 1. 판정은 delta가 발생하는 즉시 누적 (딜레이 해결)
                            accumulatedRotation += delta

                            // 2. 화면은 즉시 따라가도록 (snapTo)
                            launch {
                                rotationAnimatable.snapTo(rotationAnimatable.value + delta)
                            }

                            if (abs(accumulatedRotation) >= startThresholdDegrees) {
                                running = true
                                onStart()
                                break
                            }
                            change.consume()
                        }

                        // [손을 뗐을 때 - 관성 시작]
                        if (!running) {
                            val velocity = velocityTracker.calculateVelocity()
                            // 각속도로 변환하는 근사치 계산 (단순화)
                            val angularVelocity = (velocity.x + velocity.y) / 2f

                            launch {
                                // 핑그르르 도는 효과 (Decay 애니메이션)
                                rotationAnimatable.animateDecay(
                                    initialVelocity = angularVelocity,
                                    animationSpec = exponentialDecay()
                                )
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Companion.Center
    ) {
        Text(text = if (running) "RUNNING" else "FLICK ME!")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialStartScreen(modifier: Modifier = Modifier.Companion,
                    timerViewModel: TimerViewModel = viewModel()
) {
    // 1. ViewModel 상태 수집 (StateFlow를 Compose State로 변환)
    val isRunning by timerViewModel.isTimerRunning.collectAsState()
    val timeLeft by timerViewModel.timeLeft.collectAsState()
    var openDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 타이머가 시작되지 않았을 때 다이얼 표시
        DialStartButton_Ritual(durationSeconds = timeLeft) { timerViewModel.startTimer() }
        Spacer(modifier = Modifier.Companion.height(30.dp))
        // 타이머 실행 중일 때 표시할 UI
        TimerDisplay(timeLeft = timeLeft)
        Spacer(modifier = Modifier.Companion.height(20.dp))
        Row {
            Spacer(modifier = Modifier.Companion.width(100.dp))
            Button(
                onClick = { openDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF41C3E7),  // 부드러운 파란색 (거의 흰색) - 시간 창
                    contentColor = Color(0xFFFFFFFF) // 순수한 흰색 - Text, Surface, Background
                ),
            ) {
                Text(fontSize = 20.sp, text = "시간 선택")
            }

        }
        if (openDialog) {
            BasicAlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onDismissRequest.
                    openDialog = false
                }
            ) {
                Surface(
                    modifier = Modifier.Companion.wrapContentWidth().wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(modifier = Modifier.Companion.padding(16.dp)) {
                        PickHourMinuteSecondFun()
                        Spacer(modifier = Modifier.Companion.height(24.dp))
                        TextButton(
                            onClick = { openDialog = false },
                            modifier = Modifier.Companion.align(Alignment.Companion.End)
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerDisplay(timeLeft: Int) {
    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
        Text(
            text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
            style = MaterialTheme.typography.displayLarge,
            color = Color.Companion.DarkGray
        )
        Text(text = "Focusing...", color = Color.Companion.Gray)
    }
}

fun angleDeg(v: Offset): Float {
    // atan2(y, x) -> rad
    val rad = atan2(v.y, v.x)
    var deg = (rad * 180f / Math.PI.toFloat())
    // -180..180
    return deg
}

fun smallestAngleDeltaDeg(from: Float, to: Float): Float {
    var delta = to - from
    while (delta > 180f) delta -= 360f
    while (delta < -180f) delta += 360f
    return delta
}