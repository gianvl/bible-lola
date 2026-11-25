package com.lolabible.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Warm, high-contrast "paper and ink" palette that is easy on older eyes.
private val LightColors = lightColorScheme(
    primary = Color(0xFF7A2E1F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6DDD3),
    onPrimaryContainer = Color(0xFF3A0B03),
    secondaryContainer = Color(0xFFEFE3D3),
    onSecondaryContainer = Color(0xFF2A1E12),
    background = Color(0xFFFFFBF5),
    onBackground = Color(0xFF1C1712),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF1C1712),
    surfaceContainer = Color(0xFFF7EFE5),
    onSurfaceVariant = Color(0xFF4E4339),
    outlineVariant = Color(0xFFD8C8B8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4A1),
    onPrimary = Color(0xFF561E12),
    primaryContainer = Color(0xFF5E2316),
    onPrimaryContainer = Color(0xFFFFDBD1),
    secondaryContainer = Color(0xFF4A3F33),
    onSecondaryContainer = Color(0xFFF2E2D0),
    background = Color(0xFF17130F),
    onBackground = Color(0xFFEDE0D4),
    surface = Color(0xFF17130F),
    onSurface = Color(0xFFEDE0D4),
    surfaceContainer = Color(0xFF231E19),
    onSurfaceVariant = Color(0xFFD5C3B5),
    outlineVariant = Color(0xFF52443A),
)

// Everything is a step larger than Material defaults; the system font scale still applies on top.
private val AppTypography = Typography().run {
    copy(
        titleLarge = titleLarge.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontSize = 22.sp, lineHeight = 28.sp),
        bodyLarge = bodyLarge.copy(fontSize = 20.sp, lineHeight = 28.sp),
        labelLarge = labelLarge.copy(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun BibleTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
