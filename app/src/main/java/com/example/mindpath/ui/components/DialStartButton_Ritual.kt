package com.example.mindpath.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import com.example.mindpath.ui.screens.angleDeg
import com.example.mindpath.ui.screens.smallestAngleDeltaDeg
import com.example.mindpath.ui.theme.Purple80
import kotlin.math.abs

private data class Particle(val position: Offset, val createdAt: Long)

@Composable
fun DialStartButton_Ritual(
    modifier: Modifier = Modifier,
    startThresholdDegrees: Float = 360f,
    durationSeconds: Int,
    isRunning: Boolean = false, // 외부 상태 반영
    onStart: () -> Unit,
    onTouchDuringRunning: () -> Unit = {} // 실행 중 터치 이벤트
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }

    val particles = remember { mutableStateListOf<Particle>() }
    val sweepAngle = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ ->
                val now = System.currentTimeMillis()
                particles.removeAll { (now - it.createdAt) > 400 }
            }
        }
    }

    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .onSizeChanged { size = it }
            .pointerInput(isRunning) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    
                    if (isRunning) {
                        // 실행 중일 때는 터치 시 바로 기록 이벤트 발생
                        onTouchDuringRunning()
                        return@awaitEachGesture
                    }

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
                                particles.add(Particle(currentPos, System.currentTimeMillis()))

                                if (abs(accumulatedRotation) >= startThresholdDegrees) {
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
            val startAngleOffset = -90f
            drawCircle(color = if (isRunning) Color(0xFF444444) else Color(0xFF1A1A1A))

            if (isRunning) {
                drawArc(
                    color = Color.Red,
                    startAngle = startAngleOffset,
                    sweepAngle = sweepAngle.value,
                    useCenter = true
                )
            } else {
                val strokeWidth = 50f
                drawArc(
                    color = Purple80.copy(alpha = 1f),
                    startAngle = startAngleOffset,
                    sweepAngle = accumulatedRotation,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                val now = System.currentTimeMillis()
                particles.forEach { particle ->
                    val age = (now - particle.createdAt).coerceAtLeast(0)
                    val alpha = (1f - age / 600f).coerceIn(0f, 1f)
                    val lifeRatio = (1f - age / 600f).coerceIn(0f, 1f)

                    drawCircle(
                        color = Color(0xFFBBBBBB).copy(alpha = lifeRatio * 0.7f),
                        radius = 15f * alpha,
                        center = particle.position,
                        blendMode = BlendMode.Screen
                    )
                }
            }
        }

        Text(
            text = if (isRunning) "FOCUSING" else "SPIN TO START",
            color = Color.White
        )
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            sweepAngle.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = durationSeconds * 1000, easing = LinearEasing)
            )
        } else {
            accumulatedRotation = 0f
            particles.clear()
            sweepAngle.snapTo(0f)
        }
    }
}
