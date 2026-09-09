package com.dhlee.mindpath.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhlee.mindpath.local.MeditationSessionEntity
import com.dhlee.mindpath.local.TouchRecordEntity
import com.dhlee.mindpath.ui.theme.NanumHandwriting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SessionItem(
    session: MeditationSessionEntity,
    index: Int,
    isExpanded: Boolean,
    touchRecords: List<TouchRecordEntity>,
    onClick: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ExpandIconRotation"
    )

    val timeFormatter = remember { SimpleDateFormat("a hh:mm", Locale.KOREAN) }
    val formattedTime = timeFormatter.format(Date(session.startTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "세션 ${index + 1} - $formattedTime",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "접기" else "펼치기",
                    tint = Color.Gray,
                    modifier = Modifier.rotate(rotationState)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            TouchRecordBar(session = session, touchRecords = touchRecords)

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                TouchRecordDetail(session = session, touchRecords = touchRecords)
            }
        }
    }
}

@Composable
private fun TouchRecordDetail(
    session: MeditationSessionEntity,
    touchRecords: List<TouchRecordEntity>,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (session.endTime - session.startTime).coerceAtLeast(1L) / 1000
    val durationText = "${totalSeconds / 60}분 ${totalSeconds % 60}초"

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "알아차림 횟수 : ${touchRecords.size}번",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "소요 시간 : $durationText",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )

        val feelingText = session.feelingRecord
            ?.takeIf { it.isNotBlank() && it != "중도 종료" && it != "소감 생략" }

        if (feelingText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            FeelingQuote(text = feelingText)
        }
    }
}

@Composable
private fun FeelingQuote(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFFF2F6F8))
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(Color(0xFF00B4DB))
        )
        Text(
            text = text,
            color = Color(0xFF37474F),
            fontFamily = NanumHandwriting,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}


@Composable
fun SessionItemPreviewCard(modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(true) }

    val (dummySession, dummyRecords) = remember { createDummySessionData() }

    Box(modifier = modifier) {
        SessionItem(
            session = dummySession,
            index = 0,
            isExpanded = isExpanded,
            touchRecords = dummyRecords,
            onClick = { isExpanded = !isExpanded }
        )
    }
}

private fun createDummySessionData(): Pair<MeditationSessionEntity, List<TouchRecordEntity>> {
    // 오늘 오전 7시 30분에 10분간 명상한 것으로 가정
    val startTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 7)
        set(Calendar.MINUTE, 30)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val durationMs = 10 * 60 * 1000L
    val endTime = startTime + durationMs

    val session = MeditationSessionEntity(
        id = -1L,
        startTime = startTime,
        endTime = endTime,
        feelingRecord = "생각이 자주 떠올랐지만\n그때마다 호흡으로 돌아올 수 있었어요."
    )

    // 전체 시간 대비 비율로 터치 시점 배치
    val touchFractions = listOf(0.08f, 0.15f, 0.29f, 0.34f, 0.48f, 0.55f, 0.71f, 0.83f, 0.91f)
    val records = touchFractions.mapIndexed { index, fraction ->
        TouchRecordEntity(
            id = -(index + 1).toLong(),
            sessionId = -1L,
            touchedTime = startTime + (durationMs * fraction).toLong()
        )
    }

    return session to records
}