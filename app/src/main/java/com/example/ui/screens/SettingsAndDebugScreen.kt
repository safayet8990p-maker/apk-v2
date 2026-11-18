package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MotionPhotosOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.GlasslySettingsState
import com.example.data.SortOrder
import com.example.ui.GlasslyViewModel
import com.example.ui.SubScreenRoute
import com.example.ui.glass.FlatGlassRow
import com.example.ui.glass.GlassActionBar
import com.example.ui.glass.GlassActionItem
import com.example.ui.glass.GlassAlert
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.GlassBottomSheet
import com.example.ui.glass.GlassButton
import com.example.ui.glass.GlassButtonStyle
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassChip
import com.example.ui.glass.GlassContainer
import com.example.ui.glass.GlassLoader
import com.example.ui.glass.GlassMenu
import com.example.ui.glass.GlassMenuItem
import com.example.ui.glass.GlassPicker
import com.example.ui.glass.GlassScrubber
import com.example.ui.glass.GlassSearchField
import com.example.ui.glass.GlassSegmentedControl
import com.example.ui.glass.GlassSkeletonTile
import com.example.ui.glass.GlassSlider
import com.example.ui.glass.GlassStepper
import com.example.ui.glass.GlassSwitch
import com.example.ui.glass.GlassToolbar
import com.example.ui.glass.GlassBackdropState
import com.example.ui.glass.glassBackdropSource
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.GlassVariant
import com.example.ui.theme.LocalGlassConfig
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.tr
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: GlasslyViewModel,
    settings: GlasslySettingsState,
    backdropState: GlassBackdropState,
    hasMediaPermission: Boolean,
    isPartialPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val config = LocalGlassConfig.current

    val collapseProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 180f).coerceIn(0f, 1f)
        }
    }

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val scrollingDown = index > previousIndex || (index == previousIndex && offset > previousOffset + 24)
                val scrollingUp = index < previousIndex || (index == previousIndex && offset < previousOffset - 24)
                if (scrollingDown && index > 0) {
                    viewModel.setTabBarMinimized(true)
                } else if (scrollingUp || index == 0) {
                    viewModel.setTabBarMinimized(false)
                }
                previousIndex = index
                previousOffset = offset
            }
    }

    var showResetConfirmAlert by remember { mutableStateOf(false) }
    var showChangePinAlert by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf(settings.hiddenAlbumPin) }
    var showLicenseSheet by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Pure colorful refractive background recorded into backdropState (sibling to all GlassCards)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            RefractiveColorfulBackdrop(isDark = config.isDarkTheme)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_lazy_column"),
            contentPadding = PaddingValues(
                top = 98.dp,
                bottom = 130.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // 0. LIVE PREVIEW CARD AT THE TOP
                item(key = "live_preview_card") {
                    LiveGlassPreviewCard(
                        settings = settings,
                        modifier = Modifier.widthIn(max = 640.dp)
                    )
                }

                // 1. APPEARANCE
                item(key = "section_appearance") {
                    GlassCard(
                        title = tr("Appearance & Liquid Glass", "অ্যাপিয়ারেন্স এবং লিকুইড গ্লাস"),
                        subtitle = tr("Real-time refraction, dispersion & adaptive tint", "রিয়েল-টাইম প্রতিসরণ, বিচ্ছুরণ এবং রঙ"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = tr("Theme Mode", "থিম মোড"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val themeOptions = listOf(
                                tr("System", "সিস্টেম"),
                                tr("Light", "লাইট"),
                                tr("Dark", "ডার্ক")
                            )
                            GlassSegmentedControl(
                                options = themeOptions,
                                selectedIndex = settings.themeMode.ordinal,
                                onOptionSelected = { idx ->
                                    val mode = ThemeMode.entries[idx]
                                    scope.launch {
                                        viewModel.settingsRepo.update { it.copy(themeMode = mode) }
                                    }
                                },
                                testTagPrefix = "settings_theme_segment"
                            )
                        }

                        // Accent Tint Swatches
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = tr("Accent Tint", "অ্যাকসেন্ট রঙ"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassPalette.AccentSwatches.forEachIndexed { idx, swatchColor ->
                                    val isSelected = settings.accentSwatchIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                                                shape = CircleShape
                                            )
                                            .testTag("accent_swatch_$idx")
                                            .semantics {
                                                contentDescription = "Accent color ${idx + 1}"
                                                role = Role.Button
                                            }
                                            .clickable {
                                                scope.launch {
                                                    viewModel.settingsRepo.update { it.copy(accentSwatchIndex = idx) }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Glass Variant (Regular / Clear / Tinted)
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = tr("Glass Material Style", "গ্লাস স্টাইল"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val variants = listOf(
                                tr("Regular", "রেগুলার"),
                                tr("Clear", "ক্লিয়ার"),
                                tr("Tinted", "টিন্টেড")
                            )
                            GlassSegmentedControl(
                                options = variants,
                                selectedIndex = settings.glassVariant.ordinal,
                                onOptionSelected = { idx ->
                                    val v = GlassVariant.entries[idx]
                                    scope.launch {
                                        viewModel.settingsRepo.update { it.copy(glassVariant = v) }
                                    }
                                },
                                testTagPrefix = "settings_variant_segment"
                            )
                        }

                        // Glass Intensity Slider
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tr("Glass Refraction & Rim Intensity", "গ্লাসের তীব্রতা"),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(settings.glassIntensity * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = config.accentColor
                                )
                            }
                            GlassSlider(
                                value = settings.glassIntensity,
                                onValueChange = { newVal ->
                                    scope.launch {
                                        viewModel.settingsRepo.update { it.copy(glassIntensity = newVal) }
                                    }
                                },
                                valueRange = 0.15f..1.0f,
                                rainbowTrack = true,
                                contentDescriptionLabel = "Glass intensity",
                                testTagName = "settings_glass_intensity_slider"
                            )
                        }

                        FlatGlassRow(
                            title = tr("Reduce Glass (Tonal Solid Fallback)", "গ্লাস কমান (সলিড সারফেস)"),
                            subtitle = tr("Replaces refraction & blur with high-legibility frosted surfaces", "স্বচ্ছতা ও ব্লার কমিয়ে পরিষ্কার সলিড সারফেস দেখায়"),
                            leadingIcon = Icons.Rounded.AutoAwesome,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.reduceGlass,
                                    onCheckedChange = { checked ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(reduceGlass = checked) }
                                        }
                                    },
                                    contentDescriptionLabel = "Reduce glass",
                                    testTagName = "settings_reduce_glass_switch"
                                )
                            }
                        )
                    }
                }

                // 2. LIBRARY
                item(key = "section_library") {
                    GlassCard(
                        title = tr("Library & Grid", "লাইব্রেরি এবং গ্রিড"),
                        subtitle = tr("Grid density, aspect ratio, headers & badges", "গ্রিডের ঘনত্ব, অনুপাত, হেডার এবং ব্যাজ"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        FlatGlassRow(
                            title = tr("Default Grid Density", "ডিফল্ট গ্রিড কলাম"),
                            subtitle = tr("Columns in All Photos view", "সব ছবির গ্রিডে কলাম সংখ্যা"),
                            leadingIcon = Icons.Rounded.GridView,
                            trailing = {
                                GlassStepper(
                                    value = settings.defaultGridColumns,
                                    onValueChange = { cols ->
                                        viewModel.setGridColumns(cols)
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(defaultGridColumns = cols) }
                                        }
                                    },
                                    valueRange = 2..6,
                                    testTagPrefix = "settings_grid_cols"
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Square Photo Grid", "বর্গাকার ছবির গ্রিড"),
                            subtitle = tr("Turn off for original aspect-ratio tiles", "আসল অনুপাত দেখতে বন্ধ করুন"),
                            leadingIcon = Icons.Rounded.PhotoSizeSelectLarge,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.squareAspectGrid,
                                    onCheckedChange = { square ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(squareAspectGrid = square) }
                                        }
                                    },
                                    contentDescriptionLabel = "Square photo grid"
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Show Media Badges", "মিডিয়া ব্যাজ দেখান"),
                            subtitle = tr("Duration, LIVE, Favorite heart, RAW, GIF", "ভিডিও সময়, লাইভ, প্রিয় হার্ট, RAW, GIF"),
                            leadingIcon = Icons.Rounded.Hd,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.showBadges,
                                    onCheckedChange = { show ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(showBadges = show) }
                                        }
                                    },
                                    contentDescriptionLabel = "Show media badges"
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Open at Latest Photo", "সর্বশেষ ছবিতে খুলুন"),
                            subtitle = tr("Scrolls to bottom like iOS Photos on launch", "চালু করলে নিচে সর্বশেষ ছবিতে যাবে"),
                            leadingIcon = Icons.Rounded.Refresh,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.openAtLatest,
                                    onCheckedChange = { latest ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(openAtLatest = latest) }
                                        }
                                    },
                                    contentDescriptionLabel = "Open at latest photo"
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Show Location in Sticky Headers", "হেডারে অবস্থান দেখান"),
                            subtitle = tr("Displays city & landmark next to dates", "তারিখের পাশে শহরের নাম দেখায়"),
                            leadingIcon = Icons.Rounded.LocationOn,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.showLocationInHeaders,
                                    onCheckedChange = { loc ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(showLocationInHeaders = loc) }
                                        }
                                    },
                                    contentDescriptionLabel = "Show location in headers"
                                )
                            }
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = tr("Default Sort Order", "সাজানোর ক্রম"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            GlassPicker(
                                options = SortOrder.entries,
                                selectedOption = settings.sortOrder,
                                optionLabel = { order ->
                                    if (settings.language == AppLanguage.BANGLA) order.labelBn else order.labelEn
                                },
                                onOptionSelected = { order ->
                                    scope.launch {
                                        viewModel.settingsRepo.update { it.copy(sortOrder = order) }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 3. VIEWER & VIDEO
                item(key = "section_viewer") {
                    GlassCard(
                        title = tr("Viewer & Video", "ভিউয়ার এবং ভিডিও"),
                        subtitle = tr("Motion photos, video playback & gestures", "মোশন ফটো, ভিডিও প্লেব্যাক এবং জেসচার"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        FlatGlassRow(
                            title = tr("Autoplay Motion Photos", "মোশন ফটো অটোপ্লে"),
                            subtitle = tr("Also plays on long-press in viewer", "ভিউয়ারে লং-প্রেস করলেও চলবে"),
                            leadingIcon = Icons.Rounded.MotionPhotosOn,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.autoplayMotionPhotos,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(autoplayMotionPhotos = v) }
                                        }
                                    }
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Autoplay Videos", "ভিডিও অটোপ্লে"),
                            subtitle = tr("Start playback immediately when opened", "খোলার সাথে সাথে ভিডিও চালু হবে"),
                            leadingIcon = Icons.Rounded.PlayCircle,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.autoplayVideos,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(autoplayVideos = v) }
                                        }
                                    }
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Loop Videos", "ভিডিও লুপ করুন"),
                            subtitle = tr("Repeat videos seamlessly", "ভিডিও শেষ হলে আবার শুরু হবে"),
                            leadingIcon = Icons.Rounded.Repeat,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.loopVideos,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(loopVideos = v) }
                                        }
                                    }
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Show EXIF Info on Swipe Up", "সোয়াইপ আপ করলে তথ্য দেখান"),
                            subtitle = tr("Opens camera metadata, histogram & map sheet", "ক্যামেরা তথ্য ও ম্যাপ প্যানেল খোলে"),
                            leadingIcon = Icons.Rounded.Info,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.showInfoOnSwipeUp,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(showInfoOnSwipeUp = v) }
                                        }
                                    }
                                )
                            }
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = tr("Default Video Playback Speed", "ডিফল্ট ভিডিও গতি"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
                            GlassPicker(
                                options = speeds,
                                selectedOption = settings.defaultPlaybackSpeed,
                                optionLabel = { "${it}x" },
                                onOptionSelected = { spd ->
                                    scope.launch {
                                        viewModel.settingsRepo.update { it.copy(defaultPlaybackSpeed = spd) }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 4. PRIVACY & SECURITY
                item(key = "section_privacy") {
                    GlassCard(
                        title = tr("Privacy & Security", "গোপনীয়তা এবং নিরাপত্তা"),
                        subtitle = tr("Hidden album lock, Biometrics & Secure Screen", "হিডেন অ্যালবাম লক, বায়োমেট্রিক ও সিকিউর স্ক্রিন"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        FlatGlassRow(
                            title = tr("Lock Hidden Album", "হিডেন অ্যালবাম লক রাখুন"),
                            subtitle = tr("Require PIN or Biometric unlock to view Hidden photos", "হিডেন ছবি দেখতে পিন বা ফিঙ্গারপ্রিন্ট লাগবে"),
                            leadingIcon = Icons.Rounded.Lock,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.hiddenAlbumLockEnabled,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(hiddenAlbumLockEnabled = v) }
                                        }
                                    }
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Biometric Unlock", "বায়োমেট্রিক আনলক"),
                            subtitle = tr("Use fingerprint or face unlock alongside PIN", "পিনের পাশাপাশি ফিঙ্গারপ্রিন্ট ব্যবহার করুন"),
                            leadingIcon = Icons.Rounded.Fingerprint,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.biometricUnlockEnabled,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(biometricUnlockEnabled = v) }
                                        }
                                    }
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Change Hidden Album PIN", "হিডেন অ্যালবাম পিন পরিবর্তন করুন"),
                            subtitle = tr("Current PIN: •••• (Default: 2626)", "বর্তমান পিন: •••• (ডিফল্ট: 2626)"),
                            leadingIcon = Icons.Rounded.Password,
                            onClick = {
                                newPinInput = settings.hiddenAlbumPin
                                showChangePinAlert = true
                            },
                            trailing = {
                                GlassButton(
                                    onClick = {
                                        newPinInput = settings.hiddenAlbumPin
                                        showChangePinAlert = true
                                    },
                                    text = tr("Change", "পরিবর্তন"),
                                    style = GlassButtonStyle.REGULAR
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Secure Screen & Hide Recent Previews", "সিকিউর স্ক্রিন এবং রিসেন্ট প্রিভিউ লুকান"),
                            subtitle = tr("Blocks screenshots and masks app switcher preview", "অ্যাপ সুইচারে প্রিভিউ ঢেকে রাখে"),
                            leadingIcon = Icons.Rounded.Security,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.secureScreenFlag,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update {
                                                it.copy(secureScreenFlag = v, hidePreviewsInRecents = v)
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }

                // 5. STORAGE & CLEANUP
                item(key = "section_storage") {
                    GlassCard(
                        title = tr("Storage & Utilities", "স্টোরেজ এবং ক্লিনার"),
                        subtitle = tr("Cache management, retention, duplicates & large files", "ক্যাশ, ট্র্যাশ মেয়াদ, ডুপ্লিকেট এবং বড় ফাইল"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        FlatGlassRow(
                            title = tr("Thumbnail & Shader Cache", "থাম্বনেইল ও শেডার ক্যাশ"),
                            subtitle = tr("18.4 MB used for instant 120fps scrolling", "দ্রুত স্ক্রলিংয়ের জন্য ১৮.৪ মেগাবাইট ব্যবহৃত"),
                            leadingIcon = Icons.Rounded.Storage,
                            trailing = {
                                GlassButton(
                                    onClick = { viewModel.clearImageCache() },
                                    text = tr("Clear", "মুছুন"),
                                    style = GlassButtonStyle.REGULAR,
                                    testTagName = "settings_clear_cache_button"
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Recently Deleted Retention", "সম্প্রতি মুছে ফেলা ফাইলের মেয়াদ"),
                            subtitle = tr("Days before trashed items are permanently purged", "কত দিন পর ট্র্যাশ স্বয়ংক্রিয়ভাবে মুছে যাবে"),
                            leadingIcon = Icons.Rounded.DeleteSweep,
                            trailing = {
                                GlassStepper(
                                    value = settings.trashRetentionDays,
                                    onValueChange = { days ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(trashRetentionDays = days) }
                                        }
                                    },
                                    valueRange = 7..90,
                                    label = "d"
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Duplicate Photo Finder", "ডুপ্লিকেট ছবি খুঁজুন"),
                            subtitle = tr("Scan and merge identical or burst shots", "একই রকম বা ডুপ্লিকেট ছবি খুঁজে পরিষ্কার করুন"),
                            leadingIcon = Icons.Rounded.FileCopy,
                            onClick = { viewModel.navigateToSubScreen(SubScreenRoute.DuplicatesCleaner) }
                        )

                        FlatGlassRow(
                            title = tr("Large Files Storage Cleaner", "বড় ফাইল ক্লিনার"),
                            subtitle = tr("Review largest 4K videos and RAW files", "বড় ভিডিও এবং RAW ফাইল পর্যালোচনা করুন"),
                            leadingIcon = Icons.Rounded.CleaningServices,
                            onClick = { viewModel.navigateToSubScreen(SubScreenRoute.LargeFilesCleaner) }
                        )
                    }
                }

                // 6. LANGUAGE (Bangla / English instant switch)
                item(key = "section_language") {
                    GlassCard(
                        title = tr("Language / ভাষা", "ভাষা / Language"),
                        subtitle = tr("Switch between English and বাংলা instantly", "তাৎক্ষণিকভাবে ইংরেজি এবং বাংলা পরিবর্তন করুন"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        GlassSegmentedControl(
                            options = listOf("English", "বাংলা (Bangla)"),
                            selectedIndex = settings.language.ordinal,
                            onOptionSelected = { idx ->
                                val lang = AppLanguage.entries[idx]
                                scope.launch {
                                    viewModel.settingsRepo.update { it.copy(language = lang) }
                                }
                            },
                            testTagPrefix = "settings_language_segment"
                        )
                    }
                }

                // 7. ACCESSIBILITY
                item(key = "section_accessibility") {
                    GlassCard(
                        title = tr("Accessibility & Motion", "অ্যাক্সেসিবিলিটি এবং মোশন"),
                        subtitle = tr("WCAG AA contrast, touch targets, springs & haptics", "কনট্রাস্ট, টাচ টার্গেট এবং হ্যাপটিক"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        FlatGlassRow(
                            title = tr("Reduce Motion", "অ্যানিমেশন কমান"),
                            subtitle = tr("Replaces spring physics with calm crossfades", "স্প্রিং অ্যানিমেশন কমিয়ে সহজ ট্রানজিশন ব্যবহার করে"),
                            leadingIcon = Icons.Rounded.SlowMotionVideo,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.reduceMotion,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(reduceMotion = v) }
                                        }
                                    }
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Tactile Glass Haptics", "গ্লাস হ্যাপটিক ফিডব্যাক"),
                            subtitle = tr("Subtle tick when pressing or morphing glass controls", "গ্লাস বাটনে চাপ দিলে মৃদু ভাইব্রেশন"),
                            leadingIcon = Icons.Rounded.Vibration,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.hapticsEnabled,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(hapticsEnabled = v) }
                                        }
                                    }
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Larger Touch Targets (56dp)", "বড় টাচ টার্গেট (৫৬ ডিপি)"),
                            subtitle = tr("Expands all interactive controls above 48dp minimum", "সব বাটন ও কন্ট্রোল আরও বড় করে"),
                            leadingIcon = Icons.Rounded.TouchApp,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.largerTouchTargets,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(largerTouchTargets = v) }
                                        }
                                    }
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("High-Contrast Glass Scrim", "হাই-কনট্রাস্ট গ্লাস"),
                            subtitle = tr("Boosts text legibility and rim borders on bright photos", "উজ্জ্বল ছবির ওপর লেখার স্পষ্টতা বাড়ায়"),
                            leadingIcon = Icons.Rounded.AccessibilityNew,
                            trailing = {
                                GlassSwitch(
                                    checked = settings.highContrastGlass,
                                    onCheckedChange = { v ->
                                        scope.launch {
                                            viewModel.settingsRepo.update { it.copy(highContrastGlass = v) }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }

                // 8. NOTIFICATIONS & PERMISSIONS
                item(key = "section_permissions") {
                    GlassCard(
                        title = tr("Permissions & Media Access", "পারমিশন এবং মিডিয়া অ্যাক্সেস"),
                        subtitle = tr("Android 13/14/15 full or partial photo access", "অ্যান্ড্রয়েড ১৩/১৪/১৫ ছবি ও ভিডিও অ্যাক্সেস"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        val statusText = when {
                            hasMediaPermission && !isPartialPermission -> tr("Full Library Access Granted", "সম্পূর্ণ লাইব্রেরি অ্যাক্সেস দেওয়া হয়েছে")
                            isPartialPermission -> tr("Selected Photos Access (Partial)", "নির্বাচিত ছবির অ্যাক্সেস দেওয়া হয়েছে")
                            else -> tr("Curated Showcase Mode (Tap to Grant Device Access)", "কিউরেটেড মোড (ডিভাইস পারমিশন দিতে ট্যাপ করুন)")
                        }
                        FlatGlassRow(
                            title = tr("Media Library Permission", "মিডিয়া লাইব্রেরি পারমিশন"),
                            subtitle = statusText,
                            leadingIcon = Icons.Rounded.NotificationsActive,
                            trailing = {
                                GlassButton(
                                    onClick = onRequestPermission,
                                    text = tr("Manage", "পরিবর্তন"),
                                    style = GlassButtonStyle.TINTED,
                                    testTagName = "settings_manage_permission_button"
                                )
                            }
                        )
                    }
                }

                // 9. ABOUT GLASSLY (Single source of truth Version 0.01.10)
                item(key = "section_about") {
                    GlassCard(
                        title = tr("About Glassly", "গ্লাসলি সম্পর্কে"),
                        subtitle = tr("Crafted with Liquid Glass optical architecture", "লিকুইড গ্লাস অপটিক্যাল প্রযুক্তিতে তৈরি"),
                        modifier = Modifier.widthIn(max = 640.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LayeredGlassLogoBadge(modifier = Modifier.size(64.dp))
                            Column {
                                Text(
                                    text = "Glassly",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("about_app_name")
                                )
                                Text(
                                    text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = config.accentColor,
                                    modifier = Modifier.testTag("about_version_text")
                                )
                                Text(
                                    text = "Package: ${BuildConfig.APPLICATION_ID}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        FlatGlassRow(
                            title = tr("Glass Gallery (Component Showcase)", "গ্লাস গ্যালারি (কম্পোনেন্ট প্রদর্শনী)"),
                            subtitle = tr("Inspect all 20 Liquid Glass components in every state", "সব ২০টি লিকুইড গ্লাস কম্পোনেন্ট ও স্টেট দেখুন"),
                            leadingIcon = Icons.Rounded.BugReport,
                            onClick = { viewModel.navigateToSubScreen(SubScreenRoute.GlassGalleryDebug) },
                            trailing = {
                                GlassButton(
                                    onClick = { viewModel.navigateToSubScreen(SubScreenRoute.GlassGalleryDebug) },
                                    text = tr("Open", "খুলুন"),
                                    style = GlassButtonStyle.TINTED,
                                    testTagName = "open_glass_gallery_debug_button"
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Open-Source Licenses", "ওপেন-সোর্স লাইসেন্স"),
                            subtitle = tr("Jetpack Compose, Coil, Media3, Room, Kotlin Coroutines", "জেটপ্যাক কম্পোজ, কয়েল, মিডিয়া৩, রুম"),
                            leadingIcon = Icons.Rounded.Description,
                            onClick = { showLicenseSheet = true }
                        )

                        FlatGlassRow(
                            title = tr("Privacy Policy", "গোপনীয়তা নীতি"),
                            subtitle = tr("100% on-device processing. Zero cloud uploads.", "১০০% অন-ডিভাইস। কোনো ক্লাউড আপলোড নেই।"),
                            leadingIcon = Icons.Rounded.PrivacyTip,
                            onClick = { showPrivacySheet = true }
                        )

                        FlatGlassRow(
                            title = tr("Send Feedback & Rate Glassly", "মতামত পাঠান এবং রেটিং দিন"),
                            subtitle = tr("Tell us how Liquid Glass feels on your device", "আপনার অভিজ্ঞতা আমাদের জানান"),
                            leadingIcon = Icons.Rounded.StarRate,
                            onClick = { viewModel.showToast("Thank you for supporting Glassly 0.01.10!") }
                        )
                    }
                }

                // 10. RESET ALL SETTINGS (Expanding GlassAlert from trigger button)
                item(key = "section_reset") {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 640.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassButton(
                            onClick = { showResetConfirmAlert = true },
                            text = tr("Reset All Settings to Default", "সব সেটিংস ডিফল্টে রিসেট করুন"),
                            icon = Icons.Rounded.RestartAlt,
                            style = GlassButtonStyle.DANGER,
                            modifier = Modifier.fillMaxWidth(),
                            testTagName = "reset_all_settings_button"
                        )
                    }
                }
            }

        // Collapsing Top GlassToolbar floating above backdrop
        GlassToolbar(
            title = tr("Settings", "সেটিংস"),
            subtitle = tr("Version ${BuildConfig.VERSION_NAME}", "সংস্করণ ${BuildConfig.VERSION_NAME}"),
            collapseProgress = collapseProgress,
            actions = {
                GlassButton(
                    onClick = { viewModel.navigateToSubScreen(SubScreenRoute.GlassGalleryDebug) },
                    icon = Icons.Rounded.AutoAwesome,
                    style = GlassButtonStyle.ICON_CIRCLE,
                    contentDescriptionLabel = "Open Glass Gallery Showcase",
                    testTagName = "toolbar_glass_gallery_btn"
                )
            }
        )

        // Reset All Settings Confirmation GlassAlert expanding from bottom button
        GlassAlert(
            visible = showResetConfirmAlert,
            title = tr("Reset All Settings?", "সব সেটিংস রিসেট করবেন?"),
            message = tr(
                "This restores Glassly's Liquid Glass refraction, theme, grid density, and privacy settings to their factory defaults. Your photos and albums are untouched.",
                "এটি গ্লাসলির সব সেটিংস ডিফল্ট অবস্থায় ফিরিয়ে আনবে। আপনার ছবি এবং অ্যালবাম নিরাপদ থাকবে।"
            ),
            confirmText = tr("Reset", "রিসেট করুন"),
            dismissText = tr("Cancel", "বাতিল"),
            isDestructive = true,
            transformOrigin = TransformOrigin(0.5f, 0.9f),
            onConfirm = {
                scope.launch {
                    viewModel.settingsRepo.resetAll()
                    viewModel.showToast("All settings reset to default")
                }
            },
            onDismiss = { showResetConfirmAlert = false }
        )

        // Change PIN GlassAlert
        GlassAlert(
            visible = showChangePinAlert,
            title = tr("Set Hidden Album PIN", "হিডেন অ্যালবামের পিন সেট করুন"),
            message = tr("Enter a 4-digit PIN to protect your Hidden collection.", "আপনার হিডেন অ্যালবাম সুরক্ষিত করতে ৪ সংখ্যার পিন দিন।"),
            confirmText = tr("Save PIN", "সেভ করুন"),
            dismissText = tr("Cancel", "বাতিল"),
            onConfirm = {
                val cleaned = newPinInput.filter { it.isDigit() }.take(4).ifEmpty { "2626" }
                scope.launch {
                    viewModel.settingsRepo.update { it.copy(hiddenAlbumPin = cleaned) }
                    viewModel.showToast("Hidden Album PIN updated")
                }
            },
            onDismiss = { showChangePinAlert = false },
            extraContent = {
                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { if (it.length <= 4) newPinInput = it.filter { c -> c.isDigit() } },
                    label = { Text("4-Digit PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )

        // Open-Source Licenses Bottom Sheet
        GlassBottomSheet(
            visible = showLicenseSheet,
            title = tr("Open-Source Licenses", "ওপেন-সোর্স লাইসেন্স"),
            onDismiss = { showLicenseSheet = false }
        ) {
            FlatGlassRow(
                title = "Android Jetpack Compose & Material 3",
                subtitle = "Apache License 2.0 • Google LLC"
            )
            FlatGlassRow(
                title = "Coil (Coroutine Image Loader)",
                subtitle = "Apache License 2.0 • Coil Contributors"
            )
            FlatGlassRow(
                title = "AndroidX Media3 / ExoPlayer",
                subtitle = "Apache License 2.0 • Google LLC"
            )
            FlatGlassRow(
                title = "AndroidX Room & DataStore",
                subtitle = "Apache License 2.0 • Google LLC"
            )
        }

        // Privacy Policy Bottom Sheet
        GlassBottomSheet(
            visible = showPrivacySheet,
            title = tr("Privacy Guarantee", "গোপনীয়তার নিশ্চয়তা"),
            onDismiss = { showPrivacySheet = false }
        ) {
            FlatGlassRow(
                title = tr("100% On-Device Photo Library", "১০০% অন-ডিভাইস ফটো লাইব্রেরি"),
                subtitle = tr(
                    "Glassly never uploads your photos, videos, locations, or edits to any external server.",
                    "গ্লাসলি কখনোই আপনার ছবি, ভিডিও বা অবস্থান কোনো সার্ভারে পাঠায় না।"
                ),
                leadingIcon = Icons.Rounded.Security
            )
            FlatGlassRow(
                title = tr("Zero Telemetry & Zero Ads", "কোনো বিজ্ঞাপন বা ট্র্যাকিং নেই"),
                subtitle = tr(
                    "All search indexing, duplicate detection, and AGSL optical shaders run locally on your device.",
                    "সার্চ, ডুপ্লিকেট ক্লিনার এবং লিকুইড গ্লাস শেডার সম্পূর্ণ আপনার ফোনে চলে।"
                ),
                leadingIcon = Icons.Rounded.PrivacyTip
            )
        }
    }
}

/**
 * Interactive LIVE PREVIEW card at the top of Settings showing real-time Liquid Glass refraction,
 * chromatic rim dispersion, device tilt highlight, and press bounce over moving colorful orbs.
 */
@Composable
private fun LiveGlassPreviewCard(
    settings: GlasslySettingsState,
    modifier: Modifier = Modifier
) {
    val config = LocalGlassConfig.current
    var previewPressedCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(DesignTokens.ShapeCard)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.35f),
                shape = DesignTokens.ShapeCard
            )
    ) {
        // High-contrast vivid artwork canvas behind the preview glass lens
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0EA5E9),
                        Color(0xFF6366F1),
                        Color(0xFFEC4899),
                        Color(0xFFF59E0B)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
            // Geometric grid lines so refraction bending at the edges is unmistakably visible!
            val step = 28.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color.White.copy(alpha = 0.28f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.5f
                )
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.White.copy(alpha = 0.28f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5f
                )
                y += step
            }
            drawCircle(
                color = Color(0xFF34D399).copy(alpha = 0.85f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.25f, size.height * 0.55f)
            )
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.85f),
                radius = size.minDimension * 0.24f,
                center = Offset(size.width * 0.76f, size.height * 0.42f)
            )
        }

        // Floating Liquid Glass lens card inside the live preview
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(24.dp),
                    cornerRadius = 24.dp,
                    expansionProgress = 0.8f
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LayeredGlassLogoBadge(modifier = Modifier.size(32.dp))
                    Column {
                        Text(
                            text = tr("LIVE LIQUID GLASS PREVIEW", "লাইভ লিকুইড গ্লাস প্রিভিউ"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = config.accentColor
                        )
                        Text(
                            text = "${settings.glassVariant.name} • ${(settings.glassIntensity * 100).roundToInt()}% Optical Lens",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                GlassButton(
                    onClick = { previewPressedCount++ },
                    text = if (previewPressedCount == 0) tr("Tap Bounce", "ট্যাপ করুন") else "×$previewPressedCount",
                    style = GlassButtonStyle.TINTED
                )
            }
            Text(
                text = tr(
                    "Notice the grid lines and colorful orbs refracting along the rounded bevel rim with chromatic dispersion.",
                    "চারপাশের গোলাকার কোণায় আলোর প্রতিসরণ এবং রঙের বিচ্ছুরণ লক্ষ্য করুন।"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LayeredGlassLogoBadge(modifier: Modifier = Modifier) {
    val config = LocalGlassConfig.current
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Back prism card
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(config.accentColor, Color(0xFFEC4899))
            ),
            topLeft = Offset(w * 0.10f, h * 0.14f),
            size = Size(w * 0.65f, h * 0.65f),
            cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
        )
        // Front translucent glass lens
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.88f),
                    Color.White.copy(alpha = 0.42f)
                )
            ),
            topLeft = Offset(w * 0.26f, h * 0.24f),
            size = Size(w * 0.64f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
        )
    }
}

@Composable
fun RefractiveColorfulBackdrop(isDark: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val bgColors = if (isDark) {
            listOf(Color(0xFF070B14), Color(0xFF0F172A), Color(0xFF1E1B4B))
        } else {
            listOf(Color(0xFFF0F6FF), Color(0xFFE0E7FF), Color(0xFFFDF2F8))
        }
        drawRect(brush = Brush.verticalGradient(bgColors))

        val orbAlpha = if (isDark) 0.28f else 0.42f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = orbAlpha), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.18f),
                radius = size.minDimension * 0.55f
            ),
            center = Offset(size.width * 0.2f, size.height * 0.18f),
            radius = size.minDimension * 0.55f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFF43F5E).copy(alpha = orbAlpha), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.48f),
                radius = size.minDimension * 0.50f
            ),
            center = Offset(size.width * 0.85f, size.height * 0.48f),
            radius = size.minDimension * 0.50f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF10B981).copy(alpha = orbAlpha), Color.Transparent),
                center = Offset(size.width * 0.3f, size.height * 0.82f),
                radius = size.minDimension * 0.52f
            ),
            center = Offset(size.width * 0.3f, size.height * 0.82f),
            radius = size.minDimension * 0.52f
        )
    }
}

