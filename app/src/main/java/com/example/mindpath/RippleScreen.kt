package com.example.ripplepractice

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language

@Language("AGSL")
private const val IMG_SHADER_SRC = """
   uniform float2 size;
   uniform float touchTime; // 변경: 무한한 time 대신 터치 후 흐른 시간(0~2초)을 받습니다.
   uniform float2 touchPoint;
   uniform shader composable;

   half4 main(float2 fragCoord) {
       float scale = 1.0 / size.x;
       float2 scaledCoord = fragCoord * scale;
       
       // 1. 누르기 전(-1.0)이거나 효과가 끝난 경우: 일렁임 없이 원본 그대로 반환
       if (touchTime < 0.0) {
           return composable.eval(scaledCoord / scale);
       }

       float2 center = touchPoint * scale;
       float2 dir = scaledCoord - center;
       float dist = length(dir);
       float2 normalizedDir = dist > 0.0 ? dir / dist : float2(0.0);

       float amplitude = 0.2; 
       float frequency = 30.0; 
       float speed = 10.0; // 속도를 살짝 높여 시원하게 퍼지도록 조정
       
       // 2. 공간적 감쇠 (원래 코드 동일 - 중심에서 멀수록 약해짐)
       float spatialAttenuation = max(0.0, 1.0 - (dist * 1.5)); 
       
       // 3. 시간적 감쇠 (시간이 지날수록 전체적으로 파동이 사그라들며 사라짐)
       // touchTime이 0.0에서 2.0으로 흐르기 때문에 서서히 0이 됩니다.
       float timeAttenuation = max(0.0, 1.0 - (touchTime / 2.0)); 

       // 4. 퍼져나가는 파면(Wavefront) 생성
       float wavefront = touchTime * 1.2; // 퍼지는 반경
       // 파동의 끝부분을 부드럽게 잘라내어 중심에서 바깥으로 퍼지는 형태를 만듭니다.
       float spreadMask = 1.0 - smoothstep(max(0.0, wavefront - 0.2), wavefront, dist);

       // 파동 공식
       float wave = sin(dist * frequency - touchTime * speed);
       
       // 모든 마스크와 감쇠값을 곱해 최종 오프셋 계산
       float2 offset = normalizedDir * wave * amplitude * spatialAttenuation * timeAttenuation * spreadMask;
       
       float2 textCoord = scaledCoord + offset;
       return composable.eval(textCoord / scale);
   }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MeditationScreen() {
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

                        // 2. 파동 애니메이션 로직
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
                fontSize = 14.sp
            )
            Text(
                text = "Total Session Time",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "알아차림의 파동을 느껴보세요.",
                color = Color(0xFFE0F7FA),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Feel the waves of your awareness.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}