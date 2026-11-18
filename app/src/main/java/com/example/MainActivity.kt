package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GlasslyViewModel
import com.example.ui.SubScreenRoute
import com.example.ui.glass.GlassTabBar
import com.example.ui.glass.GlassTabDestination
import com.example.ui.glass.GlassToast
import com.example.ui.glass.LocalGlassBackdrop
import com.example.ui.glass.rememberDeviceTiltOffset
import com.example.ui.glass.rememberGlassBackdropState
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.CollectionsScreen
import com.example.ui.screens.DuplicatesCleanerScreen
import com.example.ui.screens.GlassGalleryDebugScreen
import com.example.ui.screens.HiddenAlbumScreen
import com.example.ui.screens.LargeFilesCleanerScreen
import com.example.ui.screens.LayeredGlassLogoBadge
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PhotoEditorScreen
import com.example.ui.screens.PhotoViewerScreen
import com.example.ui.screens.RecentlyDeletedScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SlideshowScreen
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.GlassConfig
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.GlasslyTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlasslyAppRoot(
                onUpdateSecureWindow = { secure ->
                    if (secure) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
            )
        }
    }
}

@Composable
fun GlasslyAppRoot(
    viewModel: GlasslyViewModel = viewModel(),
    onUpdateSecureWindow: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val currentSubScreen by viewModel.currentSubScreen.collectAsState()
    val isTabBarMinimized by viewModel.isTabBarMinimized.collectAsState()
    val librarySegment by viewModel.librarySegment.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedMediaIds.collectAsState()
    val visibleMedia by viewModel.visibleLibraryMedia.collectAsState()
    val trashedMedia by viewModel.trashedMedia.collectAsState()
    val hiddenMedia by viewModel.hiddenMedia.collectAsState()
    val userAlbums by viewModel.userAlbums.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val isHiddenUnlocked by viewModel.isHiddenAlbumUnlocked.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isLoading by viewModel.mediaRepo.isLoadingFlow.collectAsState()

    LaunchedEffect(settings.secureScreenFlag) {
        onUpdateSecureWindow(settings.secureScreenFlag)
    }

    LaunchedEffect(settings.defaultGridColumns) {
        viewModel.setGridColumns(settings.defaultGridColumns)
    }

    // Android 13 / 14 / 15 full & partial media permission state
    var hasFullMediaPermission by remember {
        mutableStateOf(checkFullMediaPermission(context))
    }
    var hasPartialMediaPermission by remember {
        mutableStateOf(checkPartialMediaPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        hasFullMediaPermission = checkFullMediaPermission(context)
        hasPartialMediaPermission = checkPartialMediaPermission(context)
        viewModel.refreshLibrary()
    }

    val requestMediaPermissions: () -> Unit = {
        val perms = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                }
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        permissionLauncher.launch(perms)
    }

    // Auto-dismiss toast after 2.6 seconds
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2600)
            viewModel.clearToast()
        }
    }

    // Fast (<0.65s) Glassly Splash Logo fade & settle
    var showSplashOverlay by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(580)
        showSplashOverlay = false
    }

    val systemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val accentSwatch = GlassPalette.AccentSwatches.getOrElse(settings.accentSwatchIndex) {
        GlassPalette.AccentSwatches.first()
    }
    val tiltOffset = rememberDeviceTiltOffset()

    val glassConfig = remember(settings, isDark, accentSwatch, tiltOffset) {
        GlassConfig(
            variant = settings.glassVariant,
            intensity = settings.glassIntensity,
            refractionStrength = 26f * settings.glassIntensity,
            rimWidthDp = 14f,
            chromaticDispersion = 0.38f * settings.glassIntensity,
            blurRadiusDp = 18f * settings.glassIntensity,
            specularIntensity = 0.78f * settings.glassIntensity,
            noiseGrainAlpha = 0.032f,
            innerGlowAlpha = 0.28f * settings.glassIntensity,
            shadowElevationDp = 14f,
            accentColor = accentSwatch,
            reduceGlass = settings.reduceGlass,
            reduceMotion = settings.reduceMotion,
            highContrastGlass = settings.highContrastGlass,
            hapticsEnabled = settings.hapticsEnabled,
            hapticStrength = settings.hapticStrength,
            largerTouchTargets = settings.largerTouchTargets,
            isDarkTheme = isDark,
            lightDirectionX = tiltOffset.x,
            lightDirectionY = tiltOffset.y
        )
    }

    val backdropState = rememberGlassBackdropState()

    GlasslyTheme(
        themeMode = settings.themeMode,
        glassConfig = glassConfig,
        language = settings.language
    ) {
        CompositionLocalProvider(LocalGlassBackdrop provides backdropState) {
            // Handle back navigation when in selection mode or non-Library tab
            BackHandler(enabled = currentSubScreen != SubScreenRoute.None || isSelectionMode || currentTab != GlassTabDestination.LIBRARY) {
                when {
                    currentSubScreen != SubScreenRoute.None -> viewModel.popSubScreen()
                    isSelectionMode -> viewModel.toggleSelectionMode(false)
                    currentTab != GlassTabDestination.LIBRARY -> viewModel.selectTab(GlassTabDestination.LIBRARY)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("glassly_root_container")
            ) {
                // Main Screen / SubScreen switcher
                AnimatedContent(
                    targetState = currentSubScreen,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                    label = "subScreenNavigation"
                ) { subRoute ->
                    when (subRoute) {
                        SubScreenRoute.None -> {
                            // Main 4 Tabs: Library, Collections, Settings, Search
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(160)) },
                                label = "mainTabNavigation"
                            ) { tab ->
                                when (tab) {
                                    GlassTabDestination.LIBRARY -> LibraryScreen(
                                        viewModel = viewModel,
                                        mediaItems = visibleMedia,
                                        isLoading = isLoading,
                                        segment = librarySegment,
                                        gridColumns = gridColumns,
                                        isSelectionMode = isSelectionMode,
                                        selectedIds = selectedIds,
                                        userAlbums = userAlbums,
                                        settings = settings,
                                        backdropState = backdropState,
                                        hasMediaPermission = hasFullMediaPermission,
                                        isPartialPermission = hasPartialMediaPermission && !hasFullMediaPermission,
                                        onRequestPermission = requestMediaPermissions
                                    )

                                    GlassTabDestination.COLLECTIONS -> CollectionsScreen(
                                        viewModel = viewModel,
                                        mediaItems = visibleMedia,
                                        trashedItems = trashedMedia,
                                        hiddenItems = hiddenMedia,
                                        userAlbums = userAlbums,
                                        settings = settings,
                                        backdropState = backdropState
                                    )

                                    GlassTabDestination.SETTINGS -> SettingsScreen(
                                        viewModel = viewModel,
                                        settings = settings,
                                        backdropState = backdropState,
                                        hasMediaPermission = hasFullMediaPermission,
                                        isPartialPermission = hasPartialMediaPermission && !hasFullMediaPermission,
                                        onRequestPermission = requestMediaPermissions
                                    )

                                    GlassTabDestination.SEARCH -> SearchScreen(
                                        viewModel = viewModel,
                                        searchQuery = searchQuery,
                                        searchResults = searchResults,
                                        recentSearches = recentSearches,
                                        allMedia = visibleMedia,
                                        settings = settings,
                                        backdropState = backdropState
                                    )
                                }
                            }
                        }

                        is SubScreenRoute.PhotoViewer -> PhotoViewerScreen(
                            initialMediaId = subRoute.initialMediaId,
                            mediaItems = visibleMedia.ifEmpty { hiddenMedia },
                            settings = settings,
                            viewModel = viewModel,
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )

                        is SubScreenRoute.PhotoEditor -> PhotoEditorScreen(
                            mediaId = subRoute.mediaId,
                            mediaItems = visibleMedia,
                            viewModel = viewModel,
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )

                        is SubScreenRoute.AlbumDetail -> AlbumDetailScreen(
                            route = subRoute,
                            viewModel = viewModel,
                            allVisibleMedia = visibleMedia,
                            userAlbums = userAlbums,
                            settings = settings,
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )

                        SubScreenRoute.RecentlyDeleted -> RecentlyDeletedScreen(
                            trashedItems = trashedMedia,
                            settings = settings,
                            viewModel = viewModel,
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )

                        SubScreenRoute.HiddenAlbum -> HiddenAlbumScreen(
                            hiddenItems = hiddenMedia,
                            isUnlocked = isHiddenUnlocked,
                            settings = settings,
                            viewModel = viewModel,
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )

                        SubScreenRoute.DuplicatesCleaner -> DuplicatesCleanerScreen(
                            allVisibleMedia = visibleMedia,
                            viewModel = viewModel,
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )

                        SubScreenRoute.LargeFilesCleaner -> LargeFilesCleanerScreen(
                            allVisibleMedia = visibleMedia,
                            viewModel = viewModel,
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )

                        is SubScreenRoute.Slideshow -> SlideshowScreen(
                            mediaIds = subRoute.mediaIds,
                            allMedia = visibleMedia,
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )

                        SubScreenRoute.GlassGalleryDebug -> GlassGalleryDebugScreen(
                            backdropState = backdropState,
                            onBack = { viewModel.popSubScreen() }
                        )
                    }
                }

                // Floating iOS 26 Liquid Glass Tab Bar (shown on main tabs when not in selection mode)
                AnimatedVisibility(
                    visible = currentSubScreen == SubScreenRoute.None && !isSelectionMode,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    GlassTabBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        isMinimized = isTabBarMinimized,
                        onExpandRequested = { viewModel.setTabBarMinimized(false) },
                        languageIsBangla = settings.language == AppLanguage.BANGLA
                    )
                }

                // Floating Liquid Glass Toast Notification
                GlassToast(
                    message = toastMessage,
                    onDismiss = { viewModel.clearToast() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top = 64.dp)
                )

                // Sub-1-second Glassly Layered Glass Splash Overlay
                AnimatedVisibility(
                    visible = showSplashOverlay,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(320))
                ) {
                    val splashScale by animateFloatAsState(
                        targetValue = if (showSplashOverlay) 1.0f else 1.08f,
                        animationSpec = tween(450),
                        label = "splashSettleScale"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.graphicsLayer {
                                scaleX = splashScale
                                scaleY = splashScale
                            }
                        ) {
                            LayeredGlassLogoBadge(modifier = Modifier.size(92.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Glassly",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Liquid Glass Photos • v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelLarge,
                                color = glassConfig.accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun checkFullMediaPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
    }
}

private fun checkPartialMediaPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        false
    }
}
