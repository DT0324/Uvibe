package com.example.uvibe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00639A),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF4F616E),
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE3EDF6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DCAFF),
    onPrimary = Color(0xFF003353),
    secondary = Color(0xFFB7C9D9),
    background = Color(0xFF111418),
    surface = Color(0xFF1A1F24),
    surfaceVariant = Color(0xFF26323C),
)

@Composable
fun UvibeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
