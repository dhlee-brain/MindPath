package com.dhlee.mindpath.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhlee.mindpath.BuildConfig
import com.dhlee.mindpath.ui.components.DialStartButton
import com.dhlee.mindpath.ui.components.MinuteSecondPicker
import com.dhlee.mindpath.ui.components.SessionModeToggle
import com.dhlee.mindpath.viewmodel.SessionMode
import com.dhlee.mindpath.viewmodel.SettingsViewModel
import com.dhlee.mindpath.viewmodel.TimerViewModel
import kotlin.math.atan2


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialStartScreen(
    modifier: Modifier = Modifier,
    onHelpClick: () -> Unit,
    onStart: () -> Unit,
    timerViewModel: TimerViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val isRunning by timerViewModel.isTimerRunning.collectAsState()
    val totalTime by timerViewModel.totalTime.collectAsState()
    val sessionMode by timerViewModel.sessionMode.collectAsState()
    var openDialog by remember { mutableStateOf(false) }


    // 1. Box를 최상위 레이아웃으로 사용하여 겹치기 및 절대 위치 지정 허용
    Column(modifier = modifier.fillMaxSize()) {

        // 2. 상단 바 영역 (왼쪽: 로고 + 앱 이름 / 오른쪽: 도움말)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 오른쪽은 IconButton(48dp) 자체 여백이 있어 덜 줌 -> 로고 왼쪽 끝과 아이콘 오른쪽 끝이 화면 가장자리에서 비슷한 거리
                .padding(start = 5.dp, end = 8.dp, top = 0.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically // 로고 / 글자 / 아이콘의 세로 중심을 맞춤
        ) {
            // 2-1. 로고 + 앱 이름
            // 온보딩과 같은 천천히 회전하는 연꽃 로고
            RotatingPetals(size = 36.dp)

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = "마음의 길",
                style = MaterialTheme.typography.titleLarge, // 기록/설정 탭 제목과 같은 스타일
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f)) // 남는 공간을 밀어 도움말 버튼을 오른쪽 끝으로

            // 2-2. 도움말 버튼
            IconButton(
                onClick = onHelpClick,
                modifier = Modifier.size(48.dp) // 최소 터치 영역 48dp 유지
            ) {
                Icon(
                    imageVector = Icons.Default.Info, // HelpOutline이나 다른 아이콘으로 변경 가능
                    contentDescription = "도움말",
                    modifier = Modifier
                        .size(40.dp)
                        // 아이콘 모양 안쪽만 그라데이션으로 칠하기 위해 별도 레이어에서 합성 (SrcIn)
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithCache {
                            // DialStartButton과 같은 135°(좌상단 -> 우하단) 그라데이션 + 같은 색
                            val iconGradient = Brush.linearGradient(
                                colorStops = arrayOf(
                                    0.0f to Color(0xFF6366F1),   // Indigo 500
                                    0.6f to Color(0xFF8B5CF6),   // Violet 500
                                    1.0f to Color(0xFFA855F7),   // Purple 500
                                ),
                                start = Offset.Zero,
                                end = Offset(size.width, size.height)
                            )
                            onDrawWithContent {
                                drawContent()
                                drawRect(brush = iconGradient, blendMode = BlendMode.SrcIn)
                            }
                        },
                    tint = Color.Black // 실제 색은 위 그라데이션이 덮어씀 (불투명하기만 하면 됨)
                )
            }
        }

        // 3. 기존 중앙 정렬 콘텐츠 (Box 안에서 전체 크기를 가지며 스스로 중앙 정렬됨)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(3.dp)) // 토글을 살짝 아래로

            SessionModeToggle(
                mode = sessionMode,
                onModeSelected = { timerViewModel.setSessionMode(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 64.dp) // 좌우 여백을 늘려 토글 폭을 더 줄임
            )

            // Spacer(modifier = Modifier.height(5.dp))

            if (sessionMode == SessionMode.TIMED) {
                TimerDisplay(timeLeft = totalTime)
            } else {
                FreeModeDisplay()
            }

            Spacer(modifier = Modifier.height(10.dp))

            DialStartButton(
                onStart = onStart
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 두 모드의 높이가 달라서 전환할 때 위쪽 요소들(토글, 다이얼 버튼)까지 같이 밀리는 걸 막기 위해
            // 이 블록 전체를 고정 높이 상자에 담고, 내용은 그 안에서 위쪽부터 채움
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 176.dp), // 평소엔 176dp 고정처럼 동작, 글꼴이 커서 내용이 넘칠 때만 늘어나 잘리지 않게
                contentAlignment = Alignment.TopCenter
            ) {
                if (sessionMode == SessionMode.TIMED) {
                    val presetTimes = listOf(1, 3, 5, 10, 15, 20)
                    val selectedMinutes = totalTime / 60
                    val isExactMinute = totalTime % 60 == 0

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 첫 번째 행 (1분, 3분, 5분)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                presetTimes.take(3).forEach { min ->
                                    val isSelected = isExactMinute && selectedMinutes == min
                                    PresetButton(
                                        minutes = min,
                                        isSelected = isSelected,
                                        onClick = { timerViewModel.setTime(min * 60) }, // 선택 시 ViewModel에 초 단위로 저장
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // 두 번째 행 (10분, 15분, 20분)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                presetTimes.drop(3).forEach { min ->
                                    val isSelected = isExactMinute && selectedMinutes == min
                                    PresetButton(
                                        minutes = min,
                                        isSelected = isSelected,
                                        onClick = { timerViewModel.setTime(min * 60) }, // 선택 시 ViewModel에 초 단위로 저장
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 분과 초 계산
                        val minutes = totalTime / 60
                        val seconds = totalTime % 60

                        // 시간에 따라 표시될 텍스트 포맷 설정 (예: "5분 30초", "45초", "5분 0초")
                        val timeText = if (minutes > 0) {
                            if (seconds > 0) "${minutes}분 ${seconds}초" else "${minutes}분 0초" // 0초일 때 생략하고 싶다면 "${minutes}분"으로 변경 가능
                        } else {
                            "${seconds}초"
                        }

                        Button(
                            onClick = { openDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp) // 내부 여백 조절
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween, // 양쪽 끝으로 배치
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "직접 설정",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray
                                )

                                Text(
                                    text = timeText, // 분과 초가 모두 반영된 텍스트
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6366F1) // 보라색(Indigo) 텍스트
                                )
                            }
                        }
                    }
                } else {
                    // 자유 모드: 프리셋/직접 설정 대신 안내 문구만, 상자 중앙에 배치
                    Text(
                        text = "시간 제한 없이 원하는 만큼 머물러 보세요.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                    )
                }
            }

            if (openDialog) {
                var tempSeconds by remember { mutableStateOf(totalTime) }

                BasicAlertDialog(
                    onDismissRequest = {
                        openDialog = false
                    }
                ) {
                    Surface(
                        modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                        shape = MaterialTheme.shapes.large,
                        shadowElevation = 6.dp,
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            MinuteSecondPicker(
                                initialTotalSeconds = totalTime,
                                onTimeChange = { newSeconds -> tempSeconds = newSeconds }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            TextButton(
                                onClick = {
                                    timerViewModel.setTime(tempSeconds)
                                    openDialog = false
                                },
                                enabled = tempSeconds > 0, // 0분 0초는 선택 불가
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    "시간 선택",
                                    color = if (tempSeconds > 0) Color(0xFF6366F1) else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerDisplay(timeLeft: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("%02d : %02d", timeLeft / 60, timeLeft % 60),
            style = MaterialTheme.typography.displayLarge,
            color = Color.DarkGray
        )
    }
}

// 자유 모드일 때 TimerDisplay 자리를 대신하는 표시 (레이아웃 높이를 맞춰서 전환 시 덜컹거리지 않게)
@Composable
fun FreeModeDisplay() {
    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
        Text(
            text = "∞", // ∞
            style = MaterialTheme.typography.displayLarge,
            color = Color.Companion.DarkGray
        )
    }
}

fun angleDeg(v: Offset): Float {
    val rad = atan2(v.y, v.x)
    return (rad * 180f / Math.PI.toFloat())
}

fun smallestAngleDeltaDeg(from: Float, to: Float): Float {
    var delta = to - from
    while (delta > 180f) delta -= 360f
    while (delta < -180f) delta += 360f
    return delta
}

@Composable
fun PresetButton(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 선택되었을 때의 그라데이션 배경
    val selectedButtonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6366F1),   // Indigo 500
            Color(0xFF8B5CF6),   // Violet 500
        )
    )

    // 선택되지 않았을 때의 반투명 배경
    val unselectedColor = Color(0xBFFFFFFF)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp)) // 버튼 끝을 Round하게 처리
            .background(
                if (isSelected) selectedButtonGradient else SolidColor(unselectedColor)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${minutes}분",
            color = if (isSelected) Color.White else Color.Black, // 선택 시 흰색, 비선택 시 검정색 글씨
            fontSize = 16.sp
        )
    }
}