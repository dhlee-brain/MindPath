package com.dhlee.mindpath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhlee.mindpath.ui.components.MeditationCalendar
import com.dhlee.mindpath.ui.components.MeditationSummaryCard
import com.dhlee.mindpath.ui.components.SessionItem
import com.dhlee.mindpath.viewmodel.MeditationViewModel
import com.dhlee.mindpath.viewmodel.toLocalDate
import kotlinx.coroutines.flow.first
import java.time.LocalDate

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
    val touchRecordsBySession by viewModel.touchRecordsBySession.collectAsState()

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

    var lastScrolledDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(selectedDate, filteredSessions.size) {
        // 첫 진입: 스크롤 없이 현재 날짜만 기록
        if (lastScrolledDate == null) {
            lastScrolledDate = selectedDate
            return@LaunchedEffect
        }

        // 날짜가 안 바뀐 재실행(데이터 로드, 세션 추가 등)이면 무시
        if (lastScrolledDate == selectedDate) return@LaunchedEffect

        lastScrolledDate = selectedDate

        if (filteredSessions.isEmpty()) return@LaunchedEffect

        val expectedCount = HEADER_ITEM_COUNT + filteredSessions.size + 1
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .first { it == expectedCount }

        val info = listState.layoutInfo
        val last = info.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect

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
                        touchRecords = touchRecordsBySession[session.id].orEmpty(),
                        onClick = {
                            expandedSessionId = if (isExpanded) null else session.id
                        }
                    )
                }
            }
            item(key = "bottom_spacer") { Spacer(Modifier.height(32.dp)) }
        }
    }
}