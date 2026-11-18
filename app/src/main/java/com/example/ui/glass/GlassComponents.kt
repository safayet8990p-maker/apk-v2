package com.example.ui.glass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.GlassVariant
import com.example.ui.theme.LocalGlassConfig
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class GlassButtonStyle {
    REGULAR,
    TINTED,
    CLEAR,
    ICON_CIRCLE,
    CAPSULE,
    DANGER
}

/**
 * 1. GlassButton: Supports Regular, Tinted/Prominent, Clear, Icon-Circle, Capsule, and Danger styles,
 * plus normal, pressed, focused, disabled, selected, and loading states.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    style: GlassButtonStyle = GlassButtonStyle.REGULAR,
    enabled: Boolean = true,
    selected: Boolean = false,
    loading: Boolean = false,
    contentDescriptionLabel: String? = null,
    testTagName: String? = null
) {
    val config = LocalGlassConfig.current
    val minTouch = if (config.largerTouchTargets) DesignTokens.LargeTouchTarget else DesignTokens.MinTouchTarget
    val variant = when {
        style == GlassButtonStyle.TINTED || selected -> GlassVariant.TINTED
        style == GlassButtonStyle.CLEAR -> GlassVariant.CLEAR
        else -> config.variant
    }
    val tintOverride = when {
        style == GlassButtonStyle.DANGER -> GlassPalette.DangerRed.copy(alpha = 0.68f)
        selected -> config.accentColor.copy(alpha = 0.65f)
        else -> null
    }
    val shape = when (style) {
        GlassButtonStyle.ICON_CIRCLE -> CircleShape
        GlassButtonStyle.CAPSULE -> DesignTokens.ShapePill
        else -> DesignTokens.ShapePill
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        style == GlassButtonStyle.TINTED || style == GlassButtonStyle.DANGER || selected -> Color.White
        style == GlassButtonStyle.CLEAR -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val a11yDesc = contentDescriptionLabel ?: text ?: "Action button"

    Box(
        modifier = modifier
            .then(if (testTagName != null) Modifier.testTag(testTagName) else Modifier)
            .minimumInteractiveComponentSize()
            .heightIn(min = minTouch)
            .widthIn(min = minTouch)
            .alpha(if (enabled) 1f else 0.48f)
            .glassPressBounce(enabled = enabled && !loading)
            .liquidGlass(
                shape = shape,
                cornerRadius = DesignTokens.RadiusPill,
                variantOverride = variant,
                tintOverride = tintOverride
            )
            .clip(shape)
            .semantics {
                this.contentDescription = a11yDesc
                this.role = Role.Button
                this.selected = selected
                if (loading) this.stateDescription = "Loading"
            }
            .clickable(
                enabled = enabled && !loading,
                onClick = onClick
            )
            .padding(
                horizontal = if (style == GlassButtonStyle.ICON_CIRCLE || text == null) 12.dp else 18.dp,
                vertical = 10.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    if (text != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                if (text != null) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 2. GlassContainer (iOS 26 GlassEffectContainer):
 * Renders SDF smooth-union (metaball liquid fusion) between adjacent glass controls so nearby droplets
 * fuse and separate smoothly as their spacing changes (`fusionGapDp`).
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    fusionDistanceDp: Dp = 18.dp,
    separationProgress: Float = 0f, // 0f = fused liquid bridge, 1f = separated droplets
    content: @Composable RowScope.() -> Unit
) {
    val config = LocalGlassConfig.current
    val animSeparation by animateFloatAsState(
        targetValue = separationProgress.coerceIn(0f, 1f),
        animationSpec = DesignTokens.morphExpandSpring(config.reduceMotion),
        label = "liquidFusion"
    )
    val bridgeAlpha = (1f - animSeparation).coerceIn(0f, 1f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (bridgeAlpha > 0.02f && !config.reduceGlass) {
            // Draw SDF smooth-union liquid neck connecting adjacent glass pills
            val bridgeColor = if (config.isDarkTheme) {
                Color.White.copy(alpha = 0.22f * bridgeAlpha * config.intensity)
            } else {
                Color.White.copy(alpha = 0.48f * bridgeAlpha * config.intensity)
            }
            val rimColor = Color.White.copy(alpha = 0.58f * bridgeAlpha * config.specularIntensity)
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val h = size.height
                val w = size.width
                val neckHeight = h * (0.42f + 0.35f * bridgeAlpha)
                val topY = (h - neckHeight) * 0.5f
                val bottomY = topY + neckHeight

                val path = Path().apply {
                    moveTo(w * 0.2f, topY)
                    cubicTo(
                        w * 0.42f, topY + h * 0.14f * animSeparation,
                        w * 0.58f, topY + h * 0.14f * animSeparation,
                        w * 0.8f, topY
                    )
                    lineTo(w * 0.8f, bottomY)
                    cubicTo(
                        w * 0.58f, bottomY - h * 0.14f * animSeparation,
                        w * 0.42f, bottomY - h * 0.14f * animSeparation,
                        w * 0.2f, bottomY
                    )
                    close()
                }
                drawPath(path = path, color = bridgeColor)
                drawPath(path = path, color = rimColor, style = Stroke(width = 1.2.dp.toPx()))
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(fusionDistanceDp * (0.35f + 0.65f * animSeparation)),
            content = content
        )
    }
}

/**
 * 3. GlassSegmentedControl:
 * iOS 26 sliding Liquid Glass capsule segmented control with spring-animated lens thumb.
 */
