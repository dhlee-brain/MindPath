package com.example.mindpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mindpath.ui.theme.MindPathTheme
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setOnExitAnimationListener { provider ->
            ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f).apply {
                duration = 250L
                interpolator = DecelerateInterpolator()
                doOnEnd { provider.remove() }   // ← 빼먹으면 화면이 멈춥니다
                start()
            }
        }
        enableEdgeToEdge()
        setContent {
            MindPathTheme {
                MindPathApp()
            }
        }

    }
}