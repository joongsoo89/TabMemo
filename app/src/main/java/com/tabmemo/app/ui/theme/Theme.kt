package com.tabmemo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Paper = Color(0xFFF3EAD9)
val Cream = Color(0xFFFFFDF8)
val Ink = Color(0xFF1C241E)
val Muted = Color(0xFF6D7A74)
val Line = Color(0xFFDCE6DF)
val Teal = Color(0xFF2C5F6E)
val TealSoft = Color(0xFFD9EBE8)
val Warm = Color(0xFFC45C26)

private val Colors = lightColorScheme(
    primary = Teal,
    onPrimary = Cream,
    secondary = Warm,
    onSecondary = Cream,
    background = Paper,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE4ECE7),
    onSurfaceVariant = Muted,
    outline = Line,
    error = Warm,
)

private val Type = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        color = Ink,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = Ink,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = Ink,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        color = Ink,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = Ink,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = Ink,
    ),
)

@Composable
fun TabMemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        typography = Type,
        content = content,
    )
}
