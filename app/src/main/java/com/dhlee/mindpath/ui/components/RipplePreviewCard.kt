package com.dhlee.mindpath.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun RipplePreviewCard(modifier: Modifier = Modifier) {
    var count by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RippleShaderLayer(
                modifier = Modifier.matchParentSize(),
                onTap = { count++ }
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(RippleGradientColors))
                    .pointerInput(Unit) { detectTapGestures { count++ } }
            )
        }

        RippleCounter(count, Modifier.align(Alignment.Center))
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun RippleShaderLayer(
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    val shader = remember { RuntimeShader(IMG_SHADER_SRC) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val touchTime = remember { Animatable(-1f) }
    var touchOffset by remember { mutableStateOf(Offset.Unspecified) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTap()
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
            .graphicsLayer {
                shader.setFloatUniform("size", size.width, size.height)
                shader.setFloatUniform("touchTime", touchTime.value)

                val touchX = if (touchOffset.isSpecified) touchOffset.x else size.width / 2f
                val touchY = if (touchOffset.isSpecified) touchOffset.y else size.height / 2f
                shader.setFloatUniform("touchPoint", touchX, touchY)

                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(shader, "composable")
                    .asComposeRenderEffect()
            }
            .background(Brush.verticalGradient(RippleGradientColors))
    )
}

@Composable
private fun RippleCounter(count: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("알아차림 횟수", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$count",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 44.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(Modifier.height(24.dp))
        AnimatedVisibility(visible = count == 0) {
            Text(
                text = "화면을 터치해보세요!",
                color = Color(0xFFE0F7FA),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}