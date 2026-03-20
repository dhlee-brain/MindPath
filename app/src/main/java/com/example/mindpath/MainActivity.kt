package com.example.mindpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
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
                //Scaffold의 배경색을 Theme.kt에 정의된 background 색상으로 명시적으로 지정합니다.
                Scaffold(
                    bottomBar = {
                        MyBottomNavigation(navController)
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "meditate",
                        modifier = Modifier
                    ) {
                        composable("meditate") {
                            DialStartScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                        composable("record"){
                            RecordScreen()
                        }
                    }
                }
            }
        }
    }
}