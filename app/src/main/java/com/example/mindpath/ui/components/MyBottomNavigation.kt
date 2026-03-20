package com.example.mindpath.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MyBottomNavigation(navController: NavHostController) {
    // 현재 네비게이션 상태를 관찰하여 아이콘의 선택 상태를 결정합니다.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    NavigationBar {
        // 명상(Meditate) 탭
        NavigationBarItem(
            selected = currentDestination == "meditate",
            onClick = {
                navController.navigate("meditate") {
                    // 백스택에 화면이 쌓이지 않도록 정리
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "명상") },
            label = { Text("명상") }
        )

        // 기록(Record) 탭
        NavigationBarItem(
            selected = currentDestination == "record",
            onClick = {
                navController.navigate("record") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "기록") },
            label = { Text("기록") }
        )
    }
}