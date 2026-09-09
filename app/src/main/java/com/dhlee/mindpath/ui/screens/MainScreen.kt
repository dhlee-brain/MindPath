package com.dhlee.mindpath.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dhlee.mindpath.ui.components.MyBottomNavigation
import com.dhlee.mindpath.viewmodel.TimerViewModel

// 2. 메인 화면 내부: Scaffold를 치고, 내부 탭은 when 분기로 초고속 전환
@Composable
fun MainScreen(
    onHelpClick: () -> Unit,
    onStart: () -> Unit,
    timerViewModel: TimerViewModel // timerViewModel 인자 추가
) {
    // 현재 어떤 탭이 선택되었는지 기억 (기본값: 명상)
    var currentTab by rememberSaveable { mutableStateOf("meditate") }

    Scaffold(
        bottomBar = {
            MyBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        // containerColor = Color(0xFFFAF5FF),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEEF2FF),  // indigo-50
                            Color(0xFFFAF5FF),  // purple-50
                            Color(0xFFFDF2F8),  // pink-50
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(innerPadding)
        ) {
            // 네비게이션 오버헤드 없이, 상태 변경에 따라 즉시 화면을 갈아끼움
            when (currentTab) {
                "meditate" -> DialStartScreen(
                    modifier = Modifier.fillMaxSize(),
                    onHelpClick = onHelpClick, // 도움말 클릭 이벤트를 위로 전달
                    onStart = onStart,
                    timerViewModel = timerViewModel // DialStartScreen에 timerViewModel 전달
                )
                "record" -> RecordScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