/**
 * Hidden "Glass Gallery" Debug Screen showing all 20 shared Liquid Glass components
 * in every state (normal, pressed, focused, disabled, selected, loading, Regular/Clear/Tinted).
 */
@Composable
fun GlassGalleryDebugScreen(
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var segIndex by remember { mutableIntStateOf(0) }
    var switchChecked by remember { mutableStateOf(true) }
    var sliderVal by remember { mutableFloatStateOf(0.68f) }
    var stepperVal by remember { mutableIntStateOf(4) }
    var searchVal by remember { mutableStateOf("") }
    var fusionSep by remember { mutableFloatStateOf(0.2f) }
    var menuOpen by remember { mutableStateOf(false) }
    var alertOpen by remember { mutableStateOf(false) }
    var sheetOpen by remember { mutableStateOf(false) }
    var pickerVal by remember { mutableStateOf("1.0x") }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            RefractiveColorfulBackdrop(isDark = LocalGlassConfig.current.isDarkTheme)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("glass_gallery_debug_list"),
            contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
                item {
                    GlassCard(
                        title = "1. GlassButton (All Variants & States)",
                        subtitle = "Regular, Tinted, Clear, Icon Circle, Disabled, Selected, Loading"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassButton(onClick = {}, text = "Regular", style = GlassButtonStyle.REGULAR)
                            GlassButton(onClick = {}, text = "Tinted", style = GlassButtonStyle.TINTED)
                            GlassButton(onClick = {}, text = "Clear", style = GlassButtonStyle.CLEAR)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassButton(onClick = {}, text = "Selected", selected = true)
                            GlassButton(onClick = {}, text = "Disabled", enabled = false)
                            GlassButton(onClick = {}, text = "Loading", loading = true)
                            GlassButton(
                                onClick = {},
                                icon = Icons.Rounded.Favorite,
                                style = GlassButtonStyle.ICON_CIRCLE,
                                contentDescriptionLabel = "Favorite Icon Circle"
                            )
                        }
                    }
                }

                item {
                    GlassCard(
                        title = "2. GlassContainer (Liquid Droplet Smooth-Union)",
                        subtitle = "Drag slider to watch adjacent glass droplets fuse and separate"
                    ) {
                        GlassContainer(
                            separationProgress = fusionSep,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GlassButton(onClick = {}, text = "Droplet A", style = GlassButtonStyle.REGULAR)
                            GlassButton(onClick = {}, icon = Icons.Rounded.AutoAwesome, style = GlassButtonStyle.ICON_CIRCLE)
                            GlassButton(onClick = {}, text = "Droplet B", style = GlassButtonStyle.TINTED)
                        }
                        GlassSlider(
                            value = fusionSep,
                            onValueChange = { fusionSep = it },
                            contentDescriptionLabel = "Droplet separation"
                        )
                    }
                }

                item {
                    GlassCard(
                        title = "3–6. Segmented, Switch, Slider, Stepper",
                        subtitle = "Interactive optical controls with spring physics"
                    ) {
                        GlassSegmentedControl(
                            options = listOf("Years", "Months", "Days", "All Photos"),
                            selectedIndex = segIndex,
                            onOptionSelected = { segIndex = it }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassSwitch(checked = switchChecked, onCheckedChange = { switchChecked = it })
                            GlassSwitch(checked = false, onCheckedChange = {}, enabled = false)
                            GlassStepper(value = stepperVal, onValueChange = { stepperVal = it }, label = "cols")
                        }
                        GlassSlider(value = sliderVal, onValueChange = { sliderVal = it }, rainbowTrack = true)
                    }
                }

                item {
                    GlassCard(
                        title = "7–9. SearchField, Chips & Thumbnail Badges",
                        subtitle = "Thumb-reach search, filter chips & Clear-Glass media badges"
                    ) {
                        GlassSearchField(
                            query = searchVal,
                            onQueryChange = { searchVal = it },
                            placeholder = "Search photos, places, 'videos last month'..."
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassChip(label = "Selected Chip", selected = true, onClick = {})
                            GlassChip(label = "Normal Chip", selected = false, onClick = {})
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassBadge(text = "0:42", icon = Icons.Rounded.PlayCircle)
                            GlassBadge(text = "LIVE", icon = Icons.Rounded.MotionPhotosOn)
                            GlassBadge(text = "RAW")
                            GlassBadge(icon = Icons.Rounded.Favorite, iconTint = Color(0xFFFF2D55))
                        }
                    }
                }

                item {
                    GlassCard(
                        title = "13–20. Menus, Alerts, Sheets, Scrubber, Picker & Loaders",
                        subtitle = "Expanding overlays & progress indicators"
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassButton(onClick = { menuOpen = !menuOpen }, text = "Expand Menu")
                            GlassButton(onClick = { alertOpen = true }, text = "Expand Alert")
                            GlassButton(onClick = { sheetOpen = true }, text = "Open Sheet")
                        }
                        GlassMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            items = listOf(
                                GlassMenuItem("fav", "Favorite", Icons.Rounded.Favorite, isSelected = true) {},
                                GlassMenuItem("del", "Delete", Icons.Rounded.DeleteSweep, isDestructive = true) {}
                            )
                        )
                        GlassScrubber(
                            currentLabel = "Nov 2026",
                            fraction = sliderVal,
                            onFractionChange = { sliderVal = it }
                        )
                        GlassPicker(
                            options = listOf("0.5x", "1.0x", "1.5x", "2.0x"),
                            selectedOption = pickerVal,
                            optionLabel = { it },
                            onOptionSelected = { pickerVal = it }
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassLoader(label = "Indeterminate")
                            GlassSkeletonTile(modifier = Modifier.size(90.dp), cornerRadius = 16.dp)
                        }
                    }
                }
            }

        GlassToolbar(
            title = "Glass Gallery (20 Components)",
            subtitle = "All Variants & States Verified",
            onBackClick = onBack
        )

        GlassAlert(
            visible = alertOpen,
            title = "Liquid Glass Alert",
            message = "Expands smoothly from its trigger button with increased lens thickness.",
            confirmText = "Awesome",
            dismissText = "Close",
            onConfirm = {},
            onDismiss = { alertOpen = false }
        )

        GlassBottomSheet(
            visible = sheetOpen,
            title = "Liquid Glass Sheet",
            onDismiss = { sheetOpen = false }
        ) {
            FlatGlassRow(
                title = "Flat Tonal Row Inside Sheet",
                subtitle = "Strictly obeys 'Never glass inside glass' rule"
            )
        }
    }
}
