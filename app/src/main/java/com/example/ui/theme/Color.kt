package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Core iOS 26 Liquid Glass Palette & Semantic Tokens
object GlassPalette {
    // Accent Swatches (User-selectable in Settings)
    val OceanBlue = Color(0xFF007AFF)
    val ElectricCyan = Color(0xFF00C7BE)
    val IrisPurple = Color(0xFF5856D6)
    val CoralPink = Color(0xFFFF2D55)
    val AmberGold = Color(0xFFFF9500)
    val EmeraldGreen = Color(0xFF34C759)

    val AccentSwatches = listOf(
        OceanBlue,
        ElectricCyan,
        IrisPurple,
        CoralPink,
        AmberGold,
        EmeraldGreen
    )

    // Light Theme Surfaces
    val LightBackground = Color(0xFFF4F6FB)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceTonal = Color(0xCCFFFFFF)
    val LightFlatRowFill = Color(0x140F172A)
    val LightFlatRowDivider = Color(0x14000000)
    val LightTextPrimary = Color(0xFF0B1324)
    val LightTextSecondary = Color(0xFF475569)
    val LightTextMuted = Color(0xFF64748B)

    // Dark Theme Surfaces
    val DarkBackground = Color(0xFF060911)
    val DarkSurface = Color(0xFF111827)
    val DarkSurfaceTonal = Color(0xCC161F30)
    val DarkFlatRowFill = Color(0x1FFFFFFF)
    val DarkFlatRowDivider = Color(0x1AFFFFFF)
    val DarkTextPrimary = Color(0xFFF8FAFC)
    val DarkTextSecondary = Color(0xFFCBD5E1)
    val DarkTextMuted = Color(0xFF94A3B8)

    // Liquid Glass Optical Tints
    val GlassRegularLight = Color(0x88FFFFFF)
    val GlassRegularDark = Color(0x6E182234)
    val GlassClearLight = Color(0x42FFFFFF)
    val GlassClearDark = Color(0x3D0B111E)
    val GlassClearScrim = Color(0x52000000)

    // Rim & Specular Highlights
    val SpecularHighlightLight = Color(0xEBFFFFFF)
    val SpecularHighlightDark = Color(0x88FFFFFF)
    val InnerGlowLight = Color(0x66FFFFFF)
    val InnerGlowDark = Color(0x33FFFFFF)

    // Status Colors
    val DangerRed = Color(0xFFFF3B30)
    val SuccessGreen = Color(0xFF34C759)
    val WarningOrange = Color(0xFFFF9500)
}
