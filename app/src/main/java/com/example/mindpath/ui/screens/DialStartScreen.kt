package com.example.mindpath.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindpath.ui.components.DialStartButton
import com.example.mindpath.ui.components.MinuteSecondPicker
import com.example.mindpath.ui.theme.Grey100
import com.example.mindpath.ui.theme.Grey200
import com.example.mindpath.viewmodel.MeditationViewModel
import com.example.mindpath.viewmodel.TimerViewModel
import kotlin.math.atan2


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialStartScreen(
    modifier: Modifier = Modifier,
    onHelpClick: () -> Unit,
    onStart: () -> Unit,
    timerViewModel: TimerViewModel = viewModel()
) {
    val isRunning by timerViewModel.isTimerRunning.collectAsState()
    val totalTime by timerViewModel.totalTime.collectAsState()
    var openDialog by remember { mutableStateOf(false) }


    // 1. Box를 최상위 레이아웃으로 사용하여 겹치기 및 절대 위치 지정 허용
    Box(modifier = modifier.fillMaxSize()) {

        // 2. 우측 상단 도움말 버튼 추가
        IconButton(
            onClick = onHelpClick,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopEnd) // 우측 상단 정렬
                .padding(16.dp) // 화면 가장자리와의 여백
        ) {
            Icon(
                imageVector = Icons.Default.Info, // HelpOutline이나 다른 아이콘으로 변경 가능
                contentDescription = "도움말",
                modifier = Modifier.fillMaxSize(),
                tint = Color.Gray // 앱 테마에 맞게 색상 조절
            )
        }

        // 3. 기존 중앙 정렬 콘텐츠 (Box 안에서 전체 크기를 가지며 스스로 중앙 정렬됨)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            TimerDisplay(timeLeft = totalTime)

            Spacer(modifier = Modifier.height(10.dp))

            DialStartButton(
                onStart = onStart
            )

            Spacer(modifier = Modifier.height(20.dp))

            val presetTimes = listOf(1, 3, 5, 10, 15, 20)
            val selectedMinutes = totalTime / 60
            val isExactMinute = totalTime % 60 == 0

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
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("시간 선택", color = Color(0xFF6366F1))
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
    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
        Text(
            text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
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