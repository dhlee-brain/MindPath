package com.dhlee.mindpath.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MeditationSummaryCard(
    totalMillis: Long,
    totalDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            SummaryColumn(
                label = "총 명상 시간",
                value = formatTotalDuration(totalMillis),
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(
                modifier = Modifier.height(40.dp),   // Row가 wrap-content라 높이 명시 필수
                thickness = 1.dp,
                color = Color.LightGray
            )

            SummaryColumn(
                label = "총 명상 일수",
                value = "${totalDays}일",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.DarkGray,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** 60분 미만이면 "X분", 이상이면 "X시간 X분" */
fun formatTotalDuration(totalMillis: Long): String {
    val totalMinutes = totalMillis / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
}