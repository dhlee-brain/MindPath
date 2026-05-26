package com.example.mindpath.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mindpath.R

data class OnboardingPageData(
    val imageRes: Int,
    val content: @Composable () -> Unit // 텍스트 영역을 직접 작성할 수 있게 함
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPageData(R.drawable.description_image_1) {
                Text("감당하기 어려운 감정이 밀려올 때,") // 원하는 곳에서 줄바꿈
                Spacer(modifier = Modifier.height(15.dp))
                Text("또는 잠시 방해받지 않는\n순간을 창조하고 싶을 때가\n있지는 않으신가요?")
            },
            OnboardingPageData(R.drawable.description_image_2) {
                Text("명상 세션을 시작해 보세요.\n")
                Text("세션 시작 후 - 부정적인 생각이 날 때,")
                Text("또는 생각에 빠져있다가 알아차렸을 때\n화면을 터치해 보세요.")
                Text("그와 동시에 다시 호흡으로 돌아와 보세요.")
            },
            OnboardingPageData(R.drawable.description_image_3) {
                Text("설정한 시간의 세션이 종료한 뒤에는")
                Text("간단하게 소감을 기록할 수 있습니다.")
            },
            OnboardingPageData(R.drawable.description_image_4) {
                Text("세션 정보와 알아차림을 터치한 시간은 기록되어")
                Text("리포트로 확인할 수 있어요.")
            },
            OnboardingPageData(R.drawable.description_image_5) {
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
                        color = Color(0xFF8B4513) // 격자무늬와 어울리는 브라운 톤
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageLayout(pageData: OnboardingPageData) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 50%: 이미지
        Box(modifier = Modifier.weight(1f)) {
            Image(
                painter = painterResource(id = pageData.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        // 하단 50%: 커스텀 텍스트 콘텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            pageData.content() // 주입받은 Composable 실행
        }
    }
}

@Composable
fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
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