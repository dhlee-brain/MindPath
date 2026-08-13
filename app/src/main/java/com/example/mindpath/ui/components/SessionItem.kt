package com.example.mindpath.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.mindpath.local.MeditationSessionEntity
import com.example.mindpath.local.TouchRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

@Composable
fun SessionItem(
    session: MeditationSessionEntity,
    index: Int,
    isExpanded: Boolean,
    touchRecords: List<TouchRecordEntity>,
    onClick: () -> Unit
) {
    // 💡 열림/닫힘 상태에 따른 화살표 회전 각도 애니메이션 처리 (0도 <-> 180도)
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ExpandIconRotation"
    )

    val timeFormatter = remember { SimpleDateFormat("a hh:mm", Locale.KOREAN) }
    val formattedTime = timeFormatter.format(Date(session.startTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 💡 타이틀과 아이콘을 가로로 배치하기 위해 Row 사용
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // 텍스트는 왼쪽, 아이콘은 오른쪽 끝으로 밀어줌
            ) {
                Text(
                    text = "세션 ${index + 1} - $formattedTime",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                )

                // 💡 확장 여부를 알려주는 화살표 아이콘
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "접기" else "펼치기",
                    tint = Color.Gray,
                    modifier = Modifier.rotate(rotationState) // 애니메이션 상태 적용
                )
            }

            // 카드를 눌렀을 때만 상세 정보 노출
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                TouchRecordDetail(
                    session = session,
                    touchRecords = touchRecords
                )
            }
        }
    }
}

@Composable
private fun TouchRecordDetail(
    session: MeditationSessionEntity,
    touchRecords: List<TouchRecordEntity>,
    modifier: Modifier = Modifier
) {
    // 소요 시간 계산
    val totalDurationMs = (session.endTime - session.startTime).coerceAtLeast(1L)
    val totalSeconds = totalDurationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    val endLabel = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val durationText = "${minutes}분 ${seconds}초"
    val count = touchRecords.size

    Column(modifier = modifier.fillMaxWidth()) {
        // 실제 데이터가 적용된 바
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
        ) {
            val width = size.width
            val height = size.height

            val trackGradient = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF00B4DB),
                    Color(0xFF005C97)
                )
            )

            // 1. 배경 트랙
            drawLine(
                brush = trackGradient,
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = height,
                cap = StrokeCap.Round
            )

            // 2. 실제 터치된 시간에 맞춰 선 그리기
            touchRecords.forEach { record ->
                val elapsedMs = record.touchedTime - session.startTime
                // 전체 시간 대비 터치된 시간의 비율 계산 (0.0 ~ 1.0)
                val fraction = (elapsedMs.toFloat() / totalDurationMs.toFloat())
                    .coerceIn(0f, 1f)
                val x = fraction * width

                drawLine(
                    color = Color.LightGray,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 바 양 끝 시작/종료 시간
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "00:00",
                color = Color.Black,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = endLabel,
                color = Color.Black,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 알아차림 횟수 및 소요 시간
        Text(
            text = "알아차림 횟수 : ${count}번",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "소요 시간 : $durationText",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))
        val feelingText = session.feelingRecord?.takeIf { it.isNotBlank() } ?: "기록이 없습니다."
        Text(
            text = "명상 후 느낌 : $feelingText",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}