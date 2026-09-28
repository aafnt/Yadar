package com.yadar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.yadar.app.domain.model.AppThemeMode

@Composable
fun YadarTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    accentColor: Color = DefaultAccent,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = accentColor,
            background = YadarBackgroundDark,
            surface = YadarSurfaceDark,
            onBackground = YadarOnSurfaceDark,
            onSurface = YadarOnSurfaceDark,
            outline = YadarOutline
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            background = YadarBackgroundLight,
            surface = YadarSurfaceLight,
            onBackground = YadarOnSurfaceLight,
            onSurface = YadarOnSurfaceLight,
            outline = YadarOutline
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YadarTypography,
        content = content
    )
}
