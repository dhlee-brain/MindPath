package com.example.mindpath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import java.nio.file.Files.size

@Composable
fun MeditationTimerBar(
    progress: Float, // 0.0f ~ 1.0f
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .height(10.dp)
    ) {
        val width = size.width
        val height = size.height

        // 1. 배경 트랙 (희미한 회색)
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
            strokeWidth = height,
            cap = StrokeCap.Round
        )
        if (progress > 0f) {
            // 2. 진행 바 (밝은 흰색)
            drawLine(
                color = Color.White,
                start = Offset(0f, height / 2),
                end = Offset(width * progress, height / 2),
                strokeWidth = height,
                cap = StrokeCap.Round
            )
        }
    }
}