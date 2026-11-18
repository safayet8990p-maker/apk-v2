package com.example.ui.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.GlassVariant
import com.example.ui.theme.LocalGlassConfig

/**
 * 11. GlassToolbar:
 * Floating iOS 26 top bar that supports large-title collapse into a compact floating Liquid Glass header
 * with optional back button, subtitle, and trailing action pills.
 */
@Composable
fun GlassToolbar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    collapseProgress: Float = 0f, // 0f = expanded large header, 1f = compact floating glass bar
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val config = LocalGlassConfig.current
    val clampedCollapse = collapseProgress.coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (onBackClick != null) {
                    GlassButton(
                        onClick = onBackClick,
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        style = GlassButtonStyle.ICON_CIRCLE,
                        contentDescriptionLabel = "Navigate back",
                        testTagName = "toolbar_back_button"
                    )
                }

                // Compact floating glass pill title when scrolled or when back button is present
                Box(
                    modifier = Modifier
                        .liquidGlass(
                            shape = DesignTokens.ShapePill,
                            cornerRadius = DesignTokens.RadiusPill
                        )
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Column {
                        Text(
                            text = title,
                            style = if (clampedCollapse > 0.5f || onBackClick != null) {
                                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.headlineLarge
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle != null && clampedCollapse < 0.6f) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions
            )
        }
    }
}

enum class GlassTabDestination(
    val route: String,
    val labelEn: String,
    val labelBn: String,
    val icon: ImageVector
) {
    LIBRARY("library", "Library", "লাইব্রেরি", Icons.Rounded.PhotoLibrary),
    COLLECTIONS("collections", "Collections", "সংগ্রহ", Icons.Rounded.Collections),
    SETTINGS("settings", "Settings", "সেটিংস", Icons.Rounded.Settings),
    SEARCH("search", "Search", "খুঁজুন", Icons.Rounded.Search)
}

/**
 * 12. GlassTabBar:
 * Faithful iOS 26 floating Liquid Glass tab bar:
 * - Main capsule holds Library, Collections, Settings with a sliding refractive pill indicator
 * - Detached circular Search orb on the right inside a morphing GlassContainer
 * - Shrinks smoothly while scrolling down (`isMinimized = true`) and expands immediately on scroll up or tap!
 */
