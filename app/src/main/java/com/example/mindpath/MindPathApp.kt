package com.example.mindpath

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindpath.ui.components.EyeBlinkTransition
import com.example.mindpath.ui.components.MyBottomNavigation
import com.example.mindpath.ui.screens.DialStartScreen
import com.example.mindpath.ui.screens.MainScreen
import com.example.mindpath.ui.screens.OnboardingScreen
import com.example.mindpath.ui.screens.RecordScreen
import com.example.mindpath.ui.screens.RippleScreen

// 1. 앱의 최상단 루트: 화면 흐름의 큰 줄기(온보딩 vs 메인)만 관리
@Composable
fun MindPathApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "onboarding" // 앱 시작은 온보딩
    ) {
        // 온보딩 겸 도움말 화면 (바텀바가 없는 전체 화면)
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    // 이전 백스택이 있다면? -> 메인에서 '도움말' 버튼을 타고 온 것임
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack() // 원래 있던 메인 화면으로 돌아감
                    } else {
                        // 이전 백스택이 없다면? -> 앱을 처음 켜서 온보딩을 본 것임
                        navController.navigate("main") {
                            popUpTo("onboarding") { inclusive = true } // 백스택에서 온보딩을 지움
                        }
                    }
                }
            )
        }

        // 메인 화면 덩어리 (내부에 Scaffold와 바텀바를 가짐)
        composable("main") {
            MainScreen(
                onHelpClick = {
                    // 명상 화면 등에서 도움말을 누르면 최상위 온보딩 화면으로 이동!
                    navController.navigate("onboarding")
                },
                onStart = {
                    navController.navigate("transition")
                }
            )
        }

        composable("transition") {
            EyeBlinkTransition (
                onComplete = {
                    navController.navigate("ripple") {
                        popUpTo("transition") {
                            inclusive = true // 'transition' 목적지 자체도 제거합니다.
                        }
                    }
                }
            )
        }

        composable("ripple") {
            RippleScreen()
        }
    }
}
