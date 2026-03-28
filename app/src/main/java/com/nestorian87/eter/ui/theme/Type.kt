package com.nestorian87.eter.ui.theme

import com.nestorian87.eter.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LoraFontFamily = FontFamily(
    Font(R.font.lora_variable, weight = FontWeight.Normal),
    Font(R.font.lora_variable, weight = FontWeight.Medium),
    Font(R.font.lora_variable, weight = FontWeight.SemiBold),
    Font(R.font.lora_variable, weight = FontWeight.Bold),
)

private val RobotoMonoFontFamily = FontFamily(
    Font(R.font.roboto_mono_variable, weight = FontWeight.Normal),
    Font(R.font.roboto_mono_variable, weight = FontWeight.Medium),
    Font(R.font.roboto_mono_variable, weight = FontWeight.SemiBold),
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = LoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = LoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = LoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = LoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = LoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoMonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = RobotoMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)