@Composable
fun GlassTabBar(
    currentTab: GlassTabDestination,
    onTabSelected: (GlassTabDestination) -> Unit,
    isMinimized: Boolean,
    onExpandRequested: () -> Unit,
    languageIsBangla: Boolean = false,
    modifier: Modifier = Modifier
) {
    val config = LocalGlassConfig.current
    val haptic = LocalHapticFeedback.current
    val mainTabs = remember {
        listOf(
            GlassTabDestination.LIBRARY,
            GlassTabDestination.COLLECTIONS,
            GlassTabDestination.SETTINGS
        )
    }

    val barScale by animateFloatAsState(
        targetValue = if (isMinimized) 0.88f else 1f,
        animationSpec = DesignTokens.fluidTabSpring(config.reduceMotion),
        label = "tabBarScale"
    )
    val barHeight by animateDpAsState(
        targetValue = if (isMinimized) DesignTokens.TabBarCollapsedHeight else DesignTokens.TabBarExpandedHeight,
        animationSpec = DesignTokens.fluidTabSpring(config.reduceMotion),
        label = "tabBarHeight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        GlassContainer(
            fusionDistanceDp = 14.dp,
            separationProgress = if (isMinimized) 0.25f else 0.85f,
            modifier = Modifier.graphicsLayer {
                scaleX = barScale
                scaleY = barScale
            }
        ) {
            // Left capsule: Library / Collections / Settings
            Row(
                modifier = Modifier
                    .height(barHeight)
                    .liquidGlass(
                        shape = DesignTokens.ShapePill,
                        cornerRadius = DesignTokens.RadiusPill,
                        elevationOverride = DesignTokens.ElevationMenu
                    )
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                mainTabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    val label = if (languageIsBangla) tab.labelBn else tab.labelEn
                    val activePillBrush = if (isSelected) {
                        Brush.verticalGradient(
                            colors = listOf(
                                config.accentColor.copy(alpha = if (config.isDarkTheme) 0.42f else 0.22f),
                                config.accentColor.copy(alpha = if (config.isDarkTheme) 0.24f else 0.12f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    }

                    Row(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .height(barHeight - 10.dp)
                            .clip(DesignTokens.ShapePill)
                            .background(activePillBrush)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.52f),
                                        shape = DesignTokens.ShapePill
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .glassPressBounce()
                            .testTag("tab_${tab.route}")
                            .semantics {
                                this.contentDescription = label
                                this.role = Role.Tab
                                this.selected = isSelected
                            }
                            .clickable {
                                if (config.hapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                if (isMinimized) {
                                    onExpandRequested()
                                }
                                onTabSelected(tab)
                            }
                            .padding(
                                horizontal = if (isMinimized) 14.dp else 18.dp,
                                vertical = 6.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) config.accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                modifier = Modifier.size(if (isMinimized) 20.dp else 22.dp)
                            )
                            AnimatedVisibility(visible = !isMinimized) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) config.accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Detached circular Search orb on the right (matching iOS 26 Photos screenshot page 42/44!)
            val searchTab = GlassTabDestination.SEARCH
            val isSearchSelected = currentTab == searchTab
            val searchLabel = if (languageIsBangla) searchTab.labelBn else searchTab.labelEn

            Box(
                modifier = Modifier
                    .size(barHeight)
                    .glassPressBounce()
                    .liquidGlass(
                        shape = CircleShape,
                        cornerRadius = DesignTokens.RadiusPill,
                        variantOverride = if (isSearchSelected) GlassVariant.TINTED else config.variant,
                        elevationOverride = DesignTokens.ElevationMenu
                    )
                    .clip(CircleShape)
                    .testTag("tab_search")
                    .semantics {
                        this.contentDescription = searchLabel
                        this.role = Role.Tab
                        this.selected = isSearchSelected
                    }
                    .clickable {
                        if (config.hapticsEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        if (isMinimized) {
                            onExpandRequested()
                        }
                        onTabSelected(searchTab)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = if (isSearchSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(if (isMinimized) 20.dp else 24.dp)
                )
            }
        }
    }
}

data class GlassMenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val isSelected: Boolean = false,
    val onClick: () -> Unit
)

/**
 * 13. GlassMenu / ContextMenu:
 * Expands directly FROM the button that triggered it (`transformOrigin`) with increased
 * refraction and shadow thickness (`expansionProgress = 1f`), and collapses back into it!
 * Rows inside use flat tonal fills (never glass inside glass).
 */
@Composable
fun GlassMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<GlassMenuItem>,
    modifier: Modifier = Modifier,
    transformOrigin: TransformOrigin = TransformOrigin(1f, 0f),
    headerTitle: String? = null
) {
    val config = LocalGlassConfig.current

    AnimatedVisibility(
        visible = expanded,
        enter = scaleIn(
            animationSpec = DesignTokens.morphExpandSpring(config.reduceMotion),
            initialScale = 0.25f,
            transformOrigin = transformOrigin
        ) + fadeIn(animationSpec = tween(160)),
        exit = scaleOut(
            animationSpec = DesignTokens.morphExpandSpring(config.reduceMotion),
            targetScale = 0.20f,
            transformOrigin = transformOrigin
        ) + fadeOut(animationSpec = tween(130)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 220.dp, max = 280.dp)
                .liquidGlass(
                    shape = DesignTokens.ShapeMenu,
                    cornerRadius = DesignTokens.RadiusMenu,
                    expansionProgress = 1f, // Thicker shadow & lens refraction when expanded!
                    elevationOverride = DesignTokens.ElevationMenu
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (headerTitle != null) {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            items.forEach { item ->
                val itemColor = if (item.isDestructive) {
                    GlassPalette.DangerRed
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                val flatFill = if (item.isSelected) {
                    config.accentColor.copy(alpha = 0.18f)
                } else {
                    if (config.isDarkTheme) GlassPalette.DarkFlatRowFill else GlassPalette.LightFlatRowFill
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(DesignTokens.concentricRadius(DesignTokens.RadiusMenu, 8.dp)))
                        .background(flatFill)
                        .testTag("glass_menu_item_${item.id}")
                        .semantics {
                            this.contentDescription = item.title
                            this.role = Role.Button
                        }
                        .clickable {
                            item.onClick()
                            onDismissRequest()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = itemColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = itemColor
                        )
                    }
                    if (item.isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = config.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 14. GlassAlert / Dialog:
 * In-place Liquid Glass confirmation alert that expands FROM the trigger origin (`transformOrigin`)
 * and collapses back into it.
 */
@Composable
fun GlassAlert(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    transformOrigin: TransformOrigin = TransformOrigin(0.5f, 0.85f),
    extraContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val config = LocalGlassConfig.current
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.36f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    animationSpec = DesignTokens.morphExpandSpring(config.reduceMotion),
                    initialScale = 0.35f,
                    transformOrigin = transformOrigin
                ) + fadeIn(),
                exit = scaleOut(
                    animationSpec = DesignTokens.morphExpandSpring(config.reduceMotion),
                    targetScale = 0.25f,
                    transformOrigin = transformOrigin
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .liquidGlass(
                            shape = RoundedCornerShape(DesignTokens.RadiusSheet),
                            cornerRadius = DesignTokens.RadiusSheet,
                            expansionProgress = 1f,
                            elevationOverride = DesignTokens.ElevationSheet
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (extraContent != null) {
                        extraContent()
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val buttonShape = RoundedCornerShape(
                            DesignTokens.concentricRadius(DesignTokens.RadiusSheet, 14.dp)
                        )
                        val flatFill = if (config.isDarkTheme) GlassPalette.DarkFlatRowFill else GlassPalette.LightFlatRowFill

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(buttonShape)
                                .background(flatFill)
                                .testTag("glass_alert_cancel")
                                .clickable(onClick = onDismiss)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dismissText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(buttonShape)
                                .background(
                                    if (isDestructive) GlassPalette.DangerRed.copy(alpha = 0.22f)
                                    else config.accentColor.copy(alpha = 0.24f)
                                )
                                .testTag("glass_alert_confirm")
                                .clickable {
                                    onConfirm()
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDestructive) GlassPalette.DangerRed else config.accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 15. GlassBottomSheet:
 * Floating Liquid Glass sheet with drag handle and flat tonal inner rows (never glass inside glass).
 */
@Composable
fun GlassBottomSheet(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.38f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .liquidGlass(
                        shape = RoundedCornerShape(DesignTokens.RadiusSheet),
                        cornerRadius = DesignTokens.RadiusSheet,
                        expansionProgress = 1f,
                        elevationOverride = DesignTokens.ElevationSheet
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sheet grabber pill
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(5.dp)
                        .clip(DesignTokens.ShapePill)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            .testTag("sheet_close_button")
                            .semantics {
                                this.contentDescription = "Close sheet"
                                this.role = Role.Button
                            }
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                content()
            }
        }
    }
}

data class GlassActionItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * 16. GlassActionBar:
 * Floating multi-select or viewer bottom action bar in Liquid Glass (matching reference page 48!).
 */
@Composable
fun GlassActionBar(
    actions: List<GlassActionItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .heightIn(min = 64.dp)
            .liquidGlass(
                shape = DesignTokens.ShapePill,
                cornerRadius = DesignTokens.RadiusPill,
                expansionProgress = 0.6f,
                elevationOverride = DesignTokens.ElevationMenu
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        actions.forEach { action ->
            val tint = if (action.isDestructive) GlassPalette.DangerRed else MaterialTheme.colorScheme.onSurface
            Column(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(DesignTokens.ShapePill)
                    .glassPressBounce()
                    .testTag("action_bar_${action.id}")
                    .semantics {
                        this.contentDescription = action.label
                        this.role = Role.Button
                    }
                    .clickable(onClick = action.onClick)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 17. GlassToast / Snackbar:
 * Floating capsule notification in Liquid Glass.
 */
@Composable
fun GlassToast(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + expandIn(expandFrom = Alignment.TopCenter),
        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopCenter),
        modifier = modifier
    ) {
        if (message != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .liquidGlass(
                        shape = DesignTokens.ShapePill,
                        cornerRadius = DesignTokens.RadiusPill,
                        elevationOverride = DesignTokens.ElevationMenu
                    )
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = LocalGlassConfig.current.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 18. GlassProgress / Loader & Skeleton Shimmer:
 */
@Composable
fun GlassLoader(
    label: String? = null,
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    val config = LocalGlassConfig.current
    Column(
        modifier = modifier
            .liquidGlass(
                shape = DesignTokens.ShapeCard,
                cornerRadius = DesignTokens.RadiusCard
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (progress != null) {
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = config.accentColor,
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(36.dp)
            )
        } else {
            CircularProgressIndicator(
                color = config.accentColor,
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(36.dp)
            )
        }
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GlassSkeletonTile(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val config = LocalGlassConfig.current
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val baseColor = if (config.isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val highlightColor = if (config.isDarkTheme) Color(0xFF334155) else Color(0xFFF8FAFC)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    start = androidx.compose.ui.geometry.Offset(shimmerX * 300f, 0f),
                    end = androidx.compose.ui.geometry.Offset((shimmerX + 0.8f) * 300f, 300f)
                )
            )
    )
}

/**
 * 19. GlassScrubber:
 * Used for both the Library fast-scroll date bubble AND the Photo/Video viewer filmstrip scrubber.
 */
@Composable
fun GlassScrubber(
    currentLabel: String,
    fraction: Float,
    onFractionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    testTagName: String = "glass_scrubber"
) {
    val config = LocalGlassConfig.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = DesignTokens.ShapePill, cornerRadius = DesignTokens.RadiusPill)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = currentLabel,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = config.accentColor,
            modifier = Modifier.widthIn(min = 56.dp)
        )
        GlassSlider(
            value = fraction.coerceIn(0f, 1f),
            onValueChange = onFractionChange,
            modifier = Modifier.weight(1f),
            contentDescriptionLabel = "Scrubber $currentLabel",
            testTagName = testTagName
        )
    }
}

/**
 * 20. GlassPicker:
 * Horizontal scrollable or multi-option Liquid Glass selector for sort order, date filter, or playback speed.
 */
@Composable
fun <T> GlassPicker(
    options: List<T>,
    selectedOption: T,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .liquidGlass(shape = DesignTokens.ShapePill, cornerRadius = DesignTokens.RadiusPill)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { item ->
            val isSelected = item == selectedOption
            val label = optionLabel(item)
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(DesignTokens.ShapePill)
                    .background(
                        if (isSelected) LocalGlassConfig.current.accentColor
                        else Color.Transparent
                    )
                    .clickable { onOptionSelected(item) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