@Composable
fun GlassSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "glass_segment"
) {
    val config = LocalGlassConfig.current
    val haptic = LocalHapticFeedback.current
    val minHeight = if (config.largerTouchTargets) 48.dp else 42.dp

    BoxWithConstraints(
        modifier = modifier
            .heightIn(min = minHeight)
            .liquidGlass(
                shape = DesignTokens.ShapePill,
                cornerRadius = DesignTokens.RadiusPill
            )
            .padding(4.dp)
    ) {
        val count = options.size.coerceAtLeast(1)
        val segmentWidth = maxWidth / count
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex.coerceIn(0, count - 1),
            animationSpec = DesignTokens.fluidTabSpring(config.reduceMotion),
            label = "segmentIndicatorOffset"
        )

        // Sliding Liquid Glass lens pill thumb
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .height(minHeight - 8.dp)
                .clip(DesignTokens.ShapePill)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (config.isDarkTheme) {
                            listOf(
                                config.accentColor.copy(alpha = 0.62f),
                                config.accentColor.copy(alpha = 0.38f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.92f),
                                Color.White.copy(alpha = 0.72f)
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.75f),
                    shape = DesignTokens.ShapePill
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val textColor = when {
                    isSelected && config.isDarkTheme -> Color.White
                    isSelected && !config.isDarkTheme -> GlassPalette.LightTextPrimary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = minHeight - 8.dp)
                        .clip(DesignTokens.ShapePill)
                        .testTag("${testTagPrefix}_$index")
                        .semantics {
                            this.contentDescription = label
                            this.role = Role.Tab
                            this.selected = isSelected
                        }
                        .clickable {
                            if (config.hapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onOptionSelected(index)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 4. GlassSwitch:
 * iOS 26 Liquid Glass toggle switch where the pill thumb is a refractive glass droplet
 * sliding over a vibrant track (matching WWDC25 page 33/34 green droplet switch).
 */
@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescriptionLabel: String = "Toggle switch",
    testTagName: String? = null
) {
    val config = LocalGlassConfig.current
    val haptic = LocalHapticFeedback.current
    val thumbProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = DesignTokens.fluidTabSpring(config.reduceMotion),
        label = "switchThumbProgress"
    )

    val trackColor = if (checked) {
        GlassPalette.SuccessGreen.copy(alpha = if (enabled) 0.92f else 0.45f)
    } else {
        if (config.isDarkTheme) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .then(if (testTagName != null) Modifier.testTag(testTagName) else Modifier)
            .minimumInteractiveComponentSize()
            .semantics {
                this.contentDescription = contentDescriptionLabel
                this.role = Role.Switch
                this.stateDescription = if (checked) "On" else "Off"
            }
            .glassPressBounce(enabled = enabled, scaleTarget = 1.08f)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (config.hapticsEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(32.dp)
                .clip(DesignTokens.ShapePill)
                .background(trackColor)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (checked) 0.55f else 0.25f),
                    shape = DesignTokens.ShapePill
                )
                .padding(3.dp)
        ) {
            // Refractive Liquid Glass capsule thumb
            Box(
                modifier = Modifier
                    .offset(x = (22.dp * thumbProgress))
                    .width(28.dp)
                    .height(26.dp)
                    .clip(DesignTokens.ShapePill)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.96f),
                                Color.White.copy(alpha = 0.65f),
                                if (checked) GlassPalette.SuccessGreen.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.82f)
                            )
                        )
                    )
                    .border(
                        width = 1.4.dp,
                        color = Color.White,
                        shape = DesignTokens.ShapePill
                    )
            )
        }
    }
}

