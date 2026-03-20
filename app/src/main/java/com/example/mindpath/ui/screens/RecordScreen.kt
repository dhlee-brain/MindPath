package com.example.mindpath.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindpath.viewmodel.MeditationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    viewModel: MeditationViewModel = viewModel(factory = MeditationViewModel.Factory)
) {
    val allSessions by viewModel.allSessions.collectAsState()
    val touchRecords by viewModel.selectedSessionTouchRecords.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllSessions()
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 상단 영역: MeditationSession 목록
        Text(
            text = "명상 기록",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(allSessions) { session ->
                SessionItem(
                    session = session,
                    onClick = { viewModel.loadTouchRecords(session.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        HorizontalDivider(thickness = 2.dp, color = Color.Gray)

        // 하단 영역: 선택된 세션의 TouchRecord 목록
        Text(
            text = "알아차림 기록",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(touchRecords) { record ->
                TouchRecordItem(record = record)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun SessionItem(session: com.example.mindpath.local.MeditationSessionEntity, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Session ID: ${session.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Start: ${sdf.format(Date(session.startTime))}")
            Text(text = "Feeling: ${session.feelingRecord ?: "None"}")
        }
    }
}

@Composable
fun TouchRecordItem(record: com.example.mindpath.local.TouchRecordEntity) {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(text = "Touched at: ${sdf.format(Date(record.touchedTime))}")
        }
    }
}
