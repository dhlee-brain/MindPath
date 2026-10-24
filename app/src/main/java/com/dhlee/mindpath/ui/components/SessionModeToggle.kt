package com.dhlee.mindpath.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhlee.mindpath.viewmodel.SessionMode

/**
 * "시간 모드 | 자유 모드" 알약(pill) 모양 세그먼트 토글.
 * 선택된 쪽 뒤로 인디케이터가 스프링으로 슬라이드되고, 색은 PresetButton의 선택 색과 동일한
 * 기존 액센트(Indigo 500 -> Violet 500)를 재사용함.
 */
@Composable
fun SessionModeToggle(
    mode: SessionMode,
    onModeSelected: (SessionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xBFFFFFFF)) // PresetButton 비선택 배경과 동일 톤
    ) {
        // maxWidth는 onSizeChanged처럼 레이아웃 이후 한 프레임 늦게 들어오는 게 아니라,
        // 첫 측정 단계부터 바로 알 수 있음 -> 마운트 첫 프레임부터 목표값이 정확해서
        // "클릭 안 했는데도 0에서 슬라이드되는" 애니메이션이 생기지 않음
        val halfWidth = maxWidth / 2

        val indicatorOffset by animateDpAsState(
            targetValue = if (mode == SessionMode.TIMED) 0.dp else halfWidth,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "sessionModeIndicatorOffset"
        )

        // 선택된 쪽으로 슬라이드하는 인디케이터
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .offset(x = indicatorOffset)
                .padding(3.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)) // Indigo 500 -> Violet 500
                    )
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            ModeLabel(
                text = "시간 모드",
                selected = mode == SessionMode.TIMED,
                onClick = { onModeSelected(SessionMode.TIMED) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModeLabel(
                text = "자유 모드",
                selected = mode == SessionMode.FREE,
                onClick = { onModeSelected(SessionMode.FREE) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ModeLabel(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null // 이미 인디케이터가 슬라이드로 선택을 보여주므로 리플 불필요
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}
