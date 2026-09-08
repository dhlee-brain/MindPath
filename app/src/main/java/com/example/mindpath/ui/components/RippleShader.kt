package com.example.mindpath.ui.components

import androidx.compose.ui.graphics.Color
import org.intellij.lang.annotations.Language

@Language("AGSL")
internal const val IMG_SHADER_SRC = """
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

internal val RippleGradientColors = listOf(
    Color(0xFF00B4DB),
    Color(0xFF005C97)
)