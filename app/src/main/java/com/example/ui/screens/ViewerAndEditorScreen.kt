package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MotionPhotosOn
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.GlasslySettingsState
import com.example.data.MediaItem
import com.example.data.PhotoEditState
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
import com.example.ui.glass.GlassMenu
import com.example.ui.glass.GlassMenuItem
import com.example.ui.glass.GlassPicker
import com.example.ui.glass.GlassScrubber
import com.example.ui.glass.GlassSegmentedControl
import com.example.ui.glass.GlassSlider
import com.example.ui.glass.GlassToolbar
import com.example.ui.glass.GlassBackdropState
import com.example.ui.glass.glassBackdropSource
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.LocalGlassConfig
import com.example.ui.theme.tr
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    initialMediaId: Long,
    mediaItems: List<MediaItem>,
    settings: GlasslySettingsState,
    viewModel: GlasslyViewModel,
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val config = LocalGlassConfig.current

    val initialIndex = remember(initialMediaId, mediaItems) {
        mediaItems.indexOfFirst { it.id == initialMediaId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { mediaItems.size.coerceAtLeast(1) }
    )

    val currentItem = mediaItems.getOrNull(pagerState.currentPage) ?: mediaItems.firstOrNull()
    if (currentItem == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var chromeVisible by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showRenameAlert by remember { mutableStateOf(false) }
    var renameText by remember(currentItem.id) { mutableStateOf(currentItem.title) }
    var showDeleteAlert by remember { mutableStateOf(false) }

    // Zoom, pan, swipe-down to dismiss & long-press motion photo state
    var zoomScale by remember(currentItem.id) { mutableFloatStateOf(1f) }
    var panOffset by remember(currentItem.id) { mutableStateOf(Offset.Zero) }
    var verticalDragDismissOffset by remember(currentItem.id) { mutableFloatStateOf(0f) }
    var isPlayingLiveMotion by remember(currentItem.id) { mutableStateOf(false) }

    // Video playback state
    var isVideoPlaying by remember(currentItem.id) { mutableStateOf(settings.autoplayVideos) }
    var isVideoMuted by remember(currentItem.id) { mutableStateOf(false) }
    var isVideoLooping by remember(currentItem.id) { mutableStateOf(settings.loopVideos) }
    var videoSpeed by remember(currentItem.id) { mutableFloatStateOf(settings.defaultPlaybackSpeed) }
    var videoProgress by remember(currentItem.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(currentItem.id, isVideoPlaying, videoSpeed) {
        if (currentItem.isVideo && isVideoPlaying) {
            while (isVideoPlaying) {
                delay(50)
                val step = 0.01f * videoSpeed
                val next = videoProgress + step
                if (next >= 1f) {
                    if (isVideoLooping) videoProgress = 0f
                    else {
                        videoProgress = 1f
                        isVideoPlaying = false
                    }
                } else {
                    videoProgress = next
                }
            }
        }
    }

    val liveMotionScale by animateFloatAsState(
        targetValue = if (isPlayingLiveMotion) 1.08f else 1f,
        animationSpec = tween(durationMillis = 650),
        label = "livePhotoMotionScale"
    )

    val dismissProgress = (abs(verticalDragDismissOffset) / 480f).coerceIn(0f, 1f)
    val bgAlpha = (1f - dismissProgress * 0.65f).coerceIn(0.2f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (!chromeVisible) Color.Black.copy(alpha = bgAlpha)
                else MaterialTheme.colorScheme.background.copy(alpha = bgAlpha)
            )
    ) {
        // Photo/Video Canvas recorded into backdropState so top/bottom Liquid Glass bars refract the photo!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = zoomScale <= 1.05f,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("photo_viewer_pager")
            ) { page ->
                val item = mediaItems.getOrNull(page) ?: return@HorizontalPager
                val editMatrix = remember(item.editState) {
                    buildEditColorMatrix(
                        item.editState.brightness,
                        item.editState.contrast,
                        item.editState.saturation,
                        item.editState.warmth
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(item.id) {
                            detectTapGestures(
                                onTap = { chromeVisible = !chromeVisible },
                                onDoubleTap = {
                                    if (zoomScale > 1.1f) {
                                        zoomScale = 1f
                                        panOffset = Offset.Zero
                                    } else {
                                        zoomScale = 2.5f
                                    }
                                },
                                onLongPress = {
                                    if (item.isLivePhoto) {
                                        isPlayingLiveMotion = true
                                    } else if (settings.showInfoOnSwipeUp) {
                                        showInfoSheet = true
                                    }
                                },
                                onPress = {
                                    tryAwaitRelease()
                                    isPlayingLiveMotion = false
                                }
                            )
                        }
                        .pointerInput(item.id) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (zoomScale * zoom).coerceIn(1f, 4f)
                                zoomScale = newScale
                                if (newScale > 1.02f) {
                                    panOffset += pan
                                } else {
                                    panOffset = Offset.Zero
                                    // Swipe up opens EXIF Info sheet; swipe down dismisses viewer!
                                    if (pan.y < -36f && settings.showInfoOnSwipeUp) {
                                        showInfoSheet = true
                                    } else if (pan.y > 12f) {
                                        verticalDragDismissOffset += pan.y
                                        if (verticalDragDismissOffset > 220f) {
                                            onBack()
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Check if real video file exists on device for ExoPlayer, otherwise render high-res photo + animated video overlay
                    val isRealDeviceVideo = item.isVideo && !item.filePath.contains("curated_glassly_media")
                    if (isRealDeviceVideo) {
                        RealExoVideoPlayer(
                            uriString = item.uri,
                            playWhenReady = isVideoPlaying,
                            isMuted = isVideoMuted,
                            isLooping = isVideoLooping,
                            playbackSpeed = videoSpeed
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(item.filePath).takeIf { it.exists() } ?: item.uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            colorFilter = if (item.editState.hasEdits) ColorFilter.colorMatrix(editMatrix) else null,
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset(panOffset.x.roundToInt(), (panOffset.y + verticalDragDismissOffset).roundToInt()) }
                                .graphicsLayer {
                                    val effectiveScale = zoomScale * liveMotionScale * (1f - dismissProgress * 0.18f)
                                    scaleX = effectiveScale * (if (item.editState.flipHorizontal) -1f else 1f)
                                    scaleY = effectiveScale * (if (item.editState.flipVertical) -1f else 1f)
                                    rotationZ = item.editState.rotationDegrees + item.editState.straightenDegrees
                                }
                        )
                    }

                    // Overlay text from non-destructive editor if present
                    if (item.editState.overlayText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(DesignTokens.ShapePill)
                                .background(Color.Black.copy(alpha = 0.48f))
                                .border(1.dp, Color.White.copy(alpha = 0.65f), DesignTokens.ShapePill)
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = item.editState.overlayText,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Top Floating Liquid Glass Bar (fades out when chrome is toggled off)
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column {
                GlassToolbar(
                    title = currentItem.locationName ?: currentItem.title,
                    subtitle = "${currentItem.dateLabel} • ${currentItem.timeLabel}",
                    onBackClick = onBack,
                    actions = {
                        if (zoomScale > 1.05f) {
                            GlassChip(
                                label = "${(zoomScale * 10f).roundToInt() / 10f}x",
                                selected = true,
                                onClick = {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                }
                            )
                        }
                        if (currentItem.isLivePhoto) {
                            GlassChip(
                                label = if (isPlayingLiveMotion) "LIVE • PLAYING" else "LIVE",
                                selected = isPlayingLiveMotion,
                                leadingIcon = Icons.Rounded.MotionPhotosOn,
                                onClick = { isPlayingLiveMotion = !isPlayingLiveMotion }
                            )
                        }
                        Box {
                            GlassButton(
                                onClick = { showMoreMenu = !showMoreMenu },
                                icon = Icons.Rounded.MoreVert,
                                style = GlassButtonStyle.ICON_CIRCLE,
                                contentDescriptionLabel = "More photo actions",
                                testTagName = "viewer_more_menu_button"
                            )
                            GlassMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                headerTitle = currentItem.title,
                                items = listOf(
                                    GlassMenuItem(
                                        id = "rename",
                                        title = tr("Rename File", "নাম পরিবর্তন করুন"),
                                        icon = Icons.Rounded.DriveFileRenameOutline,
                                        onClick = {
                                            renameText = currentItem.title
                                            showRenameAlert = true
                                        }
                                    ),
                                    GlassMenuItem(
                                        id = "wallpaper",
                                        title = tr("Set as Wallpaper", "ওয়ালপেপার হিসেবে সেট করুন"),
                                        icon = Icons.Rounded.Wallpaper,
                                        onClick = { viewModel.setAsWallpaper(currentItem) }
                                    ),
                                    GlassMenuItem(
                                        id = "hide",
                                        title = tr("Move to Hidden Album", "হিডেন অ্যালবামে লুকান"),
                                        icon = Icons.Rounded.VisibilityOff,
                                        onClick = {
                                            viewModel.toggleHidden(listOf(currentItem.id), hide = true)
                                            onBack()
                                        }
                                    ),
                                    GlassMenuItem(
                                        id = "slideshow",
                                        title = tr("Start Slideshow Here", "এখান থেকে স্লাইডশো চালান"),
                                        icon = Icons.Rounded.Slideshow,
                                        onClick = {
                                            viewModel.navigateToSubScreen(
                                                SubScreenRoute.Slideshow(mediaItems.map { it.id })
                                            )
                                        }
                                    ),
                                    GlassMenuItem(
                                        id = "print",
                                        title = tr("Print / Export PDF", "প্রিন্ট বা পিডিএফ"),
                                        icon = Icons.Rounded.Print,
                                        onClick = { viewModel.showToast("Prepared print layout for ${currentItem.title}") }
                                    )
                                )
                            )
                        }
                    }
                )
            }
        }

        // Bottom Floating Liquid Glass Controls:
        // - Video Player custom glass controls (if current item is a Video)
        // - Filmstrip thumbnail scrubber
        // - Bottom GlassActionBar (Share, Favorite, Info, Edit, Delete)
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Video Player Custom Liquid Glass Bar
                if (currentItem.isVideo) {
                    VideoGlassControllerBar(
                        isPlaying = isVideoPlaying,
                        onTogglePlay = { isVideoPlaying = !isVideoPlaying },
                        progress = videoProgress,
                        onProgressChange = { videoProgress = it },
                        isMuted = isVideoMuted,
                        onToggleMute = { isVideoMuted = !isVideoMuted },
                        isLooping = isVideoLooping,
                        onToggleLoop = { isVideoLooping = !isVideoLooping },
                        speed = videoSpeed,
                        onSpeedChange = { videoSpeed = it },
                        onEnterPiP = {
                            val activity = context as? Activity
                            if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                runCatching {
                                    activity.enterPictureInPictureMode(
                                        PictureInPictureParams.Builder()
                                            .setAspectRatio(Rational(16, 9))
                                            .build()
                                    )
                                }
                            } else {
                                viewModel.showToast("Picture-in-Picture active")
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // Filmstrip Scrubber Strip
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("viewer_filmstrip_row")
                ) {
                    itemsIndexed(mediaItems, key = { _, m -> "strip_${m.id}" }) { idx, item ->
                        val isCurrent = idx == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (isCurrent) 48.dp else 34.dp,
                                    height = if (isCurrent) 48.dp else 38.dp
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isCurrent) 2.dp else 0.5.dp,
                                    color = if (isCurrent) config.accentColor else Color.White.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    scope.launch { pagerState.animateScrollToPage(idx) }
                                }
                        ) {
                            AsyncImage(
                                model = File(item.filePath).takeIf { it.exists() } ?: item.uri,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Bottom GlassActionBar (Share, Favorite, Info, Edit, Delete)
                GlassActionBar(
                    actions = listOf(
                        GlassActionItem(
                            id = "share",
                            label = tr("Share", "শেয়ার"),
                            icon = Icons.Rounded.Share,
                            onClick = { viewModel.shareMediaItems(listOf(currentItem)) }
                        ),
                        GlassActionItem(
                            id = "favorite",
                            label = if (currentItem.isFavorite) tr("Favorited", "প্রিয়") else tr("Favorite", "প্রিয়"),
                            icon = if (currentItem.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            onClick = { viewModel.toggleFavorite(listOf(currentItem.id)) }
                        ),
                        GlassActionItem(
                            id = "info",
                            label = tr("Info", "তথ্য"),
                            icon = Icons.Rounded.Info,
                            onClick = { showInfoSheet = true }
                        ),
                        GlassActionItem(
                            id = "edit",
                            label = tr("Edit", "এডিট"),
                            icon = Icons.Rounded.Edit,
                            onClick = {
                                viewModel.navigateToSubScreen(SubScreenRoute.PhotoEditor(currentItem.id))
                            }
                        ),
                        GlassActionItem(
                            id = "delete",
                            label = tr("Delete", "মুছুন"),
                            icon = Icons.Rounded.Delete,
                            isDestructive = true,
                            onClick = { showDeleteAlert = true }
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Swipe-Up EXIF Info Panel (GlassBottomSheet with flat tonal rows inside)
        GlassBottomSheet(
            visible = showInfoSheet,
            title = tr("Photo & EXIF Metadata", "ছবির বিস্তারিত তথ্য"),
            onDismiss = { showInfoSheet = false }
        ) {
            FlatGlassRow(
                title = currentItem.title,
                subtitle = "${currentItem.dateLabel} • ${currentItem.timeLabel}",
                leadingIcon = Icons.Rounded.Info
            )
            FlatGlassRow(
                title = "${currentItem.cameraModel} • ${currentItem.lensInfo}",
                subtitle = "${currentItem.resolutionLabel} (${currentItem.megapixelsLabel}) • ${currentItem.formattedSize}",
                leadingIcon = Icons.Rounded.CameraAlt
            )
            if (currentItem.locationName != null) {
                FlatGlassRow(
                    title = currentItem.locationName,
                    subtitle = "GPS: ${currentItem.latitude ?: 0.0}, ${currentItem.longitude ?: 0.0}",
                    leadingIcon = Icons.Rounded.LocationOn
                )
            }
            FlatGlassRow(
                title = tr("File Path", "ফাইলের অবস্থান"),
                subtitle = currentItem.filePath,
                leadingIcon = Icons.Rounded.Folder
            )
        }

        // Rename File GlassAlert
        GlassAlert(
            visible = showRenameAlert,
            title = tr("Rename Item", "ফাইলের নাম পরিবর্তন করুন"),
            message = tr("Enter a new title for this photo or video.", "নতুন নাম লিখুন।"),
            confirmText = tr("Rename", "সেভ"),
            dismissText = tr("Cancel", "বাতিল"),
            onConfirm = {
                viewModel.renameMediaItem(currentItem.id, renameText)
                showRenameAlert = false
            },
            onDismiss = { showRenameAlert = false },
            extraContent = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )

        // Delete Single Item Confirmation Alert
        GlassAlert(
            visible = showDeleteAlert,
            title = tr("Move to Recently Deleted?", "ট্র্যাশে পাঠাবেন?"),
            message = tr(
                "\"${currentItem.title}\" will be kept in Recently Deleted for ${settings.trashRetentionDays} days.",
                "\"${currentItem.title}\" ${settings.trashRetentionDays} দিন পর্যন্ত রিসেন্টলি ডিলিটেড ফোল্ডারে থাকবে।"
            ),
            confirmText = tr("Delete", "মুছুন"),
            dismissText = tr("Cancel", "বাতিল"),
            isDestructive = true,
            onConfirm = {
                viewModel.moveToTrash(listOf(currentItem.id))
                showDeleteAlert = false
                onBack()
            },
            onDismiss = { showDeleteAlert = false }
        )
    }
}

@Composable
private fun VideoGlassControllerBar(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    isLooping: Boolean,
    onToggleLoop: () -> Unit,
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    onEnterPiP: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = DesignTokens.ShapeCard, cornerRadius = DesignTokens.RadiusCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassButton(
                onClick = onTogglePlay,
                icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                style = GlassButtonStyle.ICON_CIRCLE,
                contentDescriptionLabel = if (isPlaying) "Pause video" else "Play video",
                testTagName = "video_play_pause_button"
            )
            GlassSlider(
                value = progress,
                onValueChange = onProgressChange,
                modifier = Modifier.weight(1f),
                contentDescriptionLabel = "Video timeline scrubber"
            )
            GlassButton(
                onClick = onToggleMute,
                icon = if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                style = GlassButtonStyle.ICON_CIRCLE,
                contentDescriptionLabel = "Toggle mute"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlassPicker(
                options = listOf(0.5f, 1.0f, 1.5f, 2.0f),
                selectedOption = speed,
                optionLabel = { "${it}x" },
                onOptionSelected = onSpeedChange
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassButton(
                    onClick = onToggleLoop,
                    icon = Icons.Rounded.Loop,
                    selected = isLooping,
                    style = GlassButtonStyle.ICON_CIRCLE,
                    contentDescriptionLabel = "Loop video"
                )
                GlassButton(
                    onClick = onEnterPiP,
                    icon = Icons.Rounded.PictureInPictureAlt,
                    style = GlassButtonStyle.ICON_CIRCLE,
                    contentDescriptionLabel = "Picture in Picture"
                )
            }
        }
    }
}

@Composable
private fun RealExoVideoPlayer(
    uriString: String,
    playWhenReady: Boolean,
    isMuted: Boolean,
    isLooping: Boolean,
    playbackSpeed: Float
) {
    val context = LocalContext.current
    val exoPlayer = remember(uriString) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(ExoMediaItem.fromUri(uriString))
            prepare()
        }
    }

    LaunchedEffect(playWhenReady, isMuted, isLooping, playbackSpeed) {
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.volume = if (isMuted) 0f else 1f
        exoPlayer.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        exoPlayer.setPlaybackSpeed(playbackSpeed)
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * 5. PHOTO EDITOR SCREEN:
 * - Crop (free and ratios: 1:1, 4:3, 16:9, 9:16), Rotate, Flip, Straighten with dial
 * - Adjust: Brightness, Contrast, Saturation, Warmth, Sharpness, Vignette with glass sliders
 * - Filters carousel
 * - Markup / Draw & Text overlay
 * - Non-destructive edits, Revert, Save as copy or overwrite, Compare by holding
 */
@Composable
fun PhotoEditorScreen(
    mediaId: Long,
    mediaItems: List<MediaItem>,
    viewModel: GlasslyViewModel,
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val config = LocalGlassConfig.current
    val item = mediaItems.find { it.id == mediaId } ?: mediaItems.firstOrNull()
    if (item == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var editTabIndex by remember { mutableIntStateOf(0) } // 0: Adjust, 1: Crop/Rotate, 2: Filters, 3: Markup/Text

    var cropRatio by remember(item.id) { mutableStateOf(item.editState.cropRatio) }
    var rotationDegrees by remember(item.id) { mutableFloatStateOf(item.editState.rotationDegrees) }
    var straightenDegrees by remember(item.id) { mutableFloatStateOf(item.editState.straightenDegrees) }
    var flipH by remember(item.id) { mutableStateOf(item.editState.flipHorizontal) }
    var flipV by remember(item.id) { mutableStateOf(item.editState.flipVertical) }

    var brightness by remember(item.id) { mutableFloatStateOf(item.editState.brightness) }
    var contrast by remember(item.id) { mutableFloatStateOf(item.editState.contrast) }
    var saturation by remember(item.id) { mutableFloatStateOf(item.editState.saturation) }
    var warmth by remember(item.id) { mutableFloatStateOf(item.editState.warmth) }
    var sharpness by remember(item.id) { mutableFloatStateOf(item.editState.sharpness) }
    var vignette by remember(item.id) { mutableFloatStateOf(item.editState.vignette) }
    var filterName by remember(item.id) { mutableStateOf(item.editState.filterName) }
    var overlayText by remember(item.id) { mutableStateOf(item.editState.overlayText) }
    val markupPoints = remember(item.id) { mutableStateListOf<Pair<Float, Float>>() }

    var activeAdjustParam by remember { mutableStateOf("Brightness") }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Hold to compare interaction source
    val compareInteraction = remember { MutableInteractionSource() }
    val isHoldingCompare by compareInteraction.collectIsPressedAsState()

    val currentEditState = PhotoEditState(
        cropRatio = cropRatio,
        rotationDegrees = rotationDegrees,
        straightenDegrees = straightenDegrees,
        flipHorizontal = flipH,
        flipVertical = flipV,
        brightness = brightness,
        contrast = contrast,
        saturation = saturation,
        warmth = warmth,
        sharpness = sharpness,
        vignette = vignette,
        filterName = filterName,
        overlayText = overlayText,
        strokePoints = markupPoints.toList(),
        markupStrokesCount = markupPoints.size
    )

    val colorMatrix = remember(brightness, contrast, saturation, warmth, filterName, isHoldingCompare) {
        if (isHoldingCompare) {
            buildEditColorMatrix(0f, 1f, 1f, 0f)
        } else {
            val presetBoost = when (filterName) {
                "Vivid Glass" -> Quad(0.06f, 1.18f, 1.35f, 0.05f)
                "Warm Amber" -> Quad(0.04f, 1.08f, 1.15f, 0.45f)
                "Cool Prism" -> Quad(0.02f, 1.12f, 1.10f, -0.45f)
                "Noir Mono" -> Quad(0.0f, 1.28f, 0.0f, 0.0f)
                "Cinema Teal" -> Quad(-0.03f, 1.22f, 0.92f, -0.25f)
                else -> Quad(0f, 0f, 0f, 0f)
            }
            buildEditColorMatrix(
                brightness + presetBoost.b,
                (contrast + presetBoost.c).coerceAtLeast(0.4f),
                if (filterName == "Noir Mono") 0f else (saturation + presetBoost.s).coerceAtLeast(0f),
                warmth + presetBoost.w
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060911))
    ) {
        // Photo Canvas recorded into backdropState
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
                .padding(top = 88.dp, bottom = 250.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val targetAspect = when (cropRatio) {
                1f -> 1f
                4f / 3f -> 4f / 3f
                16f / 9f -> 16f / 9f
                9f / 16f -> 9f / 16f
                else -> item.itemAspectRatio.coerceIn(0.6f, 1.7f)
            }

            Box(
                modifier = Modifier
                    .aspectRatio(targetAspect)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    .pointerInput(editTabIndex) {
                        if (editTabIndex == 3) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val nx = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val ny = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                                markupPoints.add(nx to ny)
                            }
                        }
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(item.filePath).takeIf { it.exists() } ?: item.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Editing ${item.title}",
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (!isHoldingCompare) {
                                rotationZ = rotationDegrees + straightenDegrees
                                scaleX = if (flipH) -1f else 1f
                                scaleY = if (flipV) -1f else 1f
                            }
                        }
                )

                // Vignette & Markup strokes canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (!isHoldingCompare && vignette > 0.02f) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = vignette * 0.78f)),
                                radius = size.minDimension * 0.68f
                            )
                        )
                    }
                    if (!isHoldingCompare && markupPoints.size > 1) {
                        val path = Path()
                        markupPoints.forEachIndexed { idx, (nx, ny) ->
                            val px = nx * size.width
                            val py = ny * size.height
                            if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        drawPath(
                            path = path,
                            color = config.accentColor,
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Text overlay preview
                if (!isHoldingCompare && overlayText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(DesignTokens.ShapePill)
                            .background(Color.Black.copy(alpha = 0.48f))
                            .border(1.dp, Color.White.copy(alpha = 0.65f), DesignTokens.ShapePill)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = overlayText,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // "ORIGINAL" indicator badge while holding Compare (sibling outside backdrop source)
        if (isHoldingCompare) {
            GlassBadge(
                text = tr("ORIGINAL", "আসল ছবি"),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 104.dp)
            )
        }

        // Top Liquid Glass Editor Toolbar (Cancel, Revert, Hold to Compare, Save)
        GlassToolbar(
            title = tr("Editor", "ফটো এডিটর"),
            subtitle = if (currentEditState.hasEdits) tr("Non-destructive edits active", "এডিট চালু আছে") else tr("Original", "আসল"),
            onBackClick = onBack,
            actions = {
                // Hold to Compare button
                Box(
                    modifier = Modifier
                        .clip(DesignTokens.ShapePill)
                        .liquidGlass(shape = DesignTokens.ShapePill, cornerRadius = DesignTokens.RadiusPill)
                        .clickable(
                            interactionSource = compareInteraction,
                            indication = null,
                            onClick = {}
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("editor_compare_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Compare,
                            contentDescription = "Hold to compare original",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = tr("Compare", "তুলনা"),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                GlassButton(
                    onClick = {
                        cropRatio = null
                        rotationDegrees = 0f
                        straightenDegrees = 0f
                        flipH = false
                        flipV = false
                        brightness = 0f
                        contrast = 1f
                        saturation = 1f
                        warmth = 0f
                        sharpness = 0f
                        vignette = 0f
                        filterName = "Original"
                        overlayText = ""
                        markupPoints.clear()
                        viewModel.revertPhotoEdits(item.id)
                    },
                    icon = Icons.Rounded.Restore,
                    style = GlassButtonStyle.ICON_CIRCLE,
                    contentDescriptionLabel = "Revert to original",
                    testTagName = "editor_revert_button"
                )

                GlassButton(
                    onClick = { showSaveDialog = true },
                    text = tr("Save", "সেভ"),
                    icon = Icons.Rounded.Save,
                    style = GlassButtonStyle.TINTED,
                    testTagName = "editor_save_button"
                )
            }
        )

        // Bottom Liquid Glass Editor Control Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .liquidGlass(
                    shape = DesignTokens.ShapeSheet,
                    cornerRadius = DesignTokens.RadiusSheet,
                    expansionProgress = 0.8f
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (editTabIndex) {
                0 -> {
                    // ADJUST SLIDERS (Brightness, Contrast, Saturation, Warmth, Sharpness, Vignette)
                    val params = listOf("Brightness", "Contrast", "Saturation", "Warmth", "Sharpness", "Vignette")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(params) { _, param ->
                            GlassChip(
                                label = param,
                                selected = activeAdjustParam == param,
                                onClick = { activeAdjustParam = param }
                            )
                        }
                    }

                    val (sliderValue, sliderRange, onValChange) = when (activeAdjustParam) {
                        "Brightness" -> Triple(brightness, -0.8f..0.8f) { v: Float -> brightness = v }
                        "Contrast" -> Triple(contrast, 0.5f..1.6f) { v: Float -> contrast = v }
                        "Saturation" -> Triple(saturation, 0f..2f) { v: Float -> saturation = v }
                        "Warmth" -> Triple(warmth, -1f..1f) { v: Float -> warmth = v }
                        "Sharpness" -> Triple(sharpness, 0f..1f) { v: Float -> sharpness = v }
                        else -> Triple(vignette, 0f..1f) { v: Float -> vignette = v }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeAdjustParam,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(sliderValue * 100).roundToInt()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = config.accentColor
                        )
                    }
                    GlassSlider(
                        value = sliderValue,
                        onValueChange = onValChange,
                        valueRange = sliderRange,
                        contentDescriptionLabel = "Adjust $activeAdjustParam",
                        testTagName = "editor_adjust_slider"
                    )
                }

                1 -> {
                    // CROP, ROTATE, FLIP, STRAIGHTEN DIAL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ratios = listOf(
                            null to "Free",
                            1f to "1:1",
                            4f / 3f to "4:3",
                            16f / 9f to "16:9",
                            9f / 16f to "9:16"
                        )
                        ratios.forEach { (ratioVal, label) ->
                            GlassChip(
                                label = label,
                                selected = cropRatio == ratioVal,
                                onClick = { cropRatio = ratioVal }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GlassButton(
                            onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                            text = tr("Rotate 90°", "ঘোরান"),
                            icon = Icons.AutoMirrored.Rounded.RotateRight
                        )
                        GlassButton(
                            onClick = { flipH = !flipH },
                            text = tr("Flip H", "আনুভূমিক ফ্লিপ"),
                            icon = Icons.Rounded.Flip,
                            selected = flipH
                        )
                        GlassButton(
                            onClick = { flipV = !flipV },
                            text = tr("Flip V", "উল্লম্ব ফ্লিপ"),
                            icon = Icons.Rounded.Flip,
                            selected = flipV
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tr("Straighten Dial", "সোজা করুন"),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "${straightenDegrees.roundToInt()}°",
                            style = MaterialTheme.typography.labelLarge,
                            color = config.accentColor
                        )
                    }
                    GlassSlider(
                        value = straightenDegrees,
                        onValueChange = { straightenDegrees = it },
                        valueRange = -25f..25f,
                        contentDescriptionLabel = "Straighten angle"
                    )
                }

                2 -> {
                    // FILTERS CAROUSEL
                    val filters = listOf(
                        "Original",
                        "Vivid Glass",
                        "Warm Amber",
                        "Cool Prism",
                        "Noir Mono",
                        "Cinema Teal"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(filters) { _, fName ->
                            GlassChip(
                                label = fName,
                                selected = filterName == fName,
                                leadingIcon = Icons.Rounded.FilterVintage,
                                onClick = { filterName = fName },
                                testTagName = "editor_filter_$fName"
                            )
                        }
                    }
                }

                3 -> {
                    // MARKUP & TEXT OVERLAY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = overlayText,
                            onValueChange = { overlayText = it },
                            placeholder = { Text(tr("Add caption overlay...", "ছবির ওপর লেখা যোগ করুন...")) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            onClick = { markupPoints.clear() },
                            text = tr("Clear Ink", "আঁকা মুছুন"),
                            style = GlassButtonStyle.REGULAR
                        )
                    }
                    Text(
                        text = tr("Draw directly on the photo canvas above with your finger.", "উপরের ছবির ওপর আঙুল দিয়ে আঁকুন।"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Editor Tool Mode Segmented Control
            GlassSegmentedControl(
                options = listOf(
                    tr("Adjust", "অ্যাডজাস্ট"),
                    tr("Crop", "ক্রপ"),
                    tr("Filters", "ফিল্টার"),
                    tr("Markup", "মার্কআপ")
                ),
                selectedIndex = editTabIndex,
                onOptionSelected = { editTabIndex = it },
                testTagPrefix = "editor_mode_segment"
            )
        }

        // Save as Copy or Overwrite GlassAlert
        GlassAlert(
            visible = showSaveDialog,
            title = tr("Save Photo Edits", "এডিট সেভ করুন"),
            message = tr(
                "Would you like to save non-destructively to this photo (reversible anytime) or export a separate copy?",
                "আপনি কি এই ছবিতেই নন-ডেস্ট্রাকটিভভাবে সেভ করতে চান নাকি নতুন কপি তৈরি করবেন?"
            ),
            confirmText = tr("Save as Copy", "নতুন কপি"),
            dismissText = tr("Overwrite (Non-Destructive)", "এই ছবিতেই সেভ"),
            onConfirm = {
                viewModel.savePhotoEdits(item.id, currentEditState, saveAsCopy = true)
                onBack()
            },
            onDismiss = {
                viewModel.savePhotoEdits(item.id, currentEditState, saveAsCopy = false)
                showSaveDialog = false
                onBack()
            }
        )
    }
}

private data class Quad(val b: Float, val c: Float, val s: Float, val w: Float)
