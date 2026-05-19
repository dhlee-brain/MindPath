package com.example.mindpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindpath.ui.components.MyBottomNavigation
import com.example.mindpath.ui.screens.DialStartScreen
import com.example.mindpath.ui.screens.RecordScreen
import com.example.mindpath.ui.theme.MindPathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindPathTheme {
                val navController = rememberNavController()

                // RippleScreen에서 사용된 그라데이션 색상을 가져옵니다.
                // Color.kt에 정의하여 재사용성을 높이는 것을 권장합니다.
                val gradientColors = listOf(
                    Color(0xFF00B4DB), // 상단
                    Color.White  // 하단
                )

                Scaffold(
                    bottomBar = {
                        MyBottomNavigation(navController)
                    },
                    // Scaffold 자체의 컨테이너 색상은 투명하게 설정하여 아래 그라데이션이 완전히 보이도록 합니다.
                    containerColor = Color.White,
                    // Scaffold의 modifier에 background를 직접 적용하는 것은
                    // 내부 Surface에 의해 가려질 수 있으므로, 콘텐츠 람다 내부 Box에 적용합니다.
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // 1. 그라데이션 배경을 적용할 Box를 생성합니다.
                    // 이 Box가 Scaffold의 전체 콘텐츠 영역을 차지하고 패딩을 받습니다.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding) // Scaffold의 BottomBar 공간을 확보합니다.
                            // .background(Brush.verticalGradient(gradientColors)) // 여기에 그라데이션을 적용!
                    ) {
                        // 2. 이 그라데이션 Box 위에 NavHost를 배치합니다.
                        // NavHost는 이제 이 Box의 전체 크기를 채웁니다.
                        NavHost(
                            navController = navController,
                            startDestination = "meditate",
                            modifier = Modifier.fillMaxSize() // Box의 전체 크기를 채웁니다.
                        ) {
                            composable("meditate") {
                                DialStartScreen(
                                    modifier = Modifier.fillMaxSize() // DialStartScreen은 부모(NavHost)를 채웁니다.
                                )
                            }
                            composable("record"){
                                RecordScreen(
                                    modifier = Modifier.fillMaxSize() // RecordScreen은 부모(NavHost)를 채웁니다.
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}