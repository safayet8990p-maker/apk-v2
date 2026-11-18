package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CameraFront
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.GifBox
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MotionPhotosOn
import androidx.compose.material.icons.rounded.PhotoCameraBack
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RawOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ScreenshotMonitor
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.AlbumCollection
import com.example.data.CollectionTileDensity
import com.example.data.GlasslySettingsState
import com.example.data.MediaItem
import com.example.data.SortOrder
import com.example.ui.GlasslyViewModel
import com.example.ui.SubScreenRoute
import com.example.ui.glass.FlatGlassRow
import com.example.ui.glass.GlassAlert
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.GlassButton
import com.example.ui.glass.GlassButtonStyle
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassChip
import com.example.ui.glass.GlassMenu
import com.example.ui.glass.GlassMenuItem
import com.example.ui.glass.GlassPicker
import com.example.ui.glass.GlassSearchField
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
fun CollectionsScreen(
    viewModel: GlasslyViewModel,
    mediaItems: List<MediaItem>,
    trashedItems: List<MediaItem>,
    hiddenItems: List<MediaItem>,
    userAlbums: List<AlbumCollection>,
    settings: GlasslySettingsState,
    backdropState: GlassBackdropState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val config = LocalGlassConfig.current

    val collapseProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 160f).coerceIn(0f, 1f)
        }
    }

    LaunchedEffect(listState) {
        var prevIndex = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val down = index > prevIndex || (index == prevIndex && offset > prevOffset + 25)
                val up = index < prevIndex || (index == prevIndex && offset < prevOffset - 25)
                if (down && index > 0) viewModel.setTabBarMinimized(true)
                else if (up || index == 0) viewModel.setTabBarMinimized(false)
                prevIndex = index
                prevOffset = offset
            }
    }

    var showCreateAlbumAlert by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }
    var albumToRename by remember { mutableStateOf<AlbumCollection?>(null) }
    var renameAlbumInput by remember { mutableStateOf("") }
    var albumToDelete by remember { mutableStateOf<AlbumCollection?>(null) }
    var showDensityMenu by remember { mutableStateOf(false) }

    val tileWidth: Dp = when (settings.collectionTileDensity) {
        CollectionTileDensity.LARGE -> 178.dp
        CollectionTileDensity.DENSE -> 136.dp
        CollectionTileDensity.TOP_ROW_EMPHASIS -> 162.dp
    }

    val pinnedAlbums = remember(userAlbums) { userAlbums.filter { it.isPinned } }

    Box(modifier = modifier.fillMaxSize()) {
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
                .testTag("collections_lazy_column"),
            contentPadding = PaddingValues(top = 104.dp, bottom = 140.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
                // 1. PINNED COLLECTIONS
                item(key = "section_pinned") {
                    CollectionSectionHeader(
                        title = tr("Pinned Collections", "পিন করা সংগ্রহ"),
                        subtitle = tr("Quick access to your favorite albums", "আপনার প্রিয় অ্যালবামগুলো")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Built-in Pinned Favorites Card
                        item(key = "pinned_favorites") {
                            val favItems = mediaItems.filter { it.isFavorite }
                            AlbumTileCard(
                                title = tr("Favorites", "প্রিয় ছবি"),
                                count = favItems.size,
                                coverItem = favItems.lastOrNull() ?: mediaItems.lastOrNull(),
                                width = if (settings.collectionTileDensity == CollectionTileDensity.TOP_ROW_EMPHASIS) 210.dp else tileWidth,
                                isPinned = true,
                                onClick = {
                                    viewModel.navigateToSubScreen(
                                        SubScreenRoute.AlbumDetail(
                                            titleEn = "Favorites",
                                            titleBn = "প্রিয় ছবি",
                                            smartKey = "favorites"
                                        )
                                    )
                                }
                            )
                        }
                        items(pinnedAlbums, key = { "pinned_${it.id}" }) { album ->
                            AlbumTileWithContextMenu(
                                album = album,
                                width = if (settings.collectionTileDensity == CollectionTileDensity.TOP_ROW_EMPHASIS) 210.dp else tileWidth,
                                onOpen = {
                                    viewModel.navigateToSubScreen(
                                        SubScreenRoute.AlbumDetail(
                                            titleEn = album.name,
                                            titleBn = album.name,
                                            albumId = album.id
                                        )
                                    )
                                },
                                onRename = {
                                    albumToRename = album
                                    renameAlbumInput = album.name
                                },
                                onDelete = { albumToDelete = album },
                                onTogglePin = { viewModel.toggleAlbumPinned(album.id, album.isPinned) },
                                onMoveEarlier = { viewModel.moveAlbumOrder(album.id, -1) },
                                onMoveLater = { viewModel.moveAlbumOrder(album.id, +1) }
                            )
                        }
                    }
                }

                // 2. MY ALBUMS (Create / Rename / Delete / Reorder / Pin)
                item(key = "section_albums") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CollectionSectionHeader(
                            title = tr("My Albums", "আমার অ্যালবাম"),
                            subtitle = tr("Long-press any album to rename, pin, reorder or delete", "রিনেম, পিন বা মুছতে অ্যালবামে লং-প্রেস করুন")
                        )
                        GlassButton(
                            onClick = {
                                newAlbumName = ""
                                showCreateAlbumAlert = true
                            },
                            text = tr("New", "নতুন"),
                            icon = Icons.Rounded.Add,
                            style = GlassButtonStyle.TINTED,
                            testTagName = "collections_new_album_button"
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(userAlbums, key = { "album_${it.id}" }) { album ->
                            AlbumTileWithContextMenu(
                                album = album,
                                width = tileWidth,
                                onOpen = {
                                    viewModel.navigateToSubScreen(
                                        SubScreenRoute.AlbumDetail(
                                            titleEn = album.name,
                                            titleBn = album.name,
                                            albumId = album.id
                                        )
                                    )
                                },
                                onRename = {
                                    albumToRename = album
                                    renameAlbumInput = album.name
                                },
                                onDelete = { albumToDelete = album },
                                onTogglePin = { viewModel.toggleAlbumPinned(album.id, album.isPinned) },
                                onMoveEarlier = { viewModel.moveAlbumOrder(album.id, -1) },
                                onMoveLater = { viewModel.moveAlbumOrder(album.id, +1) }
                            )
                        }
                    }
                }

                // 3. SMART GROUPS (People-less smart collections)
                item(key = "section_smart_groups") {
                    GlassCard(
                        title = tr("Smart Collections", "স্মার্ট সংগ্রহ"),
                        subtitle = tr("Automatically organized by content & camera metadata", "স্বয়ংক্রিয়ভাবে সাজানো সংগ্রহ")
                    ) {
                        val videosCount = mediaItems.count { it.isVideo }
                        val favCount = mediaItems.count { it.isFavorite }
                        val selfieCount = mediaItems.count { it.isSelfie }
                        val screenshotCount = mediaItems.count { it.isScreenshot }

                        FlatGlassRow(
                            title = tr("Recents", "সাম্প্রতিক"),
                            subtitle = "${mediaItems.size} items",
                            leadingIcon = Icons.Rounded.Schedule,
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.AlbumDetail("Recents", "সাম্প্রতিক", smartKey = "recents")
                                )
                            }
                        )
                        FlatGlassRow(
                            title = tr("Favorites", "প্রিয় ছবি"),
                            subtitle = "$favCount items",
                            leadingIcon = Icons.Rounded.Favorite,
                            leadingIconTint = Color(0xFFFF2D55),
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.AlbumDetail("Favorites", "প্রিয় ছবি", smartKey = "favorites")
                                )
                            }
                        )
                        FlatGlassRow(
                            title = tr("Videos", "ভিডিও"),
                            subtitle = "$videosCount items",
                            leadingIcon = Icons.Rounded.PlayCircle,
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.AlbumDetail("Videos", "ভিডিও", smartKey = "videos")
                                )
                            }
                        )
                        FlatGlassRow(
                            title = tr("Selfies & Portraits", "সেলফি ও পোর্ট্রেট"),
                            subtitle = "$selfieCount items",
                            leadingIcon = Icons.Rounded.CameraFront,
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.AlbumDetail("Selfies", "সেলফি", smartKey = "selfies")
                                )
                            }
                        )
                        FlatGlassRow(
                            title = tr("Screenshots", "স্ক্রিনশট"),
                            subtitle = "$screenshotCount items",
                            leadingIcon = Icons.Rounded.ScreenshotMonitor,
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.AlbumDetail("Screenshots", "স্ক্রিনশট", smartKey = "screenshots")
                                )
                            }
                        )
                    }
                }

                // 4. MEDIA TYPES
                item(key = "section_media_types") {
                    GlassCard(
                        title = tr("Media Types", "মিডিয়ার ধরন"),
                        subtitle = tr("Live Photos, RAW captures & animated GIFs", "লাইভ ফটো, RAW এবং GIF")
                    ) {
                        val liveCount = mediaItems.count { it.isLivePhoto }
                        val rawCount = mediaItems.count { it.isRaw }
                        val gifCount = mediaItems.count { it.isGif }

                        FlatGlassRow(
                            title = tr("Live & Motion Photos", "লাইভ ও মোশন ফটো"),
                            subtitle = "$liveCount items",
                            leadingIcon = Icons.Rounded.MotionPhotosOn,
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.AlbumDetail("Live Photos", "লাইভ ফটো", smartKey = "live")
                                )
                            }
                        )
                        FlatGlassRow(
                            title = tr("RAW Photos", "RAW ছবি"),
                            subtitle = "$rawCount items",
                            leadingIcon = Icons.Rounded.RawOn,
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.AlbumDetail("RAW Photos", "RAW ছবি", smartKey = "raw")
                                )
                            }
                        )
                        FlatGlassRow(
                            title = tr("Animated GIFs", "অ্যানিমেটেড GIF"),
                            subtitle = "$gifCount items",
                            leadingIcon = Icons.Rounded.GifBox,
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.AlbumDetail("Animated GIFs", "অ্যানিমেটেড GIF", smartKey = "gif")
                                )
                            }
                        )
                    }
                }

                // 5. UTILITIES (Recently Deleted, Hidden, Duplicates, Large Files)
                item(key = "section_utilities") {
                    GlassCard(
                        title = tr("Utilities & Management", "ইউটিলিটি এবং ব্যবস্থাপনা"),
                        subtitle = tr("Privacy lock, trash recovery & storage cleaners", "প্রাইভেসি লক, ট্র্যাশ এবং স্টোরেজ ক্লিনার")
                    ) {
                        FlatGlassRow(
                            title = tr("Hidden", "লুকানো অ্যালবাম (Hidden)"),
                            subtitle = if (settings.hiddenAlbumLockEnabled) {
                                tr("Locked with PIN / Biometrics • ${hiddenItems.size} items", "পিন / বায়োমেট্রিক লক • ${hiddenItems.size}টি ফাইল")
                            } else {
                                "${hiddenItems.size} items"
                            },
                            leadingIcon = Icons.Rounded.VisibilityOff,
                            onClick = { viewModel.navigateToSubScreen(SubScreenRoute.HiddenAlbum) },
                            trailing = {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Locked",
                                    tint = config.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        FlatGlassRow(
                            title = tr("Recently Deleted", "সম্প্রতি মুছে ফেলা (Trash)"),
                            subtitle = "${trashedItems.size} items • ${settings.trashRetentionDays}-day recovery",
                            leadingIcon = Icons.Rounded.DeleteOutline,
                            onClick = { viewModel.navigateToSubScreen(SubScreenRoute.RecentlyDeleted) }
                        )

                        FlatGlassRow(
                            title = tr("Duplicates", "ডুপ্লিকেট ছবি"),
                            subtitle = tr("Merge or clean duplicate shots", "একই ছবি খুঁজে পরিষ্কার করুন"),
                            leadingIcon = Icons.Rounded.FileCopy,
                            onClick = { viewModel.navigateToSubScreen(SubScreenRoute.DuplicatesCleaner) }
                        )

                        FlatGlassRow(
                            title = tr("Large Files Cleaner", "বড় ফাইল ক্লিনার"),
                            subtitle = tr("Free up space from heavy videos & RAWs", "বড় ভিডিও ও ফাইল পরিষ্কার করুন"),
                            leadingIcon = Icons.Rounded.CleaningServices,
                            onClick = { viewModel.navigateToSubScreen(SubScreenRoute.LargeFilesCleaner) }
                        )
                    }
                }
            }

        // Top Collapsing GlassToolbar
        GlassToolbar(
            title = tr("Collections", "সংগ্রহ"),
            subtitle = tr("${userAlbums.size} Albums • Smart Groups", "${userAlbums.size}টি অ্যালবাম • স্মার্ট গ্রুপ"),
            collapseProgress = collapseProgress,
            actions = {
                GlassButton(
                    onClick = {
                        newAlbumName = ""
                        showCreateAlbumAlert = true
                    },
                    icon = Icons.Rounded.Add,
                    style = GlassButtonStyle.ICON_CIRCLE,
                    contentDescriptionLabel = "Create album",
                    testTagName = "collections_toolbar_add_btn"
                )
                Box {
                    GlassButton(
                        onClick = { showDensityMenu = !showDensityMenu },
                        icon = Icons.Rounded.GridView,
                        style = GlassButtonStyle.ICON_CIRCLE,
                        contentDescriptionLabel = "Tile size options",
                        testTagName = "collections_density_menu_btn"
                    )
                    GlassMenu(
                        expanded = showDensityMenu,
                        onDismissRequest = { showDensityMenu = false },
                        headerTitle = tr("Tile Size", "টাইলের আকার"),
                        items = CollectionTileDensity.entries.map { density ->
                            GlassMenuItem(
                                id = density.name,
                                title = density.labelEn,
                                isSelected = settings.collectionTileDensity == density,
                                onClick = {
                                    scope.launch {
                                        viewModel.settingsRepo.update { it.copy(collectionTileDensity = density) }
                                    }
                                }
                            )
                        }
                    )
                }
            }
        )

        // Create New Album GlassAlert
        GlassAlert(
            visible = showCreateAlbumAlert,
            title = tr("New Album", "নতুন অ্যালবাম"),
            message = tr("Enter a name for this collection.", "এই অ্যালবামের জন্য একটি নাম লিখুন।"),
            confirmText = tr("Create", "তৈরি করুন"),
            dismissText = tr("Cancel", "বাতিল"),
            onConfirm = {
                if (newAlbumName.isNotBlank()) {
                    viewModel.createAlbum(newAlbumName, isPinned = true)
                }
            },
            onDismiss = { showCreateAlbumAlert = false },
            extraContent = {
                OutlinedTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    placeholder = { Text(tr("Summer Trip 2026", "ভ্রমণ ২০২৬")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )

        // Rename Album GlassAlert
        GlassAlert(
            visible = albumToRename != null,
            title = tr("Rename Album", "অ্যালবামের নাম পরিবর্তন করুন"),
            message = tr("Update the title for this collection.", "নতুন নাম লিখুন।"),
            confirmText = tr("Save", "সেভ"),
            dismissText = tr("Cancel", "বাতিল"),
            onConfirm = {
                albumToRename?.let { viewModel.renameAlbum(it.id, renameAlbumInput) }
                albumToRename = null
            },
            onDismiss = { albumToRename = null },
            extraContent = {
                OutlinedTextField(
                    value = renameAlbumInput,
                    onValueChange = { renameAlbumInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )

        // Delete Album GlassAlert
        GlassAlert(
            visible = albumToDelete != null,
            title = tr("Delete Album \"${albumToDelete?.name}\"?", "অ্যালবামটি মুছে ফেলবেন?"),
            message = tr(
                "Deleting this album removes the collection, while keeping your original photos safe in the Library.",
                "অ্যালবামটি মুছে যাবে, কিন্তু আপনার মূল ছবিগুলো লাইব্রেরিতে নিরাপদ থাকবে।"
            ),
            confirmText = tr("Delete Album", "মুছুন"),
            dismissText = tr("Cancel", "বাতিল"),
            isDestructive = true,
            onConfirm = {
                albumToDelete?.let { viewModel.deleteAlbum(it.id) }
                albumToDelete = null
            },
            onDismiss = { albumToDelete = null }
        )
    }
}

@Composable
private fun CollectionSectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumTileWithContextMenu(
    album: AlbumCollection,
    width: Dp,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onMoveEarlier: () -> Unit,
    onMoveLater: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        AlbumTileCard(
            title = album.name,
            count = album.itemCount,
            coverItem = album.coverItem,
            width = width,
            isPinned = album.isPinned,
            onClick = onOpen,
            onLongClick = { menuExpanded = true }
        )
        GlassMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            headerTitle = album.name,
            items = listOf(
                GlassMenuItem(
                    id = "pin",
                    title = if (album.isPinned) tr("Unpin Collection", "আনপিন করুন") else tr("Pin Collection", "পিন করুন"),
                    icon = Icons.Rounded.PushPin,
                    onClick = onTogglePin
                ),
                GlassMenuItem(
                    id = "rename",
                    title = tr("Rename Album", "নাম পরিবর্তন করুন"),
                    icon = Icons.Rounded.DriveFileRenameOutline,
                    onClick = onRename
                ),
                GlassMenuItem(
                    id = "move_left",
                    title = tr("Move Earlier", "আগে নিন"),
                    icon = Icons.Rounded.ArrowUpward,
                    onClick = onMoveEarlier
                ),
                GlassMenuItem(
                    id = "move_right",
                    title = tr("Move Later", "পরে নিন"),
                    icon = Icons.Rounded.ArrowDownward,
                    onClick = onMoveLater
                ),
                GlassMenuItem(
                    id = "delete",
                    title = tr("Delete Album", "অ্যালবাম মুছুন"),
                    icon = Icons.Rounded.Delete,
                    isDestructive = true,
                    onClick = onDelete
                )
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumTileCard(
    title: String,
    count: Int,
    coverItem: MediaItem?,
    width: Dp,
    isPinned: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(width)
            .aspectRatio(0.92f)
            .clip(DesignTokens.ShapeCard)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        if (coverItem != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(coverItem.filePath).takeIf { it.exists() } ?: coverItem.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalGlassConfig.current.accentColor.copy(alpha = 0.25f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        if (isPinned) {
            GlassBadge(
                icon = Icons.Rounded.PushPin,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count items",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.84f)
            )
        }
    }
}

/**
 * Album Detail Screen with header, item count, sort options, and Slideshow trigger.
 */
@Composable
fun AlbumDetailScreen(
    route: SubScreenRoute.AlbumDetail,
    viewModel: GlasslyViewModel,
    allVisibleMedia: List<MediaItem>,
    userAlbums: List<AlbumCollection>,
    settings: GlasslySettingsState,
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var localSort by remember { mutableStateOf(SortOrder.NEWEST_FIRST) }

    val rawItems = remember(route, allVisibleMedia, userAlbums) {
        when {
            route.albumId != null -> {
                val album = userAlbums.find { it.id == route.albumId }
                val ids = album?.itemIds?.toSet() ?: emptySet()
                allVisibleMedia.filter { ids.contains(it.id) }
            }
            route.smartKey == "favorites" -> allVisibleMedia.filter { it.isFavorite }
            route.smartKey == "videos" -> allVisibleMedia.filter { it.isVideo }
            route.smartKey == "selfies" -> allVisibleMedia.filter { it.isSelfie }
            route.smartKey == "screenshots" -> allVisibleMedia.filter { it.isScreenshot }
            route.smartKey == "live" -> allVisibleMedia.filter { it.isLivePhoto }
            route.smartKey == "raw" -> allVisibleMedia.filter { it.isRaw }
            route.smartKey == "gif" -> allVisibleMedia.filter { it.isGif }
            else -> allVisibleMedia
        }
    }

    val sortedItems = remember(rawItems, localSort) {
        when (localSort) {
            SortOrder.NEWEST_FIRST -> rawItems.sortedByDescending { it.dateTakenMillis }
            SortOrder.OLDEST_FIRST -> rawItems.sortedBy { it.dateTakenMillis }
            SortOrder.NAME_ASC -> rawItems.sortedBy { it.title.lowercase() }
            SortOrder.SIZE_DESC -> rawItems.sortedByDescending { it.sizeBytes }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(top = 156.dp, bottom = 120.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedItems, key = { "album_item_${it.id}" }) { item ->
                    MediaThumbnailTile(
                        item = item,
                        squareAspect = true,
                        showBadges = settings.showBadges,
                        isSelectionMode = false,
                        isSelected = false,
                        onClick = { viewModel.navigateToSubScreen(SubScreenRoute.PhotoViewer(item.id)) },
                        onLongClick = { viewModel.toggleFavorite(listOf(item.id)) }
                    )
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.TopCenter)) {
            GlassToolbar(
                title = tr(route.titleEn, route.titleBn),
                subtitle = "${sortedItems.size} items",
                onBackClick = onBack,
                actions = {
                    if (sortedItems.isNotEmpty()) {
                        GlassButton(
                            onClick = {
                                viewModel.navigateToSubScreen(
                                    SubScreenRoute.Slideshow(sortedItems.map { it.id })
                                )
                            },
                            icon = Icons.Rounded.Slideshow,
                            text = tr("Play", "চালান"),
                            style = GlassButtonStyle.TINTED
                        )
                    }
                }
            )

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassPicker(
                    options = SortOrder.entries,
                    selectedOption = localSort,
                    optionLabel = {
                        if (settings.language == AppLanguage.BANGLA) it.labelBn else it.labelEn
                    },
                    onOptionSelected = { localSort = it }
                )
            }
        }
    }
}

/**
 * Search Tab Screen:
 * - Search field in Liquid Glass at the bottom within thumb reach
 * - Search by date, month, year, album, media type, filename, location, and natural phrases ("videos last month")
 * - Suggestions, recent searches with clear option, nice empty state
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: GlasslyViewModel,
    searchQuery: String,
    searchResults: List<MediaItem>,
    recentSearches: List<String>,
    allMedia: List<MediaItem>,
    settings: GlasslySettingsState,
    backdropState: GlassBackdropState,
    modifier: Modifier = Modifier
) {
    val config = LocalGlassConfig.current

    val suggestions = remember {
        listOf(
            "videos last month",
            "Santorini",
            "Favorites",
            "Kyoto",
            "Dolomites",
            "2026",
            "Live",
            "RAW",
            "Selfies",
            "Screenshots"
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            RefractiveColorfulBackdrop(isDark = config.isDarkTheme)
        }

        if (searchQuery.isBlank()) {
                // Discovery view: Suggestions + Recent Searches + Place/Moment previews
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("search_discovery_list"),
                    contentPadding = PaddingValues(top = 104.dp, bottom = 185.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item(key = "search_suggestions") {
                        GlassCard(
                            title = tr("Smart Suggestions", "স্মার্ট সাজেশন"),
                            subtitle = tr("Try natural phrases, places, dates or media types", "স্থান, তারিখ বা প্রাকৃতিক বাক্য দিয়ে খুঁজুন")
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                suggestions.forEach { suggestion ->
                                    GlassChip(
                                        label = suggestion,
                                        selected = false,
                                        leadingIcon = Icons.Rounded.Search,
                                        onClick = {
                                            viewModel.updateSearchQuery(suggestion)
                                            viewModel.commitRecentSearch(suggestion)
                                        },
                                        testTagName = "search_suggestion_$suggestion"
                                    )
                                }
                            }
                        }
                    }

                    if (recentSearches.isNotEmpty()) {
                        item(key = "recent_searches") {
                            GlassCard(
                                title = tr("Recent Searches", "সাম্প্রতিক অনুসন্ধান"),
                                subtitle = tr("Tap to repeat or clear history", "পুনরায় খুঁজতে ট্যাপ করুন")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    GlassButton(
                                        onClick = { viewModel.clearRecentSearches() },
                                        text = tr("Clear History", "ইতিহাস মুছুন"),
                                        icon = Icons.Rounded.ClearAll,
                                        style = GlassButtonStyle.REGULAR,
                                        testTagName = "clear_recent_searches_button"
                                    )
                                }
                                recentSearches.forEach { recent ->
                                    FlatGlassRow(
                                        title = recent,
                                        leadingIcon = Icons.Rounded.History,
                                        onClick = { viewModel.updateSearchQuery(recent) }
                                    )
                                }
                            }
                        }
                    }

                    item(key = "places_highlights") {
                        val places = allMedia.mapNotNull { it.locationName }.distinct()
                        GlassCard(
                            title = tr("Places & Landmarks", "স্থান এবং ভ্রমণ"),
                            subtitle = tr("Indexed from EXIF GPS coordinates", "ছবির অবস্থান থেকে সংগৃহীত")
                        ) {
                            places.take(5).forEach { place ->
                                val count = allMedia.count { it.locationName == place }
                                FlatGlassRow(
                                    title = place,
                                    subtitle = "$count photos & videos",
                                    leadingIcon = Icons.Rounded.LocationOn,
                                    onClick = {
                                        viewModel.updateSearchQuery(place)
                                        viewModel.commitRecentSearch(place)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Live Search Results Grid
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(
                            title = tr("No Matches Found", "কোনো ফলাফল পাওয়া যায়নি"),
                            subtitle = tr(
                                "Nothing matched \"$searchQuery\". Try \"videos last month\", \"Santorini\", or \"2026\".",
                                "\"$searchQuery\" এর সাথে কিছু মেলেনি। অন্য শব্দ দিয়ে চেষ্টা করুন।"
                            )
                        ) {
                            GlassButton(
                                onClick = { viewModel.updateSearchQuery("") },
                                text = tr("Clear Search", "অনুসন্ধান মুছুন"),
                                style = GlassButtonStyle.TINTED
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(top = 108.dp, bottom = 190.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_results_grid")
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = tr(
                                    "${searchResults.size} result(s) for \"$searchQuery\"",
                                    "\"$searchQuery\" এর জন্য ${searchResults.size}টি ফলাফল"
                                ),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                        items(searchResults, key = { "search_item_${it.id}" }) { item ->
                            MediaThumbnailTile(
                                item = item,
                                squareAspect = true,
                                showBadges = settings.showBadges,
                                isSelectionMode = false,
                                isSelected = false,
                                onClick = {
                                    viewModel.commitRecentSearch(searchQuery)
                                    viewModel.navigateToSubScreen(SubScreenRoute.PhotoViewer(item.id))
                                },
                                onLongClick = { viewModel.toggleFavorite(listOf(item.id)) }
                            )
                        }
                    }
                }
            }

        // Top GlassToolbar
        GlassToolbar(
            title = tr("Search", "খুঁজুন"),
            subtitle = tr("Dates, places, albums & natural phrases", "তারিখ, স্থান, অ্যালবাম এবং বাক্য")
        )

        // Thumb-reach Bottom Liquid Glass Search Field positioned just above the floating GlassTabBar!
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .padding( start = 18.dp, end = 18.dp, bottom = 86.dp )
        ) {
            GlassSearchField(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                placeholder = tr(
                    "Search \"videos last month\", places, dates...",
                    "খুঁজুন \"videos last month\", স্থান বা তারিখ..."
                ),
                onSearchSubmit = { viewModel.commitRecentSearch(searchQuery) },
                testTagName = "bottom_thumb_search_field"
            )
        }
    }
}
