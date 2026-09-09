package com.dhlee.mindpath

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dhlee.mindpath.ui.components.EyeBlinkTransition
import com.dhlee.mindpath.ui.screens.MainScreen
import com.dhlee.mindpath.ui.screens.OnboardingScreen
import com.dhlee.mindpath.ui.screens.RippleScreen
import com.dhlee.mindpath.viewmodel.SettingsViewModel
import com.dhlee.mindpath.viewmodel.TimerViewModel

// 1. 앱의 최상단 루트: 화면 흐름의 큰 줄기(온보딩 최초 노출 여부 vs 메인)만 관리
@Composable
fun MindPathApp() {
    val timerViewModel: TimerViewModel = viewModel() // TimerViewModel을 최상위에서 생성
    val settingsViewModel: SettingsViewModel = viewModel()

    // null: 아직 DataStore를 못 읽음(아무것도 안 그림) / false: 최초 실행(온보딩) / true: 온보딩 완료(기존 앱)
    val isOnboardingCompleted by settingsViewModel.isOnboardingCompleted.collectAsState()

    when (isOnboardingCompleted) {
        null -> {
            // 아직 값을 모르는 동안엔 아무것도 그리지 않아 깜빡임을 막음
        }

        false -> {
            OnboardingScreen(
                onFinish = {
                    // '건너뛰기'/'시작하기' 둘 다 여기로 옴 -> 온보딩 완료 표시
                    settingsViewModel.completeOnboarding()
                }
            )
        }

        true -> {
            MainNavHost(timerViewModel = timerViewModel)
        }
    }
}

// 2. 온보딩 완료 후 보여지는 기존 앱 화면 흐름
@Composable
private fun MainNavHost(timerViewModel: TimerViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // 도움말 화면 (명상 화면의 도움말 버튼을 통해서만 진입, 바텀바가 없는 전체 화면)
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    navController.popBackStack() // 원래 있던 메인 화면으로 돌아감
                }
            )
        }

        // 메인 화면 덩어리 (내부에 Scaffold와 바텀바를 가짐)
        composable("main") {
            MainScreen(
                onHelpClick = {
                    // 명상 화면 등에서 도움말을 누르면 온보딩 화면(도움말 겸용)으로 이동!
                    navController.navigate("onboarding")
                },
                onStart = {
                    navController.navigate("transition")
                },
                timerViewModel = timerViewModel // MainScreen에 timerViewModel 전달
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
            RippleScreen(
                timerViewModel = timerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
