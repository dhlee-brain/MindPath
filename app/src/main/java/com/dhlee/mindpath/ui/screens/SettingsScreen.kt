package com.dhlee.mindpath.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dhlee.mindpath.BuildConfig
import com.dhlee.mindpath.R
import com.dhlee.mindpath.ui.components.vibrateEndAlert
import com.dhlee.mindpath.viewmodel.EndAlert
import com.dhlee.mindpath.viewmodel.SettingsViewModel

private val AccentColor = Color(0xFF6366F1) // Indigo 500 (앱 공통 포인트 색)

@Composable
fun SettingsScreen(
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val endAlert by settingsViewModel.endAlert.collectAsState()
    val context = LocalContext.current

    // 소리 포함 항목을 고를 때 들려줄 싱잉볼 미리듣기 플레이어 (화면에 하나만 두고 재사용)
    val previewPlayer = remember { MediaPlayer.create(context, R.raw.singing_bowl) }
    // 다른 탭으로 이동하는 등 설정 화면이 사라지면 소리를 멈추고 리소스 해제
    DisposableEffect(Unit) {
        onDispose { previewPlayer?.release() }
    }

    // 화면이 작거나 글꼴이 커도 아래 항목이 잘리지 않도록 스크롤 가능
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 기록 탭의 "기록 모아보기"와 같은 제목 스타일
        Text(
            text = "설정",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // 1. 종료 알림
        SettingsSection(title = "종료 알림") {
            Text(
                text = "시간 모드에서 세션이 끝나면 이렇게 알려드려요.",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Column(modifier = Modifier.selectableGroup()) { // 스크린리더에 "선택지 묶음"으로 알림
                EndAlert.entries.forEach { option ->
                    EndAlertOptionRow(
                        label = option.label,
                        selected = endAlert == option,
                        onClick = {
                            settingsViewModel.setEndAlert(option)

                            // 고른 항목의 실제 종료 알림을 미리 들려주고/느끼게 함
                            previewPlayer?.let { player ->
                                if (option.hasSound) {
                                    player.seekTo(0) // 이미 재생 중이면 처음부터 다시
                                    player.start()
                                } else if (player.isPlaying) {
                                    player.pause() // 소리 없는 항목(진동/없음)으로 바꾸면 미리듣기 중단
                                }
                            }
                            if (option.hasVibration) vibrateEndAlert(context)
                        }
                    )
                }
            }
        }

        // 2. 도움말 (명상 화면 우측 상단 도움말과 같은 온보딩 화면으로 이동)
        SettingsSection(title = "도움말") {
            SettingsNavigationRow(label = "사용법 다시 보기", onClick = onHelpClick)
        }

        // 3. 앱 정보
        SettingsSection(title = "앱 정보") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("버전", style = MaterialTheme.typography.bodyLarge)
                Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// 섹션 제목 + 흰 카드 (기록 탭의 요약 카드와 같은 톤)
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        color = AccentColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

// 라디오 버튼 한 줄 — 동그라미뿐 아니라 줄 전체를 눌러도 선택됨
@Composable
private fun EndAlertOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // 클릭은 Row의 selectable이 처리 (중복 처리 방지)
            colors = RadioButtonDefaults.colors(selectedColor = AccentColor),
            modifier = Modifier.padding(12.dp)
        )
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

// 다른 화면으로 이동하는 한 줄 (오른쪽 화살표)
@Composable
private fun SettingsNavigationRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
