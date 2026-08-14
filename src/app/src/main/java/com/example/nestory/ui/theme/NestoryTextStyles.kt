package com.example.nestory.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

object NestoryTextStyles {
    private fun interStyle(weight: FontWeight, size: TextUnit, lineHeight: TextUnit) = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = weight,
        fontSize = size,
        lineHeight = lineHeight,
        letterSpacing = 0.sp
    )

    val Display35 = interStyle(FontWeight.W700, 35.sp, 42.36.sp)
    val Heading30 = interStyle(FontWeight.W700, 30.sp, 36.31.sp)
    val Heading25Bold = interStyle(FontWeight.W700, 25.sp, 30.26.sp)
    val Heading25Semi = Heading25Bold.copy(fontWeight = FontWeight.W600)
    val Title24Semi = interStyle(FontWeight.W600, 24.sp, 29.05.sp)
    val Title22Bold = interStyle(FontWeight.W700, 22.sp, 26.62.sp)
    val Title22Semi = Title22Bold.copy(fontWeight = FontWeight.W600)
    val Title21Bold = interStyle(FontWeight.W700, 21.sp, 25.41.sp)
    val Title21Semi = Title21Bold.copy(fontWeight = FontWeight.W600)
    
    // Bổ sung Title 20px chuẩn thiết kế Figma
    val Title20Bold = interStyle(FontWeight.W700, 20.sp, 24.2.sp)
    val Title20Semi = Title20Bold.copy(fontWeight = FontWeight.W600)
    
    val Body20Medium = interStyle(FontWeight.W500, 20.sp, 24.2.sp)
    val Body20Semi = Body20Medium.copy(fontWeight = FontWeight.W600)
    val Body20Bold = Body20Medium.copy(fontWeight = FontWeight.W700)
    val Body18Semi = interStyle(FontWeight.W600, 18.sp, 21.78.sp)
    val Body17Bold = interStyle(FontWeight.W700, 17.sp, 20.57.sp)
    val Body17Medium = Body17Bold.copy(fontWeight = FontWeight.W600)
    val Body16Bold = interStyle(FontWeight.W700, 16.sp, 19.36.sp)
    val Body16Semi = interStyle(FontWeight.W600, 16.sp, 19.36.sp)
    val Body16Medium = Body16Semi.copy(fontWeight = FontWeight.W500)
    val Body15Semi = interStyle(FontWeight.W600, 15.sp, 18.15.sp)
    val Body15Medium = Body15Semi.copy(fontWeight = FontWeight.W500)
    val Body14Medium = interStyle(FontWeight.W500, 14.sp, 16.94.sp)
    val Body14Semi = interStyle(FontWeight.W600, 14.sp, 16.94.sp)
    val Body13Semi = interStyle(FontWeight.W600, 13.sp, 15.73.sp)
    val Body13Medium = Body13Semi.copy(fontWeight = FontWeight.W500)
    val Body13Bold = Body13Semi.copy(fontWeight = FontWeight.W700)
    val Body12Bold = interStyle(FontWeight.W700, 12.sp, 14.52.sp)
    val Body12Semi = interStyle(FontWeight.W600, 12.sp, 14.52.sp)
    val Body12Medium = interStyle(FontWeight.W500, 12.sp, 14.52.sp)
    val Body11Semi = interStyle(FontWeight.W600, 11.sp, 13.31.sp)
    val Body10Semi = interStyle(FontWeight.W600, 10.sp, 12.1.sp)
    val Body8Medium = interStyle(FontWeight.W500, 8.sp, 9.68.sp)
}