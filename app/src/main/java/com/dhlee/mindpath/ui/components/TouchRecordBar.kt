package com.dhlee.mindpath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhlee.mindpath.data.MeditationSessionEntity
import com.dhlee.mindpath.data.TouchRecordEntity
import java.util.Locale

@Composable
fun TouchRecordBar(
    session: MeditationSessionEntity,
    touchRecords: List<TouchRecordEntity>,
    modifier: Modifier = Modifier
) {
    val totalDurationMs = (session.endTime - session.startTime).coerceAtLeast(1L)
    val totalSeconds = totalDurationMs / 1000
    val endLabel = String.format(
        Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
        ) {
            val width = size.width
            val height = size.height

            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF00B4DB), Color(0xFF005C97))
                ),
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = height,
                cap = StrokeCap.Round
            )

            touchRecords.forEach { record ->
                val elapsedMs = record.touchedTime - session.startTime
                val fraction = (elapsedMs.toFloat() / totalDurationMs.toFloat())
                    .coerceIn(0f, 1f)
                drawLine(
                    color = Color.LightGray,
                    start = Offset(fraction * width, 0f),
                    end = Offset(fraction * width, height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("00:00", color = Color.Black, style = MaterialTheme.typography.bodySmall)
            Text(endLabel, color = Color.Black, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TouchRecordBarimprovedNoCorrection(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .width(200.dp)
            .height(20.dp)
    ) {
        val linePositions = listOf(0.1f, 0.2f, 0.5f, 0.7f, 0.9f)
        val width = size.width
        val height = size.height

        // 1. 배경 트랙 (은은한 그라데이션 적용) - 포인트 2 적용
        // 명상에 어울리는 깊은 블루 계열 그라데이션 사용
        val trackGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFF005C97), Color(0xFF363795))
        )

        drawLine(
            brush = trackGradient,
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
            strokeWidth = height,
            cap = StrokeCap.Round // 둥근 캡 적용
        )

        // 2. 터치 기록 마커 (부드러운 원형) - 포인트 1 적용
        linePositions.forEach { positionFraction ->
            // 사용자의 요청에 따라 포인트 3(오차 보정) 제외하고 원본 계산 방식 유지
            val x = positionFraction * width

            // 원형 마커 그리기
            drawCircle(
                color = Color.White.copy(alpha = 0.8f), // 약간 투명도를 주어 부드럽게
                radius = height / 3.5f, // 바(Bar) 높이의 약 3/4 정도로 설정하여 안에 쏙 들어가게
                center = Offset(x, height / 2)
            )
        }
    }
}

@Preview(name = "In Parent Screen")
@Composable
fun TouchRecordBarimprovedNoCorrectionPreview() {
    Surface(color = Color(0xFF1E1E1E)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "명상 기록",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            TouchRecordBarimprovedNoCorrection()
        }
    }
}

@Preview
@Composable
fun Greeting(){
    Column(
        modifier = Modifier
        .background(Color.Blue)
        .padding(24.dp)
    ){
        Text("hello")
        Text("Android")
    }
}