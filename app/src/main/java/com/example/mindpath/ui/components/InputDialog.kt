package com.example.mindpath.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onDismiss: () -> Unit
) {
    var feeling by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        // 🌟 작성 중 밖을 터치해서 날아가는 참사를 막음 (이전 대화에서 적용했던 속성)
        properties = DialogProperties(dismissOnClickOutside = false),
        title = {
            Text("명상이 끝났습니다.", color = TextPrimaryColor)
        },
        text = {
            Column {
                Text("지금의 감각이나 감정을 짧게 남겨보세요.", color = TextSecondaryColor)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = feeling,
                    onValueChange = { feeling = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextFieldBgColor,   // 바탕보다 덜 어두운 색
                        unfocusedContainerColor = TextFieldBgColor, // 바탕보다 덜 어두운 색
                        focusedTextColor = TextPrimaryColor,
                        unfocusedTextColor = TextPrimaryColor,
                        cursorColor = TextPrimaryColor,
                        focusedIndicatorColor = Color.Transparent,  // 밑줄 제거로 깔끔하게
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(feeling) }) {
                Text("완료", color = Color(0xFF64FFDA))
            }
        },
        containerColor = DialogBgColor
    )
}