package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ScrollElectricBlue,
    onPrimary = ScrollWhite,
    primaryContainer = ScrollDeepBlue,
    onPrimaryContainer = ScrollLightBlueContainer,
    secondary = ScrollCyan,
    onSecondary = ScrollWhite,
    background = Color(0xFF0A0D22),
    surface = Color(0xFF111633),
    onBackground = Color(0xFFEFF1F9),
    onSurface = Color(0xFFEFF1F9),
    surfaceVariant = Color(0xFF1B2144),
    onSurfaceVariant = Color(0xFFB0B7D6),
    outline = Color(0xFF323B68)
)

private val LightColorScheme = lightColorScheme(
    primary = ScrollRoyalBlue,
    onPrimary = ScrollWhite,
    primaryContainer = ScrollLightBlueContainer,
    onPrimaryContainer = ScrollOnBlueContainer,
    secondary = ScrollElectricBlue,
    onSecondary = ScrollWhite,
    background = ScrollBackground,
    surface = ScrollSurface,
    onBackground = ScrollTextPrimary,
    onSurface = ScrollTextPrimary,
    surfaceVariant = ScrollSurfaceVariant,
    onSurfaceVariant = ScrollTextSecondary,
    outline = ScrollBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Chic (3).png Royal Blue theme brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
