package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MotionPhotosOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Slideshow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.AlbumCollection
import com.example.data.GlasslySettingsState
import com.example.data.LibrarySegment
import com.example.data.MediaItem
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
import com.example.ui.glass.GlassChip
import com.example.ui.glass.GlassMenu
import com.example.ui.glass.GlassMenuItem
import com.example.ui.glass.GlassScrubber
import com.example.ui.glass.GlassSegmentedControl
import com.example.ui.glass.GlassSkeletonTile
import com.example.ui.glass.GlassToolbar
import com.example.ui.glass.GlassBackdropState
import com.example.ui.glass.glassBackdropSource
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.LocalGlassConfig
import com.example.ui.theme.tr
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: GlasslyViewModel,
    mediaItems: List<MediaItem>,
    isLoading: Boolean,
    segment: LibrarySegment,
    gridColumns: Int,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    userAlbums: List<AlbumCollection>,
    settings: GlasslySettingsState,
    backdropState: GlassBackdropState,
    hasMediaPermission: Boolean,
    isPartialPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val config = LocalGlassConfig.current

    var showMoreMenu by remember { mutableStateOf(false) }
    var showAddToAlbumSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmAlert by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }
    var hasInitialScrolledToBottom by remember { mutableStateOf(false) }

    // Scroll to latest (bottom) on first load, matching iOS Photos Library behavior
    LaunchedEffect(mediaItems.size, settings.openAtLatest) {
        if (!hasInitialScrolledToBottom && mediaItems.isNotEmpty() && settings.openAtLatest) {
            hasInitialScrolledToBottom = true
            gridState.scrollToItem((mediaItems.size - 1).coerceAtLeast(0))
        }
    }

    // Auto-minimize floating tab bar when scrolling down, expand when scrolling up
    LaunchedEffect(gridState) {
        var prevIndex = gridState.firstVisibleItemIndex
        var prevOffset = gridState.firstVisibleItemScrollOffset
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val down = index > prevIndex || (index == prevIndex && offset > prevOffset + 25)
                val up = index < prevIndex || (index == prevIndex && offset < prevOffset - 25)
                if (down && index > 1) {
                    viewModel.setTabBarMinimized(true)
                } else if (up) {
                    viewModel.setTabBarMinimized(false)
                }
                prevIndex = index
                prevOffset = offset
            }
    }

    // Effective column count based on Years / Months / Days / All Photos segment
    val effectiveColumns = when (segment) {
        LibrarySegment.YEARS -> 1
        LibrarySegment.MONTHS -> 2
        LibrarySegment.DAYS -> 3
        LibrarySegment.ALL -> gridColumns.coerceIn(2, 6)
    }

    // Group items by segment for sticky headers / summary cards
    val groupedSections = remember(mediaItems, segment) {
        when (segment) {
            LibrarySegment.YEARS -> mediaItems.groupBy { it.yearLabel }
            LibrarySegment.MONTHS -> mediaItems.groupBy { it.monthYearLabel }
            LibrarySegment.DAYS -> mediaItems.groupBy { it.dateLabel }
            LibrarySegment.ALL -> mediaItems.groupBy { it.monthYearLabel }
        }
    }

    val currentStickyDateItem by remember(mediaItems) {
        derivedStateOf {
            val idx = gridState.firstVisibleItemIndex.coerceIn(0, (mediaItems.size - 1).coerceAtLeast(0))
            mediaItems.getOrNull(idx)
        }
    }

    val scrollFraction by remember(mediaItems.size) {
        derivedStateOf {
            if (mediaItems.size <= 1) 1f
            else (gridState.firstVisibleItemIndex.toFloat() / (mediaItems.size - 1).toFloat()).coerceIn(0f, 1f)
        }
    }

    var pinchAccumulator by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(segment) {
                if (segment == LibrarySegment.ALL) {
                    detectTransformGestures { _, _, zoom, _ ->
                        pinchAccumulator *= zoom
                        if (pinchAccumulator > 1.28f) {
                            viewModel.setGridColumns(gridColumns - 1)
                            pinchAccumulator = 1f
                        } else if (pinchAccumulator < 0.76f) {
                            viewModel.setGridColumns(gridColumns + 1)
                            pinchAccumulator = 1f
                        }
                    }
                }
            }
    ) {
        // Main Photo Grid recorded into backdropState so all floating glass bars refract the real photos!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading && mediaItems.isEmpty()) {
                // Loading Skeleton Shimmer Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(effectiveColumns),
                    contentPadding = PaddingValues(top = 110.dp, bottom = 180.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(16) {
                        GlassSkeletonTile(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            cornerRadius = 6.dp
                        )
                    }
                }
            } else if (mediaItems.isNotEmpty()) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(effectiveColumns),
                    contentPadding = PaddingValues(
                        top = 112.dp,
                        bottom = 195.dp,
                        start = if (segment == LibrarySegment.ALL) 2.dp else 12.dp,
                        end = if (segment == LibrarySegment.ALL) 2.dp else 12.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(if (segment == LibrarySegment.ALL) 2.dp else 10.dp),
                    verticalArrangement = Arrangement.spacedBy(if (segment == LibrarySegment.ALL) 2.dp else 10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("library_photo_grid")
                ) {

                    if (segment == LibrarySegment.YEARS || segment == LibrarySegment.MONTHS) {
                        // iOS 26 Years / Months large hero cards with date & location overlay
                        groupedSections.entries.forEach { (groupTitle, itemsInGroup) ->
                            val heroItem = itemsInGroup.lastOrNull() ?: itemsInGroup.first()
                            item(key = "hero_${segment.name}_$groupTitle") {
                                SegmentHeroCard(
                                    title = groupTitle,
                                    location = if (settings.showLocationInHeaders) heroItem.locationName else null,
                                    count = itemsInGroup.size,
                                    heroItem = heroItem,
                                    isYearCard = segment == LibrarySegment.YEARS,
                                    onClick = {
                                        if (segment == LibrarySegment.YEARS) {
                                            viewModel.setLibrarySegment(LibrarySegment.MONTHS)
                                        } else {
                                            viewModel.setLibrarySegment(LibrarySegment.DAYS)
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        // DAYS or ALL PHOTOS grid with section headers
                        groupedSections.entries.forEach { (groupTitle, itemsInGroup) ->
                            item(
                                span = { GridItemSpan(maxLineSpan) },
                                key = "header_${segment.name}_$groupTitle"
                            ) {
                                val firstLoc = if (settings.showLocationInHeaders) {
                                    itemsInGroup.firstNotNullOfOrNull { it.locationName }
                                } else null
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = groupTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        if (firstLoc != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.LocationOn,
                                                    contentDescription = null,
                                                    tint = config.accentColor,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = firstLoc,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${itemsInGroup.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            items(
                                items = itemsInGroup,
                                key = { item -> "media_${item.id}" }
                            ) { item ->
                                MediaThumbnailTile(
                                    item = item,
                                    squareAspect = settings.squareAspectGrid && segment == LibrarySegment.ALL,
                                    showBadges = settings.showBadges,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedIds.contains(item.id),
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleMediaSelected(item.id)
                                        } else {
                                            viewModel.navigateToSubScreen(SubScreenRoute.PhotoViewer(item.id))
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleMediaSelected(item.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Floating Liquid Glass Toolbar (Title + item count + Select + More Menu)
        Column(modifier = Modifier.align(Alignment.TopCenter)) {
            val headerTitle = if (isSelectionMode) {
                tr("${selectedIds.size} Selected", "${selectedIds.size}টি নির্বাচিত")
            } else {
                tr("Library", "লাইব্রেরি")
            }
            val headerSub = if (isSelectionMode) {
                tr("Tap photos to select or deselect", "নির্বাচন করতে ছবিতে ট্যাপ করুন")
            } else {
                val photoCount = mediaItems.count { !it.isVideo }
                val videoCount = mediaItems.count { it.isVideo }
                tr("$photoCount Photos, $videoCount Videos", "${photoCount}টি ছবি, ${videoCount}টি ভিডিও")
            }

            GlassToolbar(
                title = headerTitle,
                subtitle = headerSub,
                collapseProgress = 0.2f,
                actions = {
                    GlassButton(
                        onClick = { viewModel.toggleSelectionMode() },
                        text = if (isSelectionMode) tr("Cancel", "বাতিল") else tr("Select", "নির্বাচন"),
                        style = if (isSelectionMode) GlassButtonStyle.TINTED else GlassButtonStyle.REGULAR,
                        testTagName = "library_select_button"
                    )
                    Box {
                        GlassButton(
                            onClick = { showMoreMenu = !showMoreMenu },
                            icon = Icons.Rounded.MoreVert,
                            style = GlassButtonStyle.ICON_CIRCLE,
                            contentDescriptionLabel = "Library options menu",
                            testTagName = "library_more_button"
                        )
                        GlassMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            headerTitle = tr("Library Options", "লাইব্রেরি অপশন"),
                            items = listOf(
                                GlassMenuItem(
                                    id = "aspect_toggle",
                                    title = if (settings.squareAspectGrid) {
                                        tr("Aspect-Ratio Grid", "আসল অনুপাত গ্রিড")
                                    } else {
                                        tr("Square Photo Grid", "বর্গাকার ছবির গ্রিড")
                                    },
                                    icon = Icons.Rounded.AspectRatio,
                                    onClick = {
                                        scope.launch {
                                            viewModel.settingsRepo.update {
                                                it.copy(squareAspectGrid = !it.squareAspectGrid)
                                            }
                                        }
                                    }
                                ),
                                GlassMenuItem(
                                    id = "zoom_in",
                                    title = tr("Larger Thumbnails (${(gridColumns - 1).coerceAtLeast(2)} Cols)", "বড় থাম্বনেইল"),
                                    icon = Icons.Rounded.GridOn,
                                    onClick = { viewModel.setGridColumns(gridColumns - 1) }
                                ),
                                GlassMenuItem(
                                    id = "zoom_out",
                                    title = tr("Denser Grid (${(gridColumns + 1).coerceAtMost(6)} Cols)", "ঘন গ্রিড"),
                                    icon = Icons.Rounded.GridOn,
                                    onClick = { viewModel.setGridColumns(gridColumns + 1) }
                                ),
                                GlassMenuItem(
                                    id = "select_all",
                                    title = tr("Select All Photos", "সব ছবি নির্বাচন করুন"),
                                    icon = Icons.Rounded.SelectAll,
                                    onClick = {
                                        viewModel.toggleSelectionMode(true)
                                        viewModel.selectAllVisible()
                                    }
                                ),
                                GlassMenuItem(
                                    id = "slideshow",
                                    title = tr("Play Slideshow", "স্লাইডশো চালান"),
                                    icon = Icons.Rounded.Slideshow,
                                    onClick = {
                                        viewModel.navigateToSubScreen(
                                            SubScreenRoute.Slideshow(mediaItems.map { it.id })
                                        )
                                    }
                                ),
                                GlassMenuItem(
                                    id = "refresh",
                                    title = tr("Refresh MediaStore", "লাইব্রেরি রিফ্রেশ করুন"),
                                    icon = Icons.Rounded.Refresh,
                                    onClick = { viewModel.refreshLibrary() }
                                )
                            )
                        )
                    }
                }
            )

            // Sticky Floating Date + Location Chip below toolbar when scrolling All Photos
            AnimatedVisibility(
                visible = currentStickyDateItem != null && !isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                currentStickyDateItem?.let { item ->
                    val locSuffix = if (settings.showLocationInHeaders && item.locationName != null) {
                        " • ${item.locationName}"
                    } else ""
                    GlassChip(
                        label = "${item.dateLabel}$locSuffix",
                        selected = false,
                        leadingIcon = if (item.locationName != null) Icons.Rounded.LocationOn else null,
                        onClick = {
                            scope.launch {
                                gridState.animateScrollToItem((mediaItems.size - 1).coerceAtLeast(0))
                            }
                        },
                        testTagName = "library_sticky_date_chip"
                    )
                }
            }

            // Permission Glass Banner floating cleanly outside backdropState when scrolled to top
            AnimatedVisibility(
                visible = (!hasMediaPermission || isPartialPermission) && gridState.firstVisibleItemIndex == 0 && !isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                PermissionGlassBanner(
                    isPartial = isPartialPermission,
                    onRequestPermission = onRequestPermission
                )
            }
        }

        // Empty state floating outside backdropState
        if (!isLoading && mediaItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        tint = config.accentColor,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = tr("No Photos or Videos Yet", "এখনও কোনো ছবি বা ভিডিও নেই"),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    GlassButton(
                        onClick = { viewModel.refreshLibrary() },
                        text = tr("Reload Library", "লাইব্রেরি রিলোড করুন"),
                        icon = Icons.Rounded.Refresh,
                        style = GlassButtonStyle.TINTED
                    )
                }
            }
        }

        // Bottom Floating Controls:
        // - When in Selection Mode: Floating GlassActionBar (Share, Favorite, Add to Album, Hide, Delete)
        // - Otherwise: Fast-Scroll Scrubber + Floating iOS 26 Years / Months / Days / All Photos Segmented Pill
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = if (isSelectionMode) 82.dp else 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = isSelectionMode && selectedIds.isNotEmpty(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                GlassActionBar(
                    actions = listOf(
                        GlassActionItem(
                            id = "share",
                            label = tr("Share", "শেয়ার"),
                            icon = Icons.Rounded.Share,
                            onClick = {
                                val selectedItems = mediaItems.filter { selectedIds.contains(it.id) }
                                viewModel.shareMediaItems(selectedItems)
                            }
                        ),
                        GlassActionItem(
                            id = "favorite",
                            label = tr("Favorite", "প্রিয়"),
                            icon = Icons.Rounded.Favorite,
                            onClick = { viewModel.toggleFavorite(selectedIds) }
                        ),
                        GlassActionItem(
                            id = "album",
                            label = tr("Album", "অ্যালবাম"),
                            icon = Icons.Rounded.CreateNewFolder,
                            onClick = { showAddToAlbumSheet = true }
                        ),
                        GlassActionItem(
                            id = "hide",
                            label = tr("Hide", "লুকান"),
                            icon = Icons.Rounded.VisibilityOff,
                            onClick = { viewModel.toggleHidden(selectedIds, hide = true) }
                        ),
                        GlassActionItem(
                            id = "delete",
                            label = tr("Delete", "মুছুন"),
                            icon = Icons.Rounded.Delete,
                            isDestructive = true,
                            onClick = { showDeleteConfirmAlert = true }
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fast-scroll date scrubber bubble when in All Photos
                    if (segment == LibrarySegment.ALL && mediaItems.size > 6) {
                        GlassScrubber(
                            currentLabel = currentStickyDateItem?.monthYearLabel ?: "2026",
                            fraction = scrollFraction,
                            onFractionChange = { frac ->
                                val targetIdx = (frac * (mediaItems.size - 1)).toInt().coerceIn(0, mediaItems.size - 1)
                                scope.launch {
                                    gridState.scrollToItem(targetIdx)
                                }
                            },
                            testTagName = "library_fast_scroll_scrubber"
                        )
                    }

                    // iOS 26 Years / Months / Days / All Photos Floating Glass Segmented Control
                    val segLabels = LibrarySegment.entries.map {
                        if (settings.language == AppLanguage.BANGLA) it.labelBn else it.labelEn
                    }
                    GlassSegmentedControl(
                        options = segLabels,
                        selectedIndex = segment.ordinal,
                        onOptionSelected = { idx ->
                            viewModel.setLibrarySegment(LibrarySegment.entries[idx])
                        },
                        testTagPrefix = "library_time_segment"
                    )
                }
            }
        }

        // Add to Album Bottom Sheet
        GlassBottomSheet(
            visible = showAddToAlbumSheet,
            title = tr("Add ${selectedIds.size} Item(s) to Album", "অ্যালবামে যোগ করুন"),
            onDismiss = { showAddToAlbumSheet = false }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    placeholder = { Text(tr("New album name...", "নতুন অ্যালবামের নাম...")) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    onClick = {
                        if (newAlbumName.isNotBlank()) {
                            viewModel.createAlbum(newAlbumName, isPinned = true)
                            newAlbumName = ""
                            showAddToAlbumSheet = false
                        }
                    },
                    text = tr("Create", "তৈরি"),
                    icon = Icons.Rounded.Add,
                    style = GlassButtonStyle.TINTED
                )
            }

            userAlbums.forEach { album ->
                FlatGlassRow(
                    title = album.name,
                    subtitle = "${album.itemCount} items",
                    leadingIcon = Icons.Rounded.PhotoLibrary,
                    onClick = {
                        viewModel.addMediaToAlbum(album.id, selectedIds)
                        showAddToAlbumSheet = false
                    }
                )
            }
        }

        // Multi-Select Delete Confirmation Alert
        GlassAlert(
            visible = showDeleteConfirmAlert,
            title = tr("Move ${selectedIds.size} Item(s) to Trash?", "${selectedIds.size}টি ফাইল ট্র্যাশে পাঠাবেন?"),
            message = tr(
                "Items will remain in Recently Deleted for ${settings.trashRetentionDays} days before permanent removal.",
                "ফাইলগুলো ${settings.trashRetentionDays} দিন পর্যন্ত রিসেন্টলি ডিলিটেড ফোল্ডারে থাকবে।"
            ),
            confirmText = tr("Delete", "মুছুন"),
            dismissText = tr("Cancel", "বাতিল"),
            isDestructive = true,
            onConfirm = { viewModel.moveToTrash(selectedIds) },
            onDismiss = { showDeleteConfirmAlert = false }
        )
    }
}

@Composable
private fun PermissionGlassBanner(
    isPartial: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config = LocalGlassConfig.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = DesignTokens.ShapeCard,
                cornerRadius = DesignTokens.RadiusCard
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = if (isPartial) {
                    tr("Partial Photo Access Active", "আংশিক ফটো অ্যাক্সেস চালু আছে")
                } else {
                    tr("Curated Showcase + Device Media", "কিউরেটেড গ্যালারি এবং ডিভাইস মিডিয়া")
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isPartial) {
                    tr("Tap to select more photos or allow full library access.", "আরও ছবি নির্বাচন করতে বা সম্পূর্ণ অ্যাক্সেস দিতে ট্যাপ করুন।")
                } else {
                    tr("Grant Android photo permission to include all photos from your device camera roll.", "আপনার ফোনের সব ছবি দেখতে পারমিশন দিন।")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GlassButton(
            onClick = onRequestPermission,
            text = if (isPartial) tr("Select More", "আরও নিন") else tr("Allow Access", "অনুমতি দিন"),
            style = GlassButtonStyle.TINTED,
            testTagName = "library_permission_banner_button"
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SegmentHeroCard(
    title: String,
    location: String?,
    count: Int,
    heroItem: MediaItem,
    isYearCard: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isYearCard) 220.dp else 185.dp)
            .clip(DesignTokens.ShapeCard)
            .combinedClickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(heroItem.filePath).takeIf { it.exists() } ?: heroItem.uri)
                .crossfade(true)
                .build(),
            contentDescription = "$title ($count items)",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Subtle bottom gradient scrim for WCAG AA text legibility over photo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.68f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = if (isYearCard) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            val sub = if (location != null) "$location • $count items" else "$count items"
            Text(
                text = sub,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.86f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaThumbnailTile(
    item: MediaItem,
    squareAspect: Boolean,
    showBadges: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config = LocalGlassConfig.current
    val aspect = if (squareAspect) 1f else item.itemAspectRatio.coerceIn(0.65f, 1.55f)

    // Non-destructive edit visual preview on thumbnail
    val colorMatrix = remember(item.editState) {
        buildEditColorMatrix(item.editState.brightness, item.editState.contrast, item.editState.saturation, item.editState.warmth)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(4.dp))
            .testTag("media_tile_${item.id}")
            .semantics {
                contentDescription = "${item.title}, ${item.dateLabel}"
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(item.filePath).takeIf { it.exists() } ?: item.uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = if (item.editState.hasEdits) ColorFilter.colorMatrix(colorMatrix) else null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = item.editState.rotationDegrees + item.editState.straightenDegrees
                    scaleX = if (item.editState.flipHorizontal) -1f else 1f
                    scaleY = if (item.editState.flipVertical) -1f else 1f
                }
        )

        // Clear-Glass media badges (never blurring the whole photo, just tiny corner optical badges)
        if (showBadges) {
            // Top-left: LIVE or RAW or GIF or Screenshot badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (item.isLivePhoto) {
                    GlassBadge(
                        text = "LIVE",
                        icon = Icons.Rounded.MotionPhotosOn
                    )
                }
                if (item.isRaw) {
                    GlassBadge(text = "RAW")
                }
                if (item.isGif) {
                    GlassBadge(text = "GIF")
                }
            }

            // Bottom-left: Favorite Heart badge
            if (item.isFavorite) {
                GlassBadge(
                    icon = Icons.Rounded.Favorite,
                    iconTint = Color(0xFFFF2D55),
                    contentDescriptionLabel = "Favorite photo",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(5.dp)
                )
            }

            // Bottom-right: Video duration badge
            if (item.isVideo && item.formattedDuration != null) {
                GlassBadge(
                    text = item.formattedDuration,
                    icon = Icons.Rounded.PlayArrow,
                    contentDescriptionLabel = "Video ${item.formattedDuration}",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                )
            }
        }

        // Selection Mode Checkmark Overlay
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSelected) config.accentColor.copy(alpha = 0.24f)
                        else Color.Black.copy(alpha = 0.12f)
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) config.accentColor else Color.Black.copy(alpha = 0.45f))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

fun buildEditColorMatrix(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    warmth: Float
): ColorMatrix {
    val satMatrix = ColorMatrix().apply { setToSaturation(saturation.coerceIn(0f, 2f)) }
    val c = contrast.coerceIn(0.5f, 1.8f)
    val b = brightness * 70f
    val w = warmth * 25f
    val translate = (1f - c) * 128f + b

    val contrastWarmthMatrix = ColorMatrix(
        floatArrayOf(
            c, 0f, 0f, 0f, translate + w,
            0f, c, 0f, 0f, translate,
            0f, 0f, c, 0f, translate - w,
            0f, 0f, 0f, 1f, 0f
        )
    )
    satMatrix.timesAssign(contrastWarmthMatrix)
    return satMatrix
}
