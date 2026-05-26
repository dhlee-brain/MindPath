package com.example.mindpath.ui.components

import android.R.attr.label
import android.net.http.SslCertificate.restoreState
import android.net.http.SslCertificate.saveState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MyBottomNavigation(currentTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        modifier = Modifier.shadow(elevation = 10.dp),
        containerColor = Color.White
    ) {
        // 명상(Meditate) 탭
        NavigationBarItem(
            selected = currentTab == "meditate",
            onClick = { onTabSelected("meditate") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "명상") },
            label = { Text("명상") }
        )

        // 기록(Record) 탭
        NavigationBarItem(
            selected = currentTab == "record",
            onClick = { onTabSelected("record") },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "기록") },
            label = { Text("기록") }
        )
    }
}