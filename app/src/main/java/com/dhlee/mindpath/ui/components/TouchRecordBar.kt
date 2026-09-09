package com.dhlee.mindpath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
