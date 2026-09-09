package com.dhlee.mindpath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val PointColor = Color(0xFF00B4DB)

@Composable
fun MeditationCalendar(
    selectedDate: LocalDate,
    meditatedDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { YearMonth.now() }
    val daysOfWeek = remember { daysOfWeek() }
    val scope = rememberCoroutineScope()

    val state = rememberCalendarState(
        startMonth = remember { currentMonth.minusMonths(24) },
        endMonth = currentMonth,                 // 미래 달은 막음
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )
    val visibleMonth = state.firstVisibleMonth.yearMonth

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // 월 헤더 + 좌우 이동
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                scope.launch { state.animateScrollToMonth(visibleMonth.minusMonths(1)) }
            }) {
                Icon(Icons.Default.KeyboardArrowLeft, "이전 달", tint = Color.DarkGray)
            }
            Text(
                text = "${visibleMonth.year}년 ${visibleMonth.monthValue}월",
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = {
                    scope.launch { state.animateScrollToMonth(visibleMonth.plusMonths(1)) }
                },
                enabled = visibleMonth < currentMonth
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "다음 달", tint = Color.DarkGray)
            }
        }

        Spacer(Modifier.height(8.dp))

        // 요일 헤더
        Row(Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    modifier = Modifier.weight(1f),
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayCell(
                    day = day,
                    isSelected = day.date == selectedDate,
                    hasRecord = day.date in meditatedDates,
                    onClick = onDateSelected
                )
            }
        )
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    hasRecord: Boolean,
    onClick: (LocalDate) -> Unit
) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val isToday = day.date == LocalDate.now()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .background(if (isSelected) PointColor else Color.Transparent)
            .then(
                if (isToday && !isSelected) Modifier.border(1.dp, PointColor, CircleShape)
                else Modifier
            )
            .clickable(enabled = isCurrentMonth) { onClick(day.date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = when {
                !isCurrentMonth -> Color.LightGray
                isSelected -> Color.White
                else -> Color.Black
            },
            style = MaterialTheme.typography.bodyMedium
        )

        // 💡 명상 기록이 있는 날에 찍히는 작은 점
        if (hasRecord && isCurrentMonth) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 5.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else PointColor)
            )
        }
    }
}