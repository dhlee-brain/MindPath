package com.dhlee.mindpath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

private val ItemHeight = 48.dp
private const val VisibleItems = 3   // 홀수여야 가운데가 생김

@Composable
fun MinuteSecondPicker(
    initialTotalSeconds: Int,
    onTimeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var minute by remember { mutableIntStateOf(initialTotalSeconds / 60) }
    var second by remember { mutableIntStateOf(initialTotalSeconds % 60) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        WheelColumn(
            range = 0..59,
            initialValue = minute,
            label = "분",
            onValueChange = {
                minute = it
                onTimeChange(minute * 60 + second)
            }
        )

        Spacer(Modifier.width(20.dp))

        WheelColumn(
            range = 0..59,
            initialValue = second,
            label = "초",
            onValueChange = {
                second = it
                onTimeChange(minute * 60 + second)
            }
        )
    }
}

@Composable
private fun WheelColumn(
    range: IntRange,
    initialValue: Int,
    label: String,
    onValueChange: (Int) -> Unit,
) {
    val items = remember(range) { range.toList() }
    val padding = VisibleItems / 2

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialValue)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 스크롤이 멈춘 위치의 가운데 값을 읽어 콜백
    val centerIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    LaunchedEffect(centerIndex) {
        items.getOrNull(centerIndex)?.let(onValueChange)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(ItemHeight * VisibleItems),
            contentAlignment = Alignment.Center
        ) {
            // 가운데 선택 영역 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ItemHeight)
                    .background(
                        color = Color(0xFF6366F1).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = ItemHeight * padding),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { value ->
                    val index = items.indexOf(value)
                    val distance = (index - centerIndex).absoluteValue
                    val isCenter = distance == 0

                    Box(
                        modifier = Modifier
                            .height(ItemHeight)
                            .fillMaxWidth()
                            .alpha(if (isCenter) 1f else 0.35f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.toString().padStart(2, '0'),
                            fontSize = if (isCenter) 26.sp else 20.sp,
                            fontWeight = if (isCenter) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCenter) Color(0xFF6366F1) else Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color.DarkGray
        )
    }
}