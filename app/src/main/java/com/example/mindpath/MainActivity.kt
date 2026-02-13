package com.example.mindpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mindpath.ui.theme.MindPathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindPathTheme {
                // Scaffold를 사용하여 전체적인 화면 구조를 잡습니다.
                Scaffold { innerPadding ->
                    // DialStartScreen을 호출하고, 화면 전체를 채우도록 Modifier를 전달합니다.
                    DialStartScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding) // 시스템 UI(상태바 등) 영역을 제외
                    )
                }
            }
        }
    }
}