package com.dhlee.mindpath.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dhlee.mindpath.R

val NanumHandwriting = FontFamily(
    Font(R.font.nanum_himnaera, FontWeight.Normal)
)


// 앱 기본 글꼴
val AppFont = FontFamily(
    Font(R.font.notosanskr_normal, FontWeight.Normal),
    Font(R.font.notosanskr_medium, FontWeight.Medium),
    Font(R.font.notosanskr_bold, FontWeight.Bold),
    Font(R.font.notosanskr_semibold, FontWeight.Bold),
)

private fun TextStyle.app() = copy(fontFamily = AppFont)

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.app(),
    displayMedium = base.displayMedium.app(),
    displaySmall = base.displaySmall.app(),

    headlineLarge = base.headlineLarge.app(),
    headlineMedium = base.headlineMedium.app(),
    headlineSmall = base.headlineSmall.app(),

    titleLarge = base.titleLarge.app(),
    titleMedium = base.titleMedium.app(),
    titleSmall = base.titleSmall.app(),

    // 기존에 직접 정의했던 값 유지 (글꼴만 AppFont로 변경)
    bodyLarge = TextStyle(
        fontFamily = AppFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = base.bodyMedium.app(),
    bodySmall = base.bodySmall.app(),

    labelLarge = base.labelLarge.app(),
    labelMedium = base.labelMedium.app(),
    labelSmall = base.labelSmall.app(),
)