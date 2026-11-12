package com.qracesso.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = BackgroundLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = BackgroundLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = ErrorColor
)

private val DarkColors = darkColorScheme(
    primary = TextPrimaryDark,
    onPrimary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = BackgroundDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = ErrorColor
)

@Composable
fun QRAcessoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = QRAcessoTypography,
        content = content
    )
}
