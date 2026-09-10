package com.dhlee.mindpath.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dhlee.mindpath.R

val NanumHandwriting = FontFamily(
    Font(R.font.nanum_himnaera, FontWeight.Normal)
)

// Material3 기본 Typography (스타일별 fontSize/lineHeight/letterSpacing은 그대로 유지)
private val DefaultTypography = Typography()

// 위 기본값에서 fontFamily만 전부 NanumHandwriting으로 교체.
// -> MaterialTheme.typography.xxx를 참조하는 곳, 그리고 style 없이 쓰인 Text()들
//    (LocalTextStyle이 기본적으로 bodyLarge를 상속받음)까지 전부 나눔글씨가 적용됨.
//    각 스타일의 fontSize/lineHeight/letterSpacing은 손대지 않음.
val Typography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = NanumHandwriting),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = NanumHandwriting),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = NanumHandwriting),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = NanumHandwriting),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = NanumHandwriting),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = NanumHandwriting),
    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = NanumHandwriting),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = NanumHandwriting),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = NanumHandwriting),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = NanumHandwriting),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = NanumHandwriting),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = NanumHandwriting),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = NanumHandwriting),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = NanumHandwriting),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = NanumHandwriting),
)