package com.allai.gpstool.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.allai.gpstool.model.ThemeStyle

data class CustomThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val gridColor: Color,
    val isDark: Boolean
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomThemeColors(
        background = CyberBg,
        surface = CyberSurface,
        primary = CyberPrimary,
        secondary = CyberSecondary,
        textPrimary = CyberText,
        textSecondary = CyberTextDim,
        gridColor = CyberPrimary.copy(alpha = 0.25f),
        isDark = true
    )
}

@Composable
fun GPSToolTheme(
    themeStyle: ThemeStyle = ThemeStyle.CYBERPUNK,
    content: @Composable () -> Unit
) {
    val customColors = when (themeStyle) {
        ThemeStyle.CYBERPUNK -> CustomThemeColors(
            background = CyberBg,
            surface = CyberSurface,
            primary = CyberPrimary,
            secondary = CyberSecondary,
            textPrimary = CyberText,
            textSecondary = CyberTextDim,
            gridColor = CyberPrimary.copy(alpha = 0.3f),
            isDark = true
        )
        ThemeStyle.MATERIAL_YOU -> CustomThemeColors(
            background = MaterialBg,
            surface = MaterialSurface,
            primary = MaterialPrimary,
            secondary = MaterialSecondary,
            textPrimary = MaterialText,
            textSecondary = MaterialTextDim,
            gridColor = MaterialPrimary.copy(alpha = 0.25f),
            isDark = true
        )
        ThemeStyle.AVIATION_AMBER -> CustomThemeColors(
            background = AmberBg,
            surface = AmberSurface,
            primary = AmberPrimary,
            secondary = AmberSecondary,
            textPrimary = AmberText,
            textSecondary = AmberTextDim,
            gridColor = AmberPrimary.copy(alpha = 0.35f),
            isDark = true
        )
        ThemeStyle.OUTDOOR_CONTRAST -> CustomThemeColors(
            background = OutdoorBg,
            surface = OutdoorSurface,
            primary = OutdoorPrimary,
            secondary = OutdoorSecondary,
            textPrimary = OutdoorText,
            textSecondary = OutdoorTextDim,
            gridColor = Color.Black.copy(alpha = 0.2f),
            isDark = false
        )
    }

    val materialColors = if (customColors.isDark) {
        darkColorScheme(
            background = customColors.background,
            surface = customColors.surface,
            primary = customColors.primary,
            secondary = customColors.secondary,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    } else {
        lightColorScheme(
            background = customColors.background,
            surface = customColors.surface,
            primary = customColors.primary,
            secondary = customColors.secondary,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
