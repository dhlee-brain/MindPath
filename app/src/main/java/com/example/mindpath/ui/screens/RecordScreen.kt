package com.example.mindpath.ui.screens

import android.R.attr.text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindpath.local.MeditationSessionEntity
import com.example.mindpath.local.TouchRecordEntity
import com.example.mindpath.ui.components.MeditationCalendar
import com.example.mindpath.ui.components.MeditationSummaryCard
import com.example.mindpath.ui.components.SessionItem
import com.example.mindpath.ui.theme.Grey200
import com.example.mindpath.viewmodel.MeditationViewModel
import com.example.mindpath.viewmodel.toLocalDate
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

private const val HEADER_ITEM_COUNT = 4  // 제목, 요약카드, 달력, 구분선

@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    viewModel: MeditationViewModel = viewModel(factory = MeditationViewModel.Factory)
) {
    val allSessions by viewModel.allSessions.collectAsState()
    val touchRecords by viewModel.selectedSessionTouchRecords.collectAsState()
    val totalMillis by viewModel.totalMeditationMillis.collectAsState()
    val totalDays by viewModel.totalMeditationDays.collectAsState()
    val meditatedDates by viewModel.meditatedDates.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var expandedSessionId by remember { mutableStateOf<Long?>(null) }

    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current


    val filteredSessions = remember(allSessions, selectedDate) {
        allSessions
            .filter { it.startTime.toLocalDate() == selectedDate }
            .sortedByDescending { it.startTime }
    }

    // 💡 날짜 선택 시 자동 스크롤: 첫 카드가 화면 45% 지점에 오도록
    LaunchedEffect(selectedDate, filteredSessions.size) {
        if (filteredSessions.isEmpty()) return@LaunchedEffect

        // 💡 날짜가 바뀌면 아이템 개수도 바뀜. 새 레이아웃이 반영될 때까지 대기.
        //    이게 없으면 아래 layoutInfo가 '이전 날짜' 기준의 낡은 값이 됩니다.
        val expectedCount = HEADER_ITEM_COUNT + filteredSessions.size + 1  // +1 = 하단 Spacer
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .first { it == expectedCount }

        val info = listState.layoutInfo
        val last = info.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect

        // 마지막 아이템의 '아래쪽 끝'까지 화면 안에 들어와 있다 = 볼 게 더 없다
        val allContentVisible = last.index == info.totalItemsCount - 1 &&
                last.offset + last.size <= info.viewportEndOffset
        if (allContentVisible) return@LaunchedEffect

        val targetOffsetPx = with(density) {
            (configuration.screenHeightDp.dp * 0.45f).roundToPx()
        }
        listState.animateScrollToItem(HEADER_ITEM_COUNT, -targetOffsetPx)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        item(key = "title") {
            Text(
                text = "기록 모아보기",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }

        item(key = "summary") {
            MeditationSummaryCard(
                totalMillis = totalMillis,
                totalDays = totalDays
            )
            Spacer(Modifier.height(12.dp))
        }

        item(key = "calendar") {
            MeditationCalendar(
                selectedDate = selectedDate,
                meditatedDates = meditatedDates,
                onDateSelected = { date ->
                    selectedDate = date
                    expandedSessionId = null   // 날짜 바뀌면 펼침 상태 초기화
                }
            )
        }

        item(key = "divider") {
            HorizontalDivider(thickness = 1.dp)
            Spacer(Modifier.height(6.dp))
        }

        if (filteredSessions.isEmpty()) {
            item(key = "empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "이날은 기록된 명상 세션이 없어요.",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            itemsIndexed(
                items = filteredSessions,
                key = { _, session -> session.id }
            ) { index, session ->
                val isExpanded = expandedSessionId == session.id
                val reversedIndex = filteredSessions.size - index - 1

                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SessionItem(
                        session = session,
                        index = reversedIndex,
                        isExpanded = isExpanded,
                        touchRecords = if (isExpanded) touchRecords else emptyList(),
                        onClick = {
                            if (isExpanded) {
                                expandedSessionId = null
                            } else {
                                expandedSessionId = session.id
                                viewModel.loadTouchRecords(session.id)
                            }
                        }
                    )
                }
            }
            item(key = "bottom_spacer") { Spacer(Modifier.height(32.dp)) }
        }
    }
}