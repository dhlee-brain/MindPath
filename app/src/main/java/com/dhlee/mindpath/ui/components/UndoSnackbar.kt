package com.dhlee.mindpath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhlee.mindpath.ui.theme.AppFont

/**
 * 검정 바탕의 가로형 토스트.
 * 왼쪽에는 안내 문구(작고 흐린 흰색), 오른쪽에는 되돌리기 액션(크고 선명한 민트색)이 놓임.
 */
@Composable
fun UndoSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = data.visuals.message,
            color = Color(0xFFE0E0E0),
            fontFamily = AppFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )

        val actionLabel = data.visuals.actionLabel
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                color = Color(0xFF4DD0E1),
                fontFamily = AppFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { data.performAction() }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}
