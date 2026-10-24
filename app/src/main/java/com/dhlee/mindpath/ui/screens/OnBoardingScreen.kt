package com.dhlee.mindpath.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhlee.mindpath.R
import com.dhlee.mindpath.ui.components.DialStartButton
import com.dhlee.mindpath.ui.components.FeelingInputPreviewCard
import com.dhlee.mindpath.ui.components.RipplePreviewCard
import com.dhlee.mindpath.ui.components.SessionItemPreviewCard
import com.dhlee.mindpath.ui.theme.NanumHandwriting

// 하단 페이지 인디케이터 + 건너뛰기/시작하기 버튼이 차지하는 높이
private val OnboardingBottomBarHeight = 64.dp

sealed interface OnboardingPageData {
    val content: @Composable () -> Unit

    data class CardPage(
        val card: @Composable () -> Unit,
        override val content: @Composable () -> Unit
    ) : OnboardingPageData
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    // 파동 미리보기 페이지들(3~5)이 함께 쓰는 알아차림 횟수
    // pages가 remember로 한 번만 만들어지므로, 값이 아닌 State 객체 자체를 람다가 캡처해야 최신 값이 반영됨
    val rippleCount = remember { mutableIntStateOf(0) }

    val pages = remember {
        listOf(
            OnboardingPageData.CardPage(
                card = {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        RotatingPetals(size = minOf(350.dp, maxHeight * 0.7f))
                    }
                }
            ) {
                Text("감당하기 어려운 감정이 밀려올 때,")
                Spacer(modifier = Modifier.height(15.dp))
                Text("또는 잠시 방해받지 않는\n순간을 창조하고 싶을 때가\n있지는 않으신가요?")
            },

            // 2-1. 휠 — 카드 영역 안에서 중앙, 고정 크기(280dp)를 기본으로 하되 좁으면 줄어듦
            OnboardingPageData.CardPage(
                card = {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        DialStartButton(
                            onStart = {},
                            diameter = minOf(280.dp, maxHeight * 0.7f)
                        )
                    }
                }
            ) {
                Text("원형 버튼을 360도\n시계 / 시계 반대 방향으로 회전하여\n세션을 시작해보세요.")
                Text("\n시간 모드로 제한 시간동안 세션을 진행하거나,\n자유 모드로 시간 제한 없이 시작해 보세요.")
            },

            // 2-2. 파동 — 쉐이더라 카드 영역을 가장자리까지 꽉 채움
            OnboardingPageData.CardPage(
                card = {
                    RipplePreviewCard(
                        count = rippleCount.intValue,
                        onTap = { rippleCount.intValue++ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            ) {
                Text("명상 세션 중 화면이에요.", fontSize = 20.sp)
                Text("먼저 천천히, 부드럽게 -", fontSize = 20.sp)
                Text("호흡이나 몸의 감각을 알아차려 보세요.\n", fontSize = 20.sp)
                Text("호흡이 거칠거나 부드러운가요?", fontSize = 20.sp)
                Text("어딘가 긴장하고 있었던 몸의 부위가 있지는 않은가요?", fontSize = 20.sp)
            },

            OnboardingPageData.CardPage(
                card = {
                    RipplePreviewCard(
                        count = rippleCount.intValue,
                        onTap = { rippleCount.intValue++ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            ) {
                Text("얼마 지나지 않아 생각에 빠져버려요.")
                Text("생각에 빠져있는 걸 알아차렸을 때,\n화면을 터치해 보세요.")
                Text("그러면서 다시금 호흡이나 몸의 감각으로\n주의를 돌아와 보세요.")
            },

            OnboardingPageData.CardPage(
                card = {
                    RipplePreviewCard(
                        count = rippleCount.intValue,
                        onTap = { rippleCount.intValue++ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            ) {
                Text("생각이 났을 때,\n그 순간 호흡이나 몸의 감각에 주의가 있었다면\n화면을 터치하지 않아도 돼요.")
                Text("생각에 빠져있다가, '딴생각하고 있었구나!' 를 나중에 알아차릴 때만\n화면을 터치해서 지금으로 돌아와 보세요.")
            },

            // 3. 소감 입력 미리보기
            OnboardingPageData.CardPage(
                card = {
                    FeelingInputPreviewCard(
                        feeling = "오늘은 조금 편안했어요",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                    )
                }
            ) {
                Text("설정한 시간의 세션이 종료된 뒤에는")
                Text("간단하게 소감을 기록할 수 있습니다.")
                Text("\n불안한 감정이 느껴졌나요?")
                Text("딴생각이 많이 나서 산만했나요?")
            },

            OnboardingPageData.CardPage(
                card = {
                    SessionItemPreviewCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                    )
                }
            ) {
                Text("세션 정보와 화면을 터치한 시간은")
                Text("기록되어 리포트로 확인할 수 있어요.")
                Text("\n얼마나 많은 딴생각을 했는지 -\n시간 바로 직접 확인해 보세요.")
            },

            OnboardingPageData.CardPage(
                card = {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        RotatingPetals(size = minOf(350.dp, maxHeight * 0.7f))
                    }
                }
            ) {
                Text("바쁘고 쉴틈 없는 일상 속,")
                Text("정신없던 마음을 들여다 보고 싶을 때 -")
                Text("잠시라도 <마음의 길>에서 머물다 가시길 바라요.")
            },
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Box를 사용해 인디케이터 위치를 절대적으로 고정
    // safeDrawingPadding: 상태바/내비게이션 바(제스처든 3버튼이든)에 콘텐츠가 가려지지 않도록
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                OnboardingPageLayout(pages[pageIndex])
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OnboardingBottomBarHeight)
                .padding(horizontal = 15.dp)
                .align(Alignment.BottomCenter)
        ) {
            // 1. 인디케이터: Box의 정중앙에 배치
            PageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.align(Alignment.Center)
            )

            // 2. 버튼 영역: Box의 우측 중앙에 배치 (인디케이터와 세로 중심선을 맞추기 위해 Bottom 대신 Center 정렬)
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                val isLastPage = pagerState.currentPage == pages.size - 1

                TextButton(
                    onClick = onFinish,
                ) {
                    Text(
                        text = if (isLastPage) "시작하기" else "건너뛰기",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF005C97)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageLayout(pageData: OnboardingPageData) {
    when (pageData) {
        is OnboardingPageData.CardPage -> CardPageLayout(pageData)
    }
}

// 카드 영역(위) : 텍스트 영역(아래) = 60 : 40 으로 모든 페이지 통일
@Composable
private fun CardPageLayout(pageData: OnboardingPageData.CardPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(bottom = OnboardingBottomBarHeight), // 하단 인디케이터/버튼과 안 겹치도록
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            pageData.card()
        }

        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp), // 모든 페이지 동일한 여백으로 시작점 통일
            verticalArrangement = Arrangement.Top // 줄 수와 무관하게 첫 줄이 항상 같은 위치에서 시작
        ) {
            // 손글씨 글꼴은 안내 문구에만 적용 (카드 영역은 앱 기본 글꼴 유지)
            // 나눔손글씨는 같은 sp라도 글자가 작게 그려져서 기본(16/24)보다 키움
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(
                    fontFamily = NanumHandwriting,
                    fontSize = 22.sp,
                    lineHeight = 30.sp
                )
            ) {
                pageData.content()
            }
        }
    }
}

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        repeat(pageCount) { index ->
            val color = if (currentPage == index) Color.DarkGray else Color.LightGray
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun RotatingPetals(modifier: Modifier = Modifier, size: Dp = 350.dp) {
    val transition = rememberInfiniteTransition(label = "petals")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Image(
        painter = painterResource(R.drawable.ic_splash_lotus),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .rotate(angle)
    )
}
