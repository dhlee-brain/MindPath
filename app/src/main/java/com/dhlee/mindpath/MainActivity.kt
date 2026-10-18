package com.dhlee.mindpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dhlee.mindpath.ui.theme.MindPathTheme

// 스플래시 화면을 최소 이 시간(ms)만큼 띄워둠
private const val SPLASH_MIN_DURATION_MS = 1000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val splashStartTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            System.currentTimeMillis() - splashStartTime < SPLASH_MIN_DURATION_MS
        }

        setContent {
            MindPathTheme {
                MindPathApp()
            }
        }
    }
}