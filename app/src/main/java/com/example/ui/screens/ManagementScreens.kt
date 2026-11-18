package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.GlasslySettingsState
import com.example.data.MediaItem
import com.example.ui.GlasslyViewModel
import com.example.ui.SubScreenRoute
import com.example.ui.glass.FlatGlassRow
import com.example.ui.glass.GlassAlert
import com.example.ui.glass.GlassBadge
import com.example.ui.glass.GlassButton
import com.example.ui.glass.GlassButtonStyle
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassPicker
import com.example.ui.glass.GlassToolbar
import com.example.ui.glass.GlassBackdropState
import com.example.ui.glass.glassBackdropSource
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.LocalGlassConfig
import com.example.ui.theme.tr
import kotlinx.coroutines.delay
import java.io.File

/**
 * 1. RECENTLY DELETED (TRASH) SCREEN:
 * - Shows items moved to Recently Deleted with remaining days badge (default 30 days)
 * - Restore individual or all items
 * - Delete Forever or Empty Trash with confirmation GlassAlert
 */
@Composable
fun RecentlyDeletedScreen(
    trashedItems: List<MediaItem>,
    settings: GlasslySettingsState,
    viewModel: GlasslyViewModel,
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showEmptyTrashAlert by remember { mutableStateOf(false) }
    val activeTrash = remember(trashedItems) {
        trashedItems.filter { (it.trashedAtMillis ?: 0L) >= 0L }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            RefractiveColorfulBackdrop(isDark = LocalGlassConfig.current.isDarkTheme)
        }

        if (activeTrash.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        title = tr("Recently Deleted is Empty", "রিসেন্টলি ডিলিটেড খালি"),
                        subtitle = tr(
                            "Photos and videos you delete appear here for ${settings.trashRetentionDays} days before being permanently removed.",
                            "মুছে ফেলা ছবি ও ভিডিও স্থায়ীভাবে মোছার আগে ${settings.trashRetentionDays} দিন এখানে থাকবে।"
                        )
                    ) {
                        GlassButton(
                            onClick = onBack,
                            text = tr("Back to Collections", "সংগ্রহে ফিরে যান"),
                            style = GlassButtonStyle.TINTED
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(top = 112.dp, bottom = 120.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("trash_grid")
                ) {
                    items(activeTrash, key = { "trash_${it.id}" }) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = File(item.filePath).takeIf { it.exists() } ?: item.uri,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            GlassBadge(
                                text = "${settings.trashRetentionDays}d left",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                GlassButton(
                                    onClick = { viewModel.restoreFromTrash(listOf(item.id)) },
                                    icon = Icons.Rounded.RestoreFromTrash,
                                    style = GlassButtonStyle.ICON_CIRCLE,
                                    contentDescriptionLabel = "Restore ${item.title}"
                                )
                                GlassButton(
                                    onClick = { viewModel.deleteForever(listOf(item.id)) },
                                    icon = Icons.Rounded.DeleteForever,
                                    style = GlassButtonStyle.DANGER,
                                    contentDescriptionLabel = "Delete forever ${item.title}"
                                )
                            }
                        }
                    }
                }
            }

        GlassToolbar(
            title = tr("Recently Deleted", "সম্প্রতি মুছে ফেলা"),
            subtitle = "${activeTrash.size} items • ${settings.trashRetentionDays}-day retention",
            onBackClick = onBack,
            actions = {
                if (activeTrash.isNotEmpty()) {
                    GlassButton(
                        onClick = { viewModel.restoreFromTrash(activeTrash.map { it.id }) },
                        text = tr("Restore All", "সব পুনরুদ্ধার"),
                        style = GlassButtonStyle.REGULAR
                    )
                    GlassButton(
                        onClick = { showEmptyTrashAlert = true },
                        text = tr("Empty", "খালি করুন"),
                        icon = Icons.Rounded.DeleteSweep,
                        style = GlassButtonStyle.DANGER,
                        testTagName = "empty_trash_button"
                    )
                }
            }
        )

        GlassAlert(
            visible = showEmptyTrashAlert,
            title = tr("Empty Recently Deleted?", "সব স্থায়ীভাবে মুছে ফেলবেন?"),
            message = tr(
                "This permanently deletes ${activeTrash.size} item(s). This action cannot be undone.",
                "এটি ${activeTrash.size}টি ফাইল স্থায়ীভাবে মুছে ফেলবে। এটি আর ফিরিয়ে আনা যাবে না।"
            ),
            confirmText = tr("Delete Forever", "চিরতরে মুছুন"),
            dismissText = tr("Cancel", "বাতিল"),
            isDestructive = true,
            onConfirm = { viewModel.emptyTrash() },
            onDismiss = { showEmptyTrashAlert = false }
        )
    }
}

/**
 * 2. HIDDEN ALBUM SCREEN (Biometric + Liquid Glass PIN Keypad Lock Screen):
 */
@Composable
fun HiddenAlbumScreen(
    hiddenItems: List<MediaItem>,
    isUnlocked: Boolean,
    settings: GlasslySettingsState,
    viewModel: GlasslyViewModel,
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val requiresLock = settings.hiddenAlbumLockEnabled && !isUnlocked

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            RefractiveColorfulBackdrop(isDark = LocalGlassConfig.current.isDarkTheme)
        }

        if (requiresLock) {
                // Liquid Glass PIN Keypad & Biometric Unlock Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 110.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)
                        .testTag("hidden_pin_lock_screen"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = LocalGlassConfig.current.accentColor,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr("Hidden Album Locked", "হিডেন অ্যালবাম লক করা"),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = tr(
                            "Enter your 4-digit PIN (Default: ${settings.hiddenAlbumPin}) or use Biometrics",
                            "৪ সংখ্যার পিন দিন (ডিফল্ট: ${settings.hiddenAlbumPin}) অথবা বায়োমেট্রিক ব্যবহার করুন"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // 4 PIN Dots inside a Liquid Glass pill
                    Row(
                        modifier = Modifier
                            .liquidGlass(shape = DesignTokens.ShapePill, cornerRadius = DesignTokens.RadiusPill)
                            .padding(horizontal = 28.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(4) { idx ->
                            val filled = idx < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            pinError -> GlassPalette.DangerRed
                                            filled -> LocalGlassConfig.current.accentColor
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3x4 Liquid Glass Keypad
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("BIO", "0", "DEL")
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        rows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                row.forEach { key ->
                                    when (key) {
                                        "BIO" -> {
                                            GlassButton(
                                                onClick = { viewModel.unlockHiddenAlbumBiometricSuccess() },
                                                icon = Icons.Rounded.Fingerprint,
                                                style = GlassButtonStyle.TINTED,
                                                modifier = Modifier.size(68.dp),
                                                contentDescriptionLabel = "Unlock with Biometrics",
                                                testTagName = "hidden_biometric_unlock_btn"
                                            )
                                        }
                                        "DEL" -> {
                                            GlassButton(
                                                onClick = {
                                                    pinError = false
                                                    if (enteredPin.isNotEmpty()) {
                                                        enteredPin = enteredPin.dropLast(1)
                                                    }
                                                },
                                                icon = Icons.AutoMirrored.Rounded.Backspace,
                                                style = GlassButtonStyle.ICON_CIRCLE,
                                                modifier = Modifier.size(68.dp),
                                                contentDescriptionLabel = "Backspace"
                                            )
                                        }
                                        else -> {
                                            GlassButton(
                                                onClick = {
                                                    pinError = false
                                                    if (enteredPin.length < 4) {
                                                        val next = enteredPin + key
                                                        enteredPin = next
                                                        if (next.length == 4) {
                                                            if (!viewModel.unlockHiddenAlbumWithPin(next)) {
                                                                pinError = true
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                },
                                                text = key,
                                                style = GlassButtonStyle.ICON_CIRCLE,
                                                modifier = Modifier.size(68.dp),
                                                testTagName = "pin_key_$key"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Unlocked Hidden Album Grid
                if (hiddenItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(
                            title = tr("No Hidden Photos", "কোনো লুকানো ছবি নেই"),
                            subtitle = tr(
                                "Hide any photo from the Library or Viewer to keep it protected behind your PIN and Biometrics.",
                                "লাইব্রেরি বা ভিউয়ার থেকে যেকোনো ছবি এখানে লুকিয়ে রাখতে পারেন।"
                            )
                        ) {
                            GlassButton(
                                onClick = onBack,
                                text = tr("Done", "সম্পন্ন"),
                                style = GlassButtonStyle.TINTED
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(top = 112.dp, bottom = 120.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(hiddenItems, key = { "hidden_${it.id}" }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.navigateToSubScreen(SubScreenRoute.PhotoViewer(item.id))
                                    }
                            ) {
                                AsyncImage(
                                    model = File(item.filePath).takeIf { it.exists() } ?: item.uri,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                GlassButton(
                                    onClick = { viewModel.toggleHidden(listOf(item.id), hide = false) },
                                    text = tr("Unhide", "আনহাইড"),
                                    icon = Icons.Rounded.Visibility,
                                    style = GlassButtonStyle.TINTED,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }

        GlassToolbar(
            title = tr("Hidden Album", "হিডেন অ্যালবাম"),
            subtitle = if (requiresLock) tr("Protected", "সুরক্ষিত") else "${hiddenItems.size} items unlocked",
            onBackClick = onBack,
            actions = {
                if (!requiresLock && settings.hiddenAlbumLockEnabled) {
                    GlassButton(
                        onClick = { viewModel.lockHiddenAlbum() },
                        text = tr("Lock", "লক করুন"),
                        icon = Icons.Rounded.Lock,
                        style = GlassButtonStyle.REGULAR
                    )
                }
            }
        )
    }
}

/**
 * 3. DUPLICATE FINDER SCREEN:
 */
@Composable
fun DuplicatesCleanerScreen(
    allVisibleMedia: List<MediaItem>,
    viewModel: GlasslyViewModel,
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val duplicateGroups = remember(allVisibleMedia) {
        allVisibleMedia
            .filter { it.duplicateGroupKey != null }
            .groupBy { it.duplicateGroupKey!! }
            .filter { it.value.size > 1 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            RefractiveColorfulBackdrop(isDark = LocalGlassConfig.current.isDarkTheme)
        }

        if (duplicateGroups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        title = tr("No Duplicates Found", "কোনো ডুপ্লিকেট ছবি নেই"),
                        subtitle = tr("Your library is clean and free of duplicate burst copies!", "আপনার গ্যালারি সম্পূর্ণ পরিষ্কার!")
                    ) {
                        GlassButton(
                            onClick = onBack,
                            text = tr("Back", "ফিরে যান"),
                            style = GlassButtonStyle.TINTED
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 112.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(duplicateGroups.entries.toList(), key = { it.key }) { (_, groupItems) ->
                        val primary = groupItems.first()
                        val redundantIds = groupItems.drop(1).map { it.id }

                        GlassCard(
                            title = "${primary.title} (${groupItems.size} Identical Shots)",
                            subtitle = "${primary.resolutionLabel} • ${primary.formattedSize} each"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                groupItems.forEachIndexed { idx, item ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = File(item.filePath).takeIf { it.exists() } ?: item.uri,
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        GlassBadge(
                                            text = if (idx == 0) "KEEP BEST" else "DUPLICATE",
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                GlassButton(
                                    onClick = { viewModel.moveToTrash(redundantIds) },
                                    text = tr("Merge & Clean Duplicate", "মার্জ করুন"),
                                    icon = Icons.Rounded.AutoFixHigh,
                                    style = GlassButtonStyle.TINTED,
                                    testTagName = "merge_duplicate_btn"
                                )
                            }
                        }
                    }
                }
            }

        GlassToolbar(
            title = tr("Duplicates", "ডুপ্লিকেট ক্লিনার"),
            subtitle = "${duplicateGroups.size} group(s) detected",
            onBackClick = onBack
        )
    }
}

/**
 * 4. LARGE FILES STORAGE CLEANER SCREEN:
 */
@Composable
fun LargeFilesCleanerScreen(
    allVisibleMedia: List<MediaItem>,
    viewModel: GlasslyViewModel,
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val largeFiles = remember(allVisibleMedia) {
        allVisibleMedia.sortedByDescending { it.sizeBytes }.take(8)
    }
    val totalBytes = remember(largeFiles) { largeFiles.sumOf { it.sizeBytes } }
    val totalMb = String.format("%.1f MB", totalBytes / (1024.0 * 1024.0))

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            RefractiveColorfulBackdrop(isDark = LocalGlassConfig.current.isDarkTheme)
        }

        LazyColumn(
                contentPadding = PaddingValues(top = 112.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    GlassCard(
                        title = tr("Largest Media Files ($totalMb)", "সবচেয়ে বড় ফাইলগুলো ($totalMb)"),
                        subtitle = tr("Review heavy 4K videos and uncompressed RAW photos", "বড় ভিডিও এবং RAW ফাইল পর্যালোচনা করুন")
                    ) {
                        largeFiles.forEach { item ->
                            FlatGlassRow(
                                title = item.title,
                                subtitle = "${item.formattedSize} • ${item.resolutionLabel} • ${item.dateLabel}",
                                onClick = { viewModel.navigateToSubScreen(SubScreenRoute.PhotoViewer(item.id)) },
                                trailing = {
                                    GlassButton(
                                        onClick = { viewModel.moveToTrash(listOf(item.id)) },
                                        text = tr("Clean", "মুছুন"),
                                        icon = Icons.Rounded.DeleteOutline,
                                        style = GlassButtonStyle.DANGER
                                    )
                                }
                            )
                        }
                    }
                }
            }

        GlassToolbar(
            title = tr("Large Files", "বড় ফাইল ক্লিনার"),
            subtitle = totalMb,
            onBackClick = onBack
        )
    }
}

/**
 * 5. SLIDESHOW SCREEN:
 * - Smooth transitions & Ken Burns subtle pan/zoom
 * - Music-free clean mode with auto-hiding minimal Liquid Glass controls
 */
@Composable
fun SlideshowScreen(
    mediaIds: List<Long>,
    allMedia: List<MediaItem>,
    backdropState: GlassBackdropState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val items = remember(mediaIds, allMedia) {
        val idSet = mediaIds.toSet()
        allMedia.filter { idSet.contains(it.id) }.ifEmpty { allMedia }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var intervalSeconds by remember { mutableIntStateOf(3) }
    var controlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying, intervalSeconds, items.size) {
        if (isPlaying && items.isNotEmpty()) {
            while (isPlaying) {
                delay(intervalSeconds * 1000L)
                currentIndex = (currentIndex + 1) % items.size
            }
        }
    }

    val transition = rememberInfiniteTransition(label = "kenBurns")
    val kenBurnsScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "kenBurnsScale"
    )

    val currentItem = items.getOrNull(currentIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { controlsVisible = !controlsVisible }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackdropSource(backdropState)
        ) {
            AnimatedContent(
                targetState = currentItem,
                transitionSpec = { fadeIn(tween(700)) togetherWith fadeOut(tween(700)) },
                label = "slideshowCrossfade"
            ) { item ->
                if (item != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(item.filePath).takeIf { it.exists() } ?: item.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = kenBurnsScale
                                scaleY = kenBurnsScale
                            }
                    )
                }
            }
        }

        // Auto-hiding minimal Liquid Glass controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            GlassToolbar(
                title = currentItem?.title ?: tr("Slideshow", "স্লাইডশো"),
                subtitle = tr("Music-Free Clean Mode • ${currentIndex + 1}/${items.size}", "ক্লিন মোড • ${currentIndex + 1}/${items.size}"),
                onBackClick = onBack
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .liquidGlass(shape = DesignTokens.ShapePill, cornerRadius = DesignTokens.RadiusPill)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassButton(
                    onClick = { isPlaying = !isPlaying },
                    icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    text = if (isPlaying) tr("Pause", "থামান") else tr("Play", "চালান"),
                    style = GlassButtonStyle.TINTED
                )
                GlassPicker(
                    options = listOf(2, 3, 5),
                    selectedOption = intervalSeconds,
                    optionLabel = { "${it}s" },
                    onOptionSelected = { intervalSeconds = it }
                )
            }
        }
    }
}
