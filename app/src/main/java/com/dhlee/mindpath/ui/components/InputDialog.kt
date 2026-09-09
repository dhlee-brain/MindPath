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
// 다이얼로그 (1): 백 버튼/제스처 시 뜨는 [중도 종료 확인 창]
// ---------------------------------------------------------
@Composable
fun ExitMeditationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("명상을 종료하시겠어요?", color = TextPrimaryColor)
        },
        text = {
            Text("지금 종료해도 알아차림 기록은 저장됩니다.", color = TextSecondaryColor)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인", color = Color(0xFF64FFDA)) // 민트색 포인트 컬러
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondaryColor)
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
) {
    var feeling by remember { mutableStateOf("") }

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
        Column(modifier = Modifier.padding(24.dp)) {
            FeelingInputTitle()
            Spacer(modifier = Modifier.height(16.dp))
            FeelingInputContent(
                feeling = feeling,
                onFeelingChange = {},
                enabled = false,
                minLines = 3, // 미리보기 카드라 5줄까지는 필요 없음 — 온보딩에서 잘림 방지
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                FeelingInputConfirmButton(onConfirm = {}, enabled = false)
            }
        }
    }
}


@Composable
private fun FeelingInputTitle() {
    Text("세션이 종료되었어요", color = TextPrimaryColor)
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
        Text("지금의 감각이나 감정을 짧게 남겨보세요.", color = TextSecondaryColor)
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
