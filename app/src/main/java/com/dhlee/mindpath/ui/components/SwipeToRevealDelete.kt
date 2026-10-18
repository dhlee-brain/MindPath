package com.dhlee.mindpath.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val DeleteButtonSize = 48.dp
private val DeleteButtonEndPadding = 12.dp
private val RevealWidth = DeleteButtonSize + DeleteButtonEndPadding * 2  // 버튼 양옆 여백 포함

/**
 * 오른쪽 -> 왼쪽으로 스와이프하면 content가 밀리고, 뒤에 숨어 있던 빨간 원형 삭제 버튼이 드러남.
 * 드러난 상태에서 content를 탭하면 다시 닫히고, 삭제 버튼을 누르면 [onDelete]가 즉시 호출됨.
 *
 * ## 위치 상태를 둘로 나눈 이유 (레이스 방지)
 * - [dragOffsetPx]: 드래그 중 손가락을 그대로 따라가는 값. 평범한 Compose state라 대입이
 *   코루틴을 거치지 않고 즉시(동기적으로) 반영됨.
 * - [settledOffset]: 손을 뗀 뒤(또는 탭으로 닫을 때) 0 / -revealPx로 스프링 애니메이션되는 값.
 *   `onDragStarted`/`onDragStopped`는 draggable이 만드는 '단 하나의' 제스처 코루틴에서 순서대로
 *   호출되므로, 이 값을 건드리는 시점이 항상 드래그 흐름과 같은 순서로 보장됨.
 *
 * 이전 구현은 매 delta마다 `rememberCoroutineScope`로 새 코루틴을 띄워 하나의 `Animatable`에
 * `snapTo`를 불렀다. `Animatable`은 내부 `MutatorMutex`가 "나중에 도착한 호출이 이전 걸 취소하고
 * 가져간다"는 규칙으로 동작하는데, 손을 뗀 시점 전후로 아직 디스패치되지 않은 delta 코루틴이
 * `onDragStopped`가 시작한 정착 애니메이션보다 늦게 실행되면 — 그 애니메이션을 끊어버리고
 * 드래그 중이던 어중간한 좌표에 값을 고정시켜 버리는 레이스가 있었음.
 * 지금 구조는 드래그 중엔 `Animatable`을 전혀 건드리지 않으므로 그 레이스가 원천적으로 불가능함.
 */
@Composable
fun SwipeToRevealDelete(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val revealPx = with(LocalDensity.current) { RevealWidth.toPx() }

    var dragOffsetPx by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val settledOffset = remember { Animatable(0f) }
    var isRevealed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 화면에 실제로 적용할 오프셋: 드래그 중이면 손가락 추적값, 아니면 정착값
    val offsetPx = if (isDragging) dragOffsetPx else settledOffset.value

    val closeReveal = {
        isRevealed = false
        scope.launch {
            settledOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
        }
        Unit
    }

    Box(modifier = modifier) {
        // 뒤에 가려져 있는 삭제 버튼
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .padding(end = DeleteButtonEndPadding)
                    .size(DeleteButtonSize)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable(enabled = isRevealed) { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "세션 기록 삭제",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 스와이프로 밀리는 본문
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetPx.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // 코루틴을 거치지 않는 동기 대입 -> 다른 코루틴과 경쟁할 여지가 없음
                        dragOffsetPx = (dragOffsetPx + delta).coerceIn(-revealPx, 0f)
                    },
                    onDragStarted = {
                        // 현재 정착 위치(열림/닫힘 혹은 애니메이션 중간값)에서 이어서 드래그 시작
                        dragOffsetPx = settledOffset.value
                        isDragging = true
                    },
                    onDragStopped = { velocity ->
                        // 손을 뗀 순간에만 결정: 빠르게 튕겼거나, 절반 이상 밀린 상태면 끝까지 열림
                        val shouldReveal = velocity < -300f ||
                                (velocity <= 300f && dragOffsetPx < -revealPx / 2)
                        isRevealed = shouldReveal

                        // 드래그가 멈춘 지점에서 정착값을 그대로 이어받고(점프 없음),
                        // 그 다음에 '정착값 모드'로 전환 -> 전환 순간 시각적 끊김도 없음
                        settledOffset.snapTo(dragOffsetPx)
                        isDragging = false

                        settledOffset.animateTo(
                            targetValue = if (shouldReveal) -revealPx else 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                )
        ) {
            content()

            // 열린 상태에서는 본문 탭이 '닫기'로 동작 (펼치기/접기보다 우선)
            if (isRevealed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures { closeReveal() }
                        }
                )
            }
        }
    }
}