/**
 * 5. GlassSlider:
 * iOS 26 Liquid Glass slider with a refractive capsule lens thumb that magnifies/refracts the track
 * underneath and expands into a clear glass droplet when dragged (matching WWDC25 page 16/26/33/34).
 */
@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    rainbowTrack: Boolean = false,
    contentDescriptionLabel: String = "Slider",
    testTagName: String? = null
) {
    val config = LocalGlassConfig.current
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }

    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val fraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.18f else 1f,
        animationSpec = DesignTokens.pressBounceSpring(config.reduceMotion),
        label = "sliderThumbScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .then(if (testTagName != null) Modifier.testTag(testTagName) else Modifier)
            .fillMaxWidth()
            .height(48.dp)
            .semantics {
                this.contentDescription = "$contentDescriptionLabel ${(fraction * 100).roundToInt()}%"
            }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    if (config.hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onValueChange(valueRange.start + newFrac * rangeSpan)
                }
            }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                ) { change, _ ->
                    change.consume()
                    val newFrac = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onValueChange(valueRange.start + newFrac * rangeSpan)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val trackWidth = maxWidth
        val thumbWidth = 38.dp
        val usableWidth = (trackWidth - thumbWidth).coerceAtLeast(0.dp)

        // Track background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(DesignTokens.ShapePill)
                .background(
                    if (config.isDarkTheme) Color.White.copy(alpha = 0.16f)
                    else Color.Black.copy(alpha = 0.10f)
                )
        ) {
            if (rainbowTrack) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF007AFF),
                                    Color(0xFF5856D6),
                                    Color(0xFFFF2D55),
                                    Color(0xFFFFCC00),
                                    Color(0xFF34C759)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(DesignTokens.ShapePill)
                        .background(config.accentColor)
                )
            }
        }

        // Liquid Glass Capsule Lens Thumb
        Box(
            modifier = Modifier
                .offset(x = usableWidth * fraction)
                .width(thumbWidth * thumbScale)
                .height(26.dp * thumbScale)
                .clip(DesignTokens.ShapePill)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDragging) 0.55f else 0.92f),
                            Color.White.copy(alpha = if (isDragging) 0.28f else 0.72f)
                        )
                    )
                )
                .border(
                    width = 1.6.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            config.accentColor.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.85f)
                        )
                    ),
                    shape = DesignTokens.ShapePill
                )
        )
    }
}

/**
 * 6. GlassStepper:
 * Fused Liquid Glass minus / value / plus control.
 */
