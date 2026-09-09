package com.dhlee.mindpath.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun EyeBlinkTransition(
    onComplete: () -> Unit  // onMidpoint와 통합하여 하나만 사용합니다.
) {
    val progress = remember { Animatable(0f) }
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
            // 1. 눈을 부드럽게 감기 (이 애니메이션은 유지)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )

            // 2. 완전히 감긴 상태에서 텍스트 표시
            showText = true
            delay(1500) // 문구를 읽을 수 있도록 1.5초 대기
            showText = false
            delay(400) // 텍스트가 서서히 사라지는 여운 0.4초

            // 3. 눈을 뜨는 애니메이션 삭제!
            // 즉시 onComplete를 호출하여 검정 화면 컴포넌트 자체를 날려버립니다.
            onComplete()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // 눈꺼풀 그리기
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f
            val edgeY = progress.value * midY
            val curveAmount = (1f - progress.value) * (height * 0.15f)

            // 위쪽 눈꺼풀
            val topEyelid = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(width, edgeY)
                quadraticBezierTo(width / 2f, edgeY + curveAmount, 0f, edgeY)
                close()
            }

            // 아래쪽 눈꺼풀
            val bottomEyelid = Path().apply {
                moveTo(0f, height)
                lineTo(width, height)
                lineTo(width, height - edgeY)
                quadraticBezierTo(width / 2f, height - edgeY - curveAmount, 0f, height - edgeY)
                close()
            }

            drawPath(path = topEyelid, color = Color.Black)
            drawPath(path = bottomEyelid, color = Color.Black)
        }

        // 안내 문구
        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500))
        ) {
            Text(
                text = "호흡이나 몸의 감각에 주의를 기울여 보세요.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}