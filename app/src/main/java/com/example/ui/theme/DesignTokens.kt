package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for spacing, concentric corner radii, elevation,
 * motion springs, and touch target sizing across Glassly.
 */
object DesignTokens {
    // 4/8-pt Spacing Scale
    val SpaceXXS: Dp = 2.dp
    val SpaceXS: Dp = 4.dp
    val SpaceSM: Dp = 8.dp
    val SpaceMD: Dp = 12.dp
    val SpaceLG: Dp = 16.dp
    val SpaceXL: Dp = 20.dp
    val SpaceXXL: Dp = 24.dp
    val Space3XL: Dp = 32.dp
    val Space4XL: Dp = 48.dp

    // Concentric Corner Radius Scale: innerRadius = outerRadius - padding
    val RadiusBadge: Dp = 8.dp
    val RadiusInnerRow: Dp = 14.dp
    val RadiusControl: Dp = 18.dp
    val RadiusCard: Dp = 24.dp
    val RadiusMenu: Dp = 26.dp
    val RadiusSheet: Dp = 34.dp
    val RadiusPill: Dp = 999.dp

    /**
     * Concentric inner radius helper: ensures inner elements nestled inside a container
     * with [outerRadius] and [padding] have an exact optical concentric curvature.
     */
    fun concentricRadius(outerRadius: Dp, padding: Dp): Dp =
        (outerRadius - padding).coerceAtLeast(4.dp)

    val ShapePill = RoundedCornerShape(percent = 50)
    val ShapeCard = RoundedCornerShape(RadiusCard)
    val ShapeMenu = RoundedCornerShape(RadiusMenu)
    val ShapeSheet = RoundedCornerShape(topStart = RadiusSheet, topEnd = RadiusSheet)
    val ShapeInnerRow = RoundedCornerShape(RadiusInnerRow)

    // Component Heights & Touch Targets
    val MinTouchTarget: Dp = 48.dp
    val LargeTouchTarget: Dp = 56.dp
    val TabBarExpandedHeight: Dp = 64.dp
    val TabBarCollapsedHeight: Dp = 48.dp
    val ToolbarHeight: Dp = 56.dp
    val SegmentedControlHeight: Dp = 40.dp

    // Elevation & Layered Shadow Tokens
    val ElevationControl: Dp = 10.dp
    val ElevationMenu: Dp = 22.dp
    val ElevationSheet: Dp = 28.dp

    // Motion Tokens (Interruptible 60/120fps Springs)
    fun <T> pressBounceSpring(reduceMotion: Boolean = false): SpringSpec<T> =
        if (reduceMotion) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = 0.56f, stiffness = 480f)
        }

    fun <T> morphExpandSpring(reduceMotion: Boolean = false): SpringSpec<T> =
        if (reduceMotion) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = 0.72f, stiffness = 380f)
        }

    fun <T> fluidTabSpring(reduceMotion: Boolean = false): SpringSpec<T> =
        if (reduceMotion) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = 0.68f, stiffness = 420f)
        }
}
