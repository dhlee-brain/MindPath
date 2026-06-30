package com.example.mindpath.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
fun DialStartButton(
    modifier: Modifier = Modifier,
    startThresholdDegrees: Float = 360f,
    onStart: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var accumulatedRotation by remember { mutableFloatStateOf(0f) }

    val particles = remember { mutableStateListOf<Particle>() }

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
            .pointerInput(Unit) {
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
                                particles.add(Particle(currentPos, System.currentTimeMillis()))

                                if (abs(accumulatedRotation) >= startThresholdDegrees) {
                                    accumulatedRotation = 0f
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
            drawCircle(color = Color(0xFF1A1A1A))
            // 1. 그라디언트 색상 정의 (파란색 -> 보라색)
            val gradientColors = listOf(
                Color(0xFF00B4DB), // 상단
                Color(0xFF005C97)  // 하단
            )

            // 2. 그라디언트의 중심 설정 (현재 캔버스 중심)
            // 캔버스 크기의 절반 지점을 Offset으로 지정합니다.
            val arcCenter = Offset(size.width / 2f, size.height / 2f)

            // 3. sweepGradient Brush 생성
            val sweepGradientBrush = Brush.sweepGradient(
                colors = gradientColors,
                center = arcCenter // 그라디언트가 회전할 기준점
            )

            val strokeWidth = 50f
            rotate(degrees = -90f, pivot = arcCenter) {
                drawArc(
                    brush = sweepGradientBrush,
                    startAngle = 0f,
                    sweepAngle = accumulatedRotation,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            }
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

        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Text(
                text = "360도 회전하여",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "명상 시작하기",
                color = Color.White
            )
        }
    }
}
