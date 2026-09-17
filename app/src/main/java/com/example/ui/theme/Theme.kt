package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EzigoNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = EzigoGold,
    onSecondary = Color.White,
    secondaryContainer = EzigoGoldContainer,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = EzigoInfo,
    onTertiary = Color.White,
    background = EzigoSurface,
    onBackground = EzigoTextPrimary,
    surface = EzigoSurfaceCard,
    onSurface = EzigoTextPrimary,
    surfaceVariant = EzigoSurfaceVariant,
    onSurfaceVariant = EzigoTextSecondary,
    outline = EzigoBorder,
    error = EzigoDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = EzigoNavyLight,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = EzigoGoldLight,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = EzigoGoldContainer,
    tertiary = Color(0xFF7DD3FC),
    onTertiary = Color(0xFF0C4A6E),
    background = Color(0xFF0B1320),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF131E30),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF450A0A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
