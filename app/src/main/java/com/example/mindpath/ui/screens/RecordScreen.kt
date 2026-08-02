package com.example.mindpath.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindpath.local.MeditationSessionEntity
import com.example.mindpath.local.TouchRecordEntity
import com.example.mindpath.ui.theme.Grey200
import com.example.mindpath.viewmodel.MeditationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    viewModel: MeditationViewModel = viewModel(factory = MeditationViewModel.Factory)
) {
    val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
    val touchRecords by viewModel.selectedSessionTouchRecords.collectAsState(initial = emptyList())

    // 달력 상태: 초기값 오늘 날짜
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    // 열려있는(확장된) 세션 카드의 ID 상태 관리
    var expandedSessionId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAllSessions()
    }

    // 선택된 날짜와 동일한 세션만 필터링
    val filteredSessions = allSessions.filter { session ->
        isSameDay(session.startTime, selectedDateMillis)
    }.sortedByDescending { it.startTime }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = "기록 모아보기",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(all = 16.dp)
        )
        // 1. 커스텀 색상이 적용된 달력 (어두운 테마 + 포인트 색상)
        DatePicker(
            state = datePickerState,
            modifier = Modifier.fillMaxWidth(),
            title = null,          // 💡 "날짜 선택" 등의 상단 타이틀 제거
            headline = null,       // 💡 "2026년 6월 19일" 등의 선택된 날짜 텍스트 제거
            showModeToggle = false, // 💡 우측 상단의 연필 아이콘(입력 모드 전환) 제거 (선택 사항)
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFFFAF5FF),
                weekdayContentColor = Color.DarkGray,
                dayContentColor = Color.Black,
                selectedDayContainerColor = Color(0xFF00B4DB), // 포인트 색상
                selectedDayContentColor = Color.White,
                todayContentColor = Color(0xFF00B4DB),
                todayDateBorderColor = Color(0xFF00B4DB),
                yearContentColor = Color.Black,
                currentYearContentColor = Color(0xFF00B4DB),
                selectedYearContainerColor = Color(0xFF00B4DB),
                selectedYearContentColor = Color.White
            )
        )

        HorizontalDivider(modifier = Modifier.height(1.dp))

        // 2. 선택된 날짜의 세션 목록
        if (filteredSessions.isEmpty()) {
            // 💡 데이터가 없을 때 띄워줄 화면
            Box(
                modifier = Modifier
                    .fillMaxSize() // 남은 공간을 모두 채움
                    .padding(bottom = 32.dp), // 달력과 너무 붙지 않게 시각적 중앙을 맞추기 위한 패딩
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "이날은 기록된 명상 세션이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray // 튀지 않는 색상으로 표시
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(filteredSessions) { index, session ->
                    val isExpanded = expandedSessionId == session.id

                    // 💡 추가된 부분: 전체 개수에서 현재 인덱스를 빼서 역순 번호 생성
                    // 예) 총 3개일 때 -> index 0은 2(세션 3), index 2는 0(세션 1)이 됨
                    val reversedIndex = filteredSessions.size - index - 1

                    Spacer(modifier = Modifier.height(6.dp))
                    SessionItem(
                        session = session,
                        index = reversedIndex, // 💡 기존 index 대신 역순 인덱스 전달
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
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

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
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
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
fun TouchRecordDetail(
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
                colors = listOf(Color(0xFF00B4DB), Color(0xFF005C97))
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
                val fraction = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
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
            Text(text = "00:00", color = Color.Black, style = MaterialTheme.typography.bodySmall)
            Text(text = endLabel, color = Color.Black, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 알아차림 횟수 및 소요 시간
        Text(text = "알아차림 횟수 : ${count}번", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "소요 시간 : $durationText", color = Color.Black, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(4.dp))
        val feelingText = session.feelingRecord?.takeIf { it.isNotBlank() } ?: "기록이 없습니다."
        Text(
            text = "명상 후 느낌 : $feelingText",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// 두 Timestamp 가 같은 날짜인지 판단하는 헬퍼 함수
fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}