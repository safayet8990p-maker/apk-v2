package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class GlassVariant {
    REGULAR, CLEAR, TINTED
}

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা")
}

/**
 * Central GlassConfig data class holding every tunable for Apple iOS 26 Liquid Glass:
 * refraction strength, rim width, chromatic dispersion, blur radius, tint,
 * spring stiffness, durations, adaptive luminance, and accessibility fallbacks.
 */
data class GlassConfig(
    val variant: GlassVariant = GlassVariant.REGULAR,
    val intensity: Float = 0.85f,            // 0f..1f master optical intensity
    val refractionStrength: Float = 26f,     // Edge lens displacement in px
    val rimWidthDp: Float = 14f,             // Bevel rim width for SDF lensing
    val chromaticDispersion: Float = 0.38f,  // RGB channel split along rim
    val blurRadiusDp: Float = 18f,           // Background blur under glass
    val specularIntensity: Float = 0.78f,    // Top-left bevel highlight strength
    val noiseGrainAlpha: Float = 0.032f,     // Fine optical grain
    val innerGlowAlpha: Float = 0.28f,       // Soft inner edge luminance
    val shadowElevationDp: Float = 14f,      // Soft layered drop shadow
    val accentColor: Color = GlassPalette.AccentSwatches[0],
    val reduceGlass: Boolean = false,
    val highContrastGlass: Boolean = false,
    val reduceMotion: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val hapticStrength: Float = 0.8f,
    val largerTouchTargets: Boolean = false,
    val isDarkTheme: Boolean = false,
    val lightDirectionX: Float = -0.65f,     // Smoothed device tilt + touch light X
    val lightDirectionY: Float = -0.75f,     // Smoothed device tilt + touch light Y
    val backdropLuminance: Float = 0.5f      // Sampled backdrop luminance (0=dark, 1=bright)
)

val LocalGlassConfig = staticCompositionLocalOf { GlassConfig() }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

@Composable
fun GlasslyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    glassConfig: GlassConfig = GlassConfig(),
    language: AppLanguage = AppLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val accent = glassConfig.accentColor
    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = accent.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = accent,
            onSecondary = Color.White,
            background = GlassPalette.DarkBackground,
            onBackground = GlassPalette.DarkTextPrimary,
            surface = GlassPalette.DarkSurface,
            onSurface = GlassPalette.DarkTextPrimary,
            surfaceVariant = GlassPalette.DarkSurfaceTonal,
            onSurfaceVariant = GlassPalette.DarkTextSecondary,
            error = GlassPalette.DangerRed,
            outline = GlassPalette.DarkFlatRowDivider
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = accent.copy(alpha = 0.16f),
            onPrimaryContainer = GlassPalette.LightTextPrimary,
            secondary = accent,
            onSecondary = Color.White,
            background = GlassPalette.LightBackground,
            onBackground = GlassPalette.LightTextPrimary,
            surface = GlassPalette.LightSurface,
            onSurface = GlassPalette.LightTextPrimary,
            surfaceVariant = GlassPalette.LightSurfaceTonal,
            onSurfaceVariant = GlassPalette.LightTextSecondary,
            error = GlassPalette.DangerRed,
            outline = GlassPalette.LightFlatRowDivider
        )
    }

    val effectiveGlassConfig = glassConfig.copy(isDarkTheme = useDark)

    CompositionLocalProvider(
        LocalGlassConfig provides effectiveGlassConfig,
        LocalAppLanguage provides language
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GlasslyTypography,
            content = content
        )
    }
}