@Composable
fun GlassStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange = 1..10,
    label: String? = null,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "glass_stepper"
) {
    val config = LocalGlassConfig.current
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .heightIn(min = DesignTokens.MinTouchTarget)
            .liquidGlass(shape = DesignTokens.ShapePill, cornerRadius = DesignTokens.RadiusPill)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconButtonPill(
            icon = Icons.Rounded.Remove,
            contentDesc = "Decrease ${label ?: "value"}",
            enabled = value > valueRange.first,
            testTag = "${testTagPrefix}_dec",
            onClick = {
                if (config.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onValueChange((value - 1).coerceIn(valueRange))
            }
        )
        Text(
            text = if (label != null) "$value $label" else value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        IconButtonPill(
            icon = Icons.Rounded.Add,
            contentDesc = "Increase ${label ?: "value"}",
            enabled = value < valueRange.last,
            testTag = "${testTagPrefix}_inc",
            onClick = {
                if (config.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onValueChange((value + 1).coerceIn(valueRange))
            }
        )
    }
}

@Composable
private fun IconButtonPill(
    icon: ImageVector,
    contentDesc: String,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .alpha(if (enabled) 1f else 0.35f)
            .testTag(testTag)
            .semantics {
                this.contentDescription = contentDesc
                this.role = Role.Button
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 7. GlassSearchField:
 * Thumb-reach bottom Liquid Glass search bar with clear button and subtle refraction.
 */
@Composable
fun GlassSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearchSubmit: (() -> Unit)? = null,
    testTagName: String = "glass_search_field"
) {
    val config = LocalGlassConfig.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .liquidGlass(
                shape = DesignTokens.ShapePill,
                cornerRadius = DesignTokens.RadiusPill
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search icon",
            tint = config.accentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(config.accentColor),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit?.invoke() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTagName)
                    .semantics {
                        this.contentDescription = placeholder
                    }
            )
        }
        AnimatedVisibility(
            visible = query.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f))
                    .testTag("${testTagName}_clear")
                    .semantics {
                        this.contentDescription = "Clear search"
                        this.role = Role.Button
                    }
                    .clickable { onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 8. GlassChip:
 * Interactive filter/suggestion pill in Liquid Glass.
 */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    testTagName: String? = null
) {
    val config = LocalGlassConfig.current
    val variant = if (selected) GlassVariant.TINTED else config.variant
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .then(if (testTagName != null) Modifier.testTag(testTagName) else Modifier)
            .minimumInteractiveComponentSize()
            .heightIn(min = 38.dp)
            .glassPressBounce()
            .liquidGlass(
                shape = DesignTokens.ShapePill,
                cornerRadius = DesignTokens.RadiusPill,
                variantOverride = variant
            )
            .clip(DesignTokens.ShapePill)
            .semantics {
                this.contentDescription = label
                this.role = Role.Button
                this.selected = selected
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1
        )
    }
}

/**
 * 9. GlassBadge:
 * Small Clear-Glass optical chip overlaid on photo/video thumbnails (duration, LIVE, RAW, GIF, Heart).
 */
@Composable
fun GlassBadge(
    text: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    contentDescriptionLabel: String? = null
) {
    Row(
        modifier = modifier
            .clip(DesignTokens.ShapePill)
            .background(Color.Black.copy(alpha = 0.44f))
            .border(
                width = 0.8.dp,
                color = Color.White.copy(alpha = 0.55f),
                shape = DesignTokens.ShapePill
            )
            .padding(horizontal = if (text != null) 7.dp else 5.dp, vertical = 3.dp)
            .semantics {
                this.contentDescription = contentDescriptionLabel ?: text ?: "Media badge"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(12.dp)
            )
        }
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                ),
                color = Color.White
            )
        }
    }
}

/**
 * 10. GlassCard & FlatTonalRow:
 * Floating Liquid Glass card container for Settings and grouped panels.
 * Rule enforcement: NEVER glass inside glass! Rows inside a GlassCard use flat tonal fills.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    cornerRadius: Dp = DesignTokens.RadiusCard,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(cornerRadius),
                    cornerRadius = cornerRadius
                )
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

/**
 * Flat tonal row for use INSIDE a GlassCard or GlassBottomSheet (strictly respecting
 * the "Never glass inside glass" rule and concentric corner radius = outerRadius - padding).
 */
@Composable
fun FlatGlassRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = LocalGlassConfig.current.accentColor,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val config = LocalGlassConfig.current
    val rowShape = RoundedCornerShape(
        DesignTokens.concentricRadius(DesignTokens.RadiusCard, 10.dp)
    )
    val flatFill = if (config.isDarkTheme) GlassPalette.DarkFlatRowFill else GlassPalette.LightFlatRowFill

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (config.largerTouchTargets) 58.dp else 52.dp)
            .clip(rowShape)
            .background(flatFill)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(leadingIconTint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = leadingIconTint,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}
