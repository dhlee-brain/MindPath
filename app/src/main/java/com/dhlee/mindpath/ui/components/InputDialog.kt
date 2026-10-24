package com.dhlee.mindpath.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

// 공통으로 사용할 색상 정의 (명상 앱 무드)
val DialogBgColor = Color(0xFF0A192F)       // 다크 네이비 바탕
val TextFieldBgColor = Color(0xFF172A45)    // 입력창 (바탕보다 살짝 밝은 톤)
val TextPrimaryColor = Color.White.copy(alpha = 0.9f)
val TextSecondaryColor = Color.LightGray

// ---------------------------------------------------------
// 다이얼로그 (1): 백 버튼/제스처 시 뜨는 [마치기 + 소감 창]
// 시간 모드 중도 종료 / 자유 모드 종료 모두 이 창 하나로 처리 (확인 + 소감을 한 번에)
// ---------------------------------------------------------
@Composable
fun FinishMeditationDialog(
    feeling: String,
    onFeelingChange: (String) -> Unit,
    onFinish: () -> Unit,
    onContinue: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onContinue, // 다이얼로그 위에서 한 번 더 뒤로가기 = 계속하기
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text("세션을 종료할까요?", color = TextPrimaryColor) },
        text = {
            FeelingInputContent(
                feeling = feeling,
                onFeelingChange = onFeelingChange,
                enabled = true,
            )
        },
        confirmButton = {
            TextButton(onClick = onFinish) {
                Text("마치기", color = Color(0xFF64FFDA)) // 민트색 포인트 컬러
            }
        },
        dismissButton = {
            TextButton(onClick = onContinue) {
                Text("계속하기", color = TextSecondaryColor)
            }
        },
        containerColor = DialogBgColor
    )
}

// ---------------------------------------------------------
// 다이얼로그 (2): 타이머 완전 종료 시 뜨는 [소감 기록 창]
// ---------------------------------------------------------
@Composable
fun FeelingInputDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    enabled: Boolean,
    initialFeeling: String = "", // 마치기 창에서 쓰던 중에 타이머가 끝난 경우, 쓰던 내용을 이어받기 위함
) {
    var feeling by remember { mutableStateOf(initialFeeling) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { FeelingInputTitle() },
        text = {
            FeelingInputContent(
                feeling = feeling,
                onFeelingChange = { feeling = it },
                enabled = enabled,
            )
        },
        confirmButton = { FeelingInputConfirmButton(onConfirm = { onConfirm(feeling) }) },
        containerColor = DialogBgColor
    )
}


@Composable
fun FeelingInputPreviewCard(
    feeling: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = DialogBgColor,
        shape = MaterialTheme.shapes.extraLarge,
        shadowElevation = 6.dp,
    ) {
        // AlertDialog가 슬롯마다 내부적으로 지정하는 글꼴 스타일/여백을 그대로 맞춤
        // (제목: headlineSmall / 본문·입력창: bodyMedium / 제목 아래 16dp, 본문 아래 24dp)
        Column(modifier = Modifier.padding(24.dp)) {
            ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                FeelingInputTitle()
            }
            Spacer(modifier = Modifier.height(16.dp))
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                FeelingInputContent(
                    feeling = feeling,
                    onFeelingChange = {},
                    enabled = false,
                    minLines = 3, // 미리보기 카드라 5줄까지는 필요 없음 — 온보딩에서 잘림 방지
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                FeelingInputConfirmButton(onConfirm = {}, enabled = false)
            }
        }
    }
}


@Composable
private fun FeelingInputTitle() {
    Text("세션이 종료되었어요.", color = TextPrimaryColor)
}

@Composable
private fun FeelingInputContent(
    feeling: String,
    onFeelingChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    minLines: Int = 5,
) {
    Column(modifier = modifier) {
        Text("지금의 느낌이나 감정을 짧게 남겨보세요.", color = TextSecondaryColor)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            enabled = enabled,
            value = feeling,
            onValueChange = onFeelingChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldBgColor,
                unfocusedContainerColor = TextFieldBgColor,
                focusedTextColor = TextPrimaryColor,
                unfocusedTextColor = TextPrimaryColor,
                cursorColor = TextPrimaryColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledContainerColor = TextFieldBgColor,
                disabledTextColor = TextPrimaryColor,
                disabledIndicatorColor = Color.Transparent,
            ),
            shape = MaterialTheme.shapes.medium,
            minLines = minLines,
            maxLines = 8,
        )
    }
}

@Composable
private fun FeelingInputConfirmButton(
    onConfirm: () -> Unit,
    enabled: Boolean = true,
) {
    TextButton(onClick = onConfirm, enabled = enabled) {
        Text("완료", color = Color(0xFF64FFDA))
    }
}
