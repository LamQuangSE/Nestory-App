package com.example.nestory.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.nestory.R

@OptIn(ExperimentalTextApi::class)
private fun interFont(weight: FontWeight) = Font(
    resId = R.font.inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val InterFontFamily = FontFamily(
    interFont(FontWeight.W400),
    interFont(FontWeight.W500),
    interFont(FontWeight.W600),
    interFont(FontWeight.W700),
    interFont(FontWeight.W800),
    interFont(FontWeight.W900)
)

val GeneratedTypography = Typography(
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        lineHeight = 14.52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 35.sp,
        lineHeight = 42.36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 30.sp,
        lineHeight = 36.31.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 25.sp,
        lineHeight = 30.26.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 10.sp,
        lineHeight = 12.1.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 22.sp,
        lineHeight = 26.62.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        lineHeight = 21.78.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 13.sp,
        lineHeight = 15.73.sp,
        letterSpacing = 0.sp
    )
)
