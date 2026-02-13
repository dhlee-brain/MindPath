package com.example.mindpath
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// ... (상단 import 생략)

private data class Particle(val position: Offset, val createdAt: Long)

@Composable
fun DialStartButton_Ritual(
    modifier: Modifier = Modifier,
    startThresholdDegrees: Float = 300f,
    onStart: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var running by remember { mutableStateOf(false) }
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }

    // 손가락 위치 잔상을 위한 리스트
    val particles = remember { mutableStateListOf<Particle>() }

    // 애니메이션 루프: 오래된 잔상 제거
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ ->
                val now = System.currentTimeMillis()
                // 400ms가 지난 잔상은 삭제 (숫자를 줄이면 더 빨리 사라짐)
                particles.removeAll { (now - it.createdAt) > 400 }
            }
        }
    }

    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
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
                        if (change == null || !change.pressed){
                            accumulatedRotation = 0f
                            particles.clear()
                            break
                        }

                        val currentPos = change.position
                        val dist = (currentPos - center).getDistance()

                        if (dist > 20f) {
                            val currentAngle = angleDeg(currentPos - center)
                            val delta = smallestAngleDeltaDeg(lastAngle, currentAngle)

                            if (abs(delta) < 60f) {
                                accumulatedRotation += delta

                                // ✅ 손가락 현재 위치에 잔상 추가
                                particles.add(Particle(currentPos, System.currentTimeMillis()))

                                if (abs(accumulatedRotation) >= startThresholdDegrees) {
                                    running = true
                                    onStart()
                                    break
                                }
                            }
                            lastAngle = currentAngle
                        }
                        change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12f
            val startAngleOffset = -90f

            // 1. 배경 원
            drawCircle(color = if (running) Color(0xFF444444) else Color(0xFF1A1A1A))

            // 2. 진행 상황 테두리 (Arc)
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = startAngleOffset,
                sweepAngle = accumulatedRotation,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // 3. 손가락 끝 잔상(Particles) 그리기
            val now = System.currentTimeMillis()
            particles.forEach { particle ->
                val age = (now - particle.createdAt).coerceAtLeast(0)
                val alpha = (1f - age / 600f).coerceIn(0f, 1f)
                val lifeRatio = (1f - age / 600f).coerceIn(0f, 1f)

                // 빛나는 효과를 위해 BlendMode.Plus 사용 (선택 사항)
                drawCircle(
                    color = Color(0xFFBBBBBB).copy(alpha = lifeRatio * 0.7f),
                    radius = 15f * alpha, // 시간이 지날수록 크기가 줄어듦
                    center = particle.position,
                    blendMode = BlendMode.Screen
                )
            }
        }

        Text(
            text = if (running) "RUNNING" else "SPIN TO START",
            color = Color.White
        )
    }

    // 상태 초기화
    LaunchedEffect(running) {
        if (!running) {
            accumulatedRotation = 0f
            particles.clear()
        }
    }
}