package com.example.mindpath.ui.screens

import android.R.attr.thickness
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindpath.R
import com.example.mindpath.ui.components.DialStartButton
import com.example.mindpath.ui.components.FeelingInputPreviewCard
import com.example.mindpath.ui.components.RipplePreviewCard
import com.example.mindpath.ui.components.SessionItemPreviewCard
import com.example.mindpath.ui.theme.NanumHandwriting

sealed interface OnboardingPageData {
    val content: @Composable () -> Unit

    data class ImagePage(
        val imageRes: Int,
        override val content: @Composable () -> Unit
    ) : OnboardingPageData

    data class CardPage(
        val horizontalPadding: Dp = 30.dp,
        val alignCardToTop: Boolean = false,
        val card: @Composable () -> Unit,
        override val content: @Composable () -> Unit
    ) : OnboardingPageData
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontFamily = NanumHandwriting,
            fontSize = 20.sp,
            lineHeight = 30.sp
        )
    ) {
        val pages = remember {
            listOf(
                OnboardingPageData.ImagePage(R.drawable.description_image_1) {
                    Text("감당하기 어려운 감정이 밀려올 때,")
                    Spacer(modifier = Modifier.height(15.dp))
                    Text("또는 잠시 방해받지 않는\n순간을 창조하고 싶을 때가\n있지는 않으신가요?")
                },

                // 2-1. 휠 — 기본 여백 24dp 사용
                OnboardingPageData.CardPage(
                    card = {
                        DialStartButton(
                            onStart = {},
                            diameter = 280.dp
                        )
                    }
                ) {
                    Text("원형 버튼을 360도\n시계 / 시계 반대 방향으로 회전하여\n세션을 시작해보세요.")
                },

                // 2-2. 파동 — 화면 폭 전체
                OnboardingPageData.CardPage(
                    horizontalPadding = 0.dp,
                    alignCardToTop = true,
                    card = { RipplePreviewCard(modifier = Modifier.fillMaxHeight(0.7f)) }
                ) {
                    Text("세션 시작 후 - 부정적인 생각이 날 때,")
                    Text("또는 생각에 빠져있다가 알아차렸을 때\n화면을 터치해 보세요.")
                    Text("그와 동시에 다시 호흡으로 돌아와 보세요.")
                },

                // 3. 소감 입력 미리보기
                OnboardingPageData.CardPage(
                    card = {
                        FeelingInputPreviewCard(
                            feeling = "오늘은 조금 편안했어요",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                ) {
                    Text("설정한 시간의 세션이 종료한 뒤에는")
                    Text("간단하게 소감을 기록할 수 있습니다.")
                },

                OnboardingPageData.CardPage(
                    card = { SessionItemPreviewCard(modifier = Modifier.fillMaxWidth()) }
                ) {
                    Text("세션 정보와 알아차림을 터치한 시간은 기록되어")
                    Text("리포트로 확인할 수 있어요.")
                },

                OnboardingPageData.CardPage(
                    card = {
                        RotatingPetals()
                    }
                ) {
                    Text("바쁘고 쉴틈 없는 일상 속,")
                    Text("쉬고 싶을 땐 잠시라도 <마음의 길>에서 머물다 가시길 바래요.")
                },
            )
        }

        val pagerState = rememberPagerState(pageCount = { pages.size })

        // Box를 사용해 인디케이터 위치를 절대적으로 고정
        Box(modifier = Modifier.fillMaxSize()) {
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
                    .height(150.dp) // 원하는 높이로 고정
                    .padding(horizontal = 15.dp)
                    .align(Alignment.BottomCenter)
            ) {
                // 1. 인디케이터: Box의 중앙에 배치
                PageIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.Center) // Box의 정중앙
                )

                // 2. 버튼 영역: Box의 우측 중앙에 배치
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
}

@Composable
fun OnboardingPageLayout(pageData: OnboardingPageData) {
    when (pageData) {
        is OnboardingPageData.ImagePage -> ImagePageLayout(pageData)
        is OnboardingPageData.CardPage -> CardPageLayout(pageData)
    }
}

@Composable
private fun ImagePageLayout(pageData: OnboardingPageData.ImagePage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(pageData.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(0.8f)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        Column(
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center
        ) { pageData.content() }
    }

}

@Composable
private fun CardPageLayout(pageData: OnboardingPageData.CardPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(bottom = 150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (pageData.alignCardToTop) Arrangement.Top
        else Arrangement.Center
    ) {
        Box(modifier = Modifier.padding(horizontal = pageData.horizontalPadding)) {
            pageData.card()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)
        ) {
            pageData.content()
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
fun RotatingPetals(modifier: Modifier = Modifier) {
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
            .size(350.dp)
            .rotate(angle)
    )
}