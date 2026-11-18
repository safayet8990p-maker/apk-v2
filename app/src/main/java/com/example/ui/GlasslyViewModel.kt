package com.example.ui

import android.app.Application
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlbumCollection
import com.example.data.AlbumEntity
import com.example.data.AlbumMediaCrossRef
import com.example.data.CollectionTileDensity
import com.example.data.GlasslyDatabase
import com.example.data.GlasslySettingsState
import com.example.data.LibrarySegment
import com.example.data.MediaItem
import com.example.data.MediaOverrideEntity
import com.example.data.MediaRepository
import com.example.data.MediaType
import com.example.data.PhotoEditState
import com.example.data.RecentSearchEntity
import com.example.data.SettingsRepository
import com.example.data.SortOrder
import com.example.ui.glass.GlassTabDestination
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.GlassConfig
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.GlassVariant
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

sealed class SubScreenRoute {
    data object None : SubScreenRoute()
    data class PhotoViewer(val initialMediaId: Long) : SubScreenRoute()
    data class PhotoEditor(val mediaId: Long) : SubScreenRoute()
    data class AlbumDetail(
        val titleEn: String,
        val titleBn: String,
        val albumId: Long? = null,
        val smartKey: String? = null
    ) : SubScreenRoute()
    data object RecentlyDeleted : SubScreenRoute()
    data object HiddenAlbum : SubScreenRoute()
    data object DuplicatesCleaner : SubScreenRoute()
    data object LargeFilesCleaner : SubScreenRoute()
    data class Slideshow(val mediaIds: List<Long>) : SubScreenRoute()
    data object GlassGalleryDebug : SubScreenRoute()
}

class GlasslyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GlasslyDatabase.getInstance(application)
    private val dao = database.glasslyDao()
    val settingsRepo = SettingsRepository(application)
    val mediaRepo = MediaRepository(application, viewModelScope)

    val settingsState: StateFlow<GlasslySettingsState> = settingsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlasslySettingsState()
    )

    // Navigation & Chrome state
    private val _currentTab = MutableStateFlow(GlassTabDestination.LIBRARY)
    val currentTab: StateFlow<GlassTabDestination> = _currentTab.asStateFlow()

    private val _subScreenStack = MutableStateFlow<List<SubScreenRoute>>(emptyList())
    val currentSubScreen: StateFlow<SubScreenRoute> = _subScreenStack
        .combine(MutableStateFlow(Unit)) { stack, _ -> stack.lastOrNull() ?: SubScreenRoute.None }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubScreenRoute.None)

    private val _isTabBarMinimized = MutableStateFlow(false)
    val isTabBarMinimized: StateFlow<Boolean> = _isTabBarMinimized.asStateFlow()

    // Library controls
    private val _librarySegment = MutableStateFlow(LibrarySegment.ALL)
    val librarySegment: StateFlow<LibrarySegment> = _librarySegment.asStateFlow()

    private val _gridColumns = MutableStateFlow(4)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedMediaIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMediaIds: StateFlow<Set<Long>> = _selectedMediaIds.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val recentSearches: StateFlow<List<String>> = dao.observeRecentSearches()
        .combine(MutableStateFlow(Unit)) { list, _ -> list.map { it.query } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Santorini", "Videos", "2026", "Favorites"))

    // Hidden album unlock state for current session
    private val _isHiddenAlbumUnlocked = MutableStateFlow(false)
    val isHiddenAlbumUnlocked: StateFlow<Boolean> = _isHiddenAlbumUnlocked.asStateFlow()

    // Toast feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed default user albums if none exist yet
            dao.observeAlbums().collect { existing ->
                if (existing.isEmpty()) {
                    val favTripId = dao.insertAlbum(AlbumEntity(name = "Aegean Adventure", isPinned = true, sortOrderIndex = 0))
                    val archId = dao.insertAlbum(AlbumEntity(name = "Prism & Architecture", isPinned = true, sortOrderIndex = 1))
                    val natureId = dao.insertAlbum(AlbumEntity(name = "Wanderlust Landscapes", isPinned = false, sortOrderIndex = 2))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(favTripId, 1001L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(favTripId, 1004L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(favTripId, 1014L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(archId, 1008L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(archId, 1009L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(archId, 1016L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(natureId, 1002L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(natureId, 1003L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(natureId, 1006L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(natureId, 1010L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(natureId, 1011L))
                    dao.addMediaToAlbum(AlbumMediaCrossRef(natureId, 1015L))
                }
            }
        }
    }

    /**
     * Merged stream of all media items enriched with Room overrides (favorites, hidden, trash, edits).
     */
    val allEnrichedMedia: StateFlow<List<MediaItem>> = combine(
        mediaRepo.rawMediaFlow,
        dao.observeMediaOverrides(),
        settingsState
    ) { rawList, overrides, settings ->
        val overrideMap = overrides.associateBy { it.mediaId }
        val mapped = rawList.map { item ->
            val ov = overrideMap[item.id]
            if (ov == null) {
                item
            } else {
                item.copy(
                    title = ov.customTitle ?: item.title,
                    isFavorite = ov.isFavorite,
                    isHidden = ov.isHidden,
                    isTrashed = ov.isTrashed,
                    trashedAtMillis = ov.trashedAtMillis,
                    editState = PhotoEditState(
                        cropRatio = ov.cropRatio,
                        rotationDegrees = ov.rotationDegrees,
                        straightenDegrees = ov.straightenDegrees,
                        flipHorizontal = ov.flipHorizontal,
                        flipVertical = ov.flipVertical,
                        brightness = ov.brightness,
                        contrast = ov.contrast,
                        saturation = ov.saturation,
                        warmth = ov.warmth,
                        sharpness = ov.sharpness,
                        vignette = ov.vignette,
                        filterName = ov.filterName,
                        overlayText = ov.overlayText,
                        markupStrokesCount = ov.markupStrokesCount
                    )
                )
            }
        }
        when (settings.sortOrder) {
            SortOrder.NEWEST_FIRST -> mapped.sortedBy { it.dateTakenMillis } // iOS Photos puts newest at bottom of Library grid!
            SortOrder.OLDEST_FIRST -> mapped.sortedByDescending { it.dateTakenMillis }
            SortOrder.NAME_ASC -> mapped.sortedBy { it.title.lowercase() }
            SortOrder.SIZE_DESC -> mapped.sortedByDescending { it.sizeBytes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Active Library items (excluding Hidden and Recently Deleted).
     */
    val visibleLibraryMedia: StateFlow<List<MediaItem>> = allEnrichedMedia
        .combine(MutableStateFlow(Unit)) { all, _ ->
            all.filter { !it.isTrashed && !it.isHidden }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedMedia: StateFlow<List<MediaItem>> = allEnrichedMedia
        .combine(MutableStateFlow(Unit)) { all, _ ->
            all.filter { it.isTrashed }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenMedia: StateFlow<List<MediaItem>> = allEnrichedMedia
        .combine(MutableStateFlow(Unit)) { all, _ ->
            all.filter { it.isHidden && !it.isTrashed }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userAlbums: StateFlow<List<AlbumCollection>> = combine(
        dao.observeAlbums(),
        dao.observeAlbumCrossRefs(),
        visibleLibraryMedia
    ) { albums, refs, mediaList ->
        val mediaById = mediaList.associateBy { it.id }
        val refsByAlbum = refs.groupBy { it.albumId }
        albums.map { album ->
            val ids = refsByAlbum[album.albumId]?.map { it.mediaId } ?: emptyList()
            val items = ids.mapNotNull { mediaById[it] }
            AlbumCollection(
                id = album.albumId,
                name = album.name,
                isPinned = album.isPinned,
                isSmartGroup = false,
                sortOrderIndex = album.sortOrderIndex,
                itemIds = items.map { it.id },
                coverItem = items.lastOrNull() ?: mediaList.lastOrNull(),
                itemCount = items.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Natural language + structured search results (supports date, month, year, album,
     * media type, filename, location, and natural phrases like "videos last month" or "favorites 2026").
     */
    val searchResults: StateFlow<List<MediaItem>> = combine(
        _searchQuery,
        visibleLibraryMedia,
        userAlbums
    ) { rawQuery, mediaList, albums ->
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) {
            emptyList()
        } else {
            val isVideosLastMonth = q.contains("video") && q.contains("month")
            val isFavoritesQuery = q.contains("favorite") || q.contains("প্রিয়") || q.contains("heart")
            val matchingAlbumMediaIds = albums
                .filter { it.name.lowercase().contains(q) }
                .flatMap { it.itemIds }
                .toSet()

            mediaList.filter { item ->
                when {
                    isVideosLastMonth -> item.isVideo
                    isFavoritesQuery && q.split(" ").size == 1 -> item.isFavorite
                    else -> {
                        val tokens = q.split(" ").filter { it.isNotBlank() }
                        tokens.all { token ->
                            item.title.lowercase().contains(token) ||
                                (item.locationName?.lowercase()?.contains(token) == true) ||
                                item.dateLabel.lowercase().contains(token) ||
                                item.monthYearLabel.lowercase().contains(token) ||
                                item.yearLabel.lowercase().contains(token) ||
                                item.mediaType.name.lowercase().contains(token) ||
                                (token == "videos" && item.isVideo) ||
                                (token == "video" && item.isVideo) ||
                                (token == "live" && item.isLivePhoto) ||
                                (token == "raw" && item.isRaw) ||
                                (token == "gif" && item.isGif) ||
                                (token == "screenshots" && item.isScreenshot) ||
                                (token == "selfies" && item.isSelfie) ||
                                (token == "favorites" && item.isFavorite) ||
                                matchingAlbumMediaIds.contains(item.id)
                        }
                    }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: GlassTabDestination) {
        _currentTab.value = tab
        _subScreenStack.value = emptyList()
        _isTabBarMinimized.value = false
    }

    fun setTabBarMinimized(minimized: Boolean) {
        _isTabBarMinimized.value = minimized
    }

    fun navigateToSubScreen(route: SubScreenRoute) {
        _subScreenStack.value = _subScreenStack.value + route
    }

    fun popSubScreen(): Boolean {
        val current = _subScreenStack.value
        return if (current.isNotEmpty()) {
            _subScreenStack.value = current.dropLast(1)
            true
        } else {
            false
        }
    }

    fun setLibrarySegment(segment: LibrarySegment) {
        _librarySegment.value = segment
    }

    fun setGridColumns(cols: Int) {
        _gridColumns.value = cols.coerceIn(2, 6)
    }

    fun toggleSelectionMode(force: Boolean? = null) {
        val target = force ?: !_isSelectionMode.value
        _isSelectionMode.value = target
        if (!target) {
            _selectedMediaIds.value = emptySet()
        }
    }

    fun toggleMediaSelected(id: Long) {
        val current = _selectedMediaIds.value.toMutableSet()
        if (!current.add(id)) {
            current.remove(id)
        }
        _selectedMediaIds.value = current
        if (current.isNotEmpty() && !_isSelectionMode.value) {
            _isSelectionMode.value = true
        }
    }

    fun selectRange(ids: List<Long>) {
        _selectedMediaIds.value = _selectedMediaIds.value + ids
        if (_selectedMediaIds.value.isNotEmpty()) {
            _isSelectionMode.value = true
        }
    }

    fun selectAllVisible() {
        _selectedMediaIds.value = visibleLibraryMedia.value.map { it.id }.toSet()
    }

    fun updateSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun commitRecentSearch(q: String) {
        val trimmed = q.trim()
        if (trimmed.isNotEmpty()) {
            viewModelScope.launch {
                dao.upsertRecentSearch(RecentSearchEntity(query = trimmed))
            }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            dao.clearRecentSearches()
            showToast("Cleared recent searches")
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            mediaRepo.reloadMedia()
            showToast("Library refreshed")
        }
    }

    // Media management operations (Favorites, Trash, Restore, Hidden, Rename, Edit)
    fun toggleFavorite(mediaIds: Collection<Long>) {
        viewModelScope.launch {
            val currentMap = allEnrichedMedia.value.associateBy { it.id }
            val anyNotFav = mediaIds.any { currentMap[it]?.isFavorite == false }
            mediaIds.forEach { id ->
                val existing = dao.getMediaOverride(id) ?: MediaOverrideEntity(
                    mediaId = id,
                    isFavorite = currentMap[id]?.isFavorite ?: false
                )
                dao.upsertMediaOverride(existing.copy(isFavorite = anyNotFav))
            }
            showToast(if (anyNotFav) "Added to Favorites" else "Removed from Favorites")
            if (_isSelectionMode.value) toggleSelectionMode(false)
        }
    }

    fun moveToTrash(mediaIds: Collection<Long>) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val currentMap = allEnrichedMedia.value.associateBy { it.id }
            mediaIds.forEach { id ->
                val existing = dao.getMediaOverride(id) ?: MediaOverrideEntity(
                    mediaId = id,
                    isFavorite = currentMap[id]?.isFavorite ?: false
                )
                dao.upsertMediaOverride(
                    existing.copy(
                        isTrashed = true,
                        trashedAtMillis = now
                    )
                )
            }
            showToast("Moved ${mediaIds.size} item(s) to Recently Deleted")
            if (_isSelectionMode.value) toggleSelectionMode(false)
        }
    }

    fun restoreFromTrash(mediaIds: Collection<Long>) {
        viewModelScope.launch {
            mediaIds.forEach { id ->
                val existing = dao.getMediaOverride(id) ?: return@forEach
                dao.upsertMediaOverride(
                    existing.copy(
                        isTrashed = false,
                        trashedAtMillis = null
                    )
                )
            }
            showToast("Restored ${mediaIds.size} item(s)")
        }
    }

    fun deleteForever(mediaIds: Collection<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentMap = allEnrichedMedia.value.associateBy { it.id }
            mediaIds.forEach { id ->
                currentMap[id]?.filePath?.let { path ->
                    runCatching { File(path).delete() }
                }
                val existing = dao.getMediaOverride(id) ?: MediaOverrideEntity(mediaId = id)
                dao.upsertMediaOverride(
                    existing.copy(
                        isTrashed = true,
                        trashedAtMillis = -1L // Permanently purged marker
                    )
                )
            }
            showToast("Permanently deleted ${mediaIds.size} item(s)")
        }
    }

    fun emptyTrash() {
        val ids = trashedMedia.value.map { it.id }
        if (ids.isNotEmpty()) {
            deleteForever(ids)
        }
    }

    fun toggleHidden(mediaIds: Collection<Long>, hide: Boolean) {
        viewModelScope.launch {
            val currentMap = allEnrichedMedia.value.associateBy { it.id }
            mediaIds.forEach { id ->
                val existing = dao.getMediaOverride(id) ?: MediaOverrideEntity(
                    mediaId = id,
                    isFavorite = currentMap[id]?.isFavorite ?: false
                )
                dao.upsertMediaOverride(existing.copy(isHidden = hide))
            }
            showToast(if (hide) "Moved to Hidden album" else "Unhidden to Library")
            if (_isSelectionMode.value) toggleSelectionMode(false)
        }
    }

    fun renameMediaItem(mediaId: Long, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            val current = allEnrichedMedia.value.find { it.id == mediaId }
            val existing = dao.getMediaOverride(mediaId) ?: MediaOverrideEntity(
                mediaId = mediaId,
                isFavorite = current?.isFavorite ?: false
            )
            dao.upsertMediaOverride(existing.copy(customTitle = newTitle.trim()))
            showToast("Renamed to ${newTitle.trim()}")
        }
    }

    fun savePhotoEdits(mediaId: Long, editState: PhotoEditState, saveAsCopy: Boolean) {
        viewModelScope.launch {
            val current = allEnrichedMedia.value.find { it.id == mediaId } ?: return@launch
            if (saveAsCopy) {
                val copy = mediaRepo.saveEditedCopy(current, editState)
                dao.upsertMediaOverride(
                    MediaOverrideEntity(
                        mediaId = copy.id,
                        isFavorite = current.isFavorite,
                        cropRatio = editState.cropRatio,
                        rotationDegrees = editState.rotationDegrees,
                        straightenDegrees = editState.straightenDegrees,
                        flipHorizontal = editState.flipHorizontal,
                        flipVertical = editState.flipVertical,
                        brightness = editState.brightness,
                        contrast = editState.contrast,
                        saturation = editState.saturation,
                        warmth = editState.warmth,
                        sharpness = editState.sharpness,
                        vignette = editState.vignette,
                        filterName = editState.filterName,
                        overlayText = editState.overlayText,
                        markupStrokesCount = editState.strokePoints.size
                    )
                )
                showToast("Saved edited copy")
            } else {
                val existing = dao.getMediaOverride(mediaId) ?: MediaOverrideEntity(
                    mediaId = mediaId,
                    isFavorite = current.isFavorite
                )
                dao.upsertMediaOverride(
                    existing.copy(
                        cropRatio = editState.cropRatio,
                        rotationDegrees = editState.rotationDegrees,
                        straightenDegrees = editState.straightenDegrees,
                        flipHorizontal = editState.flipHorizontal,
                        flipVertical = editState.flipVertical,
                        brightness = editState.brightness,
                        contrast = editState.contrast,
                        saturation = editState.saturation,
                        warmth = editState.warmth,
                        sharpness = editState.sharpness,
                        vignette = editState.vignette,
                        filterName = editState.filterName,
                        overlayText = editState.overlayText,
                        markupStrokesCount = editState.strokePoints.size
                    )
                )
                showToast("Saved non-destructive edits")
            }
        }
    }

    fun revertPhotoEdits(mediaId: Long) {
        viewModelScope.launch {
            val existing = dao.getMediaOverride(mediaId) ?: return@launch
            dao.upsertMediaOverride(
                existing.copy(
                    cropRatio = null,
                    rotationDegrees = 0f,
                    straightenDegrees = 0f,
                    flipHorizontal = false,
                    flipVertical = false,
                    brightness = 0f,
                    contrast = 1f,
                    saturation = 1f,
                    warmth = 0f,
                    sharpness = 0f,
                    vignette = 0f,
                    filterName = "Original",
                    overlayText = "",
                    markupStrokesCount = 0
                )
            )
            showToast("Reverted to original photo")
        }
    }

    // Album operations
    fun createAlbum(name: String, isPinned: Boolean = false) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextOrder = userAlbums.value.size
            val id = dao.insertAlbum(
                AlbumEntity(
                    name = name.trim(),
                    isPinned = isPinned,
                    sortOrderIndex = nextOrder
                )
            )
            // If items are selected, add them immediately
            _selectedMediaIds.value.forEach { mediaId ->
                dao.addMediaToAlbum(AlbumMediaCrossRef(id, mediaId))
            }
            showToast("Created album \"${name.trim()}\"")
        }
    }

    fun renameAlbum(albumId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            dao.renameAlbum(albumId, newName.trim())
            showToast("Renamed album")
        }
    }

    fun deleteAlbum(albumId: Long) {
        viewModelScope.launch {
            dao.deleteAlbum(albumId)
            showToast("Deleted album")
        }
    }

    fun toggleAlbumPinned(albumId: Long, currentlyPinned: Boolean) {
        viewModelScope.launch {
            dao.setAlbumPinned(albumId, !currentlyPinned)
            showToast(if (!currentlyPinned) "Pinned collection" else "Unpinned collection")
        }
    }

    fun moveAlbumOrder(albumId: Long, delta: Int) {
        viewModelScope.launch {
            val currentList = userAlbums.value.toMutableList()
            val idx = currentList.indexOfFirst { it.id == albumId }
            if (idx == -1) return@launch
            val targetIdx = (idx + delta).coerceIn(0, currentList.lastIndex)
            if (idx == targetIdx) return@launch
            val item = currentList.removeAt(idx)
            currentList.add(targetIdx, item)
            currentList.forEachIndexed { index, album ->
                dao.updateAlbumOrder(album.id, index)
            }
        }
    }

    fun addMediaToAlbum(albumId: Long, mediaIds: Collection<Long>) {
        viewModelScope.launch {
            mediaIds.forEach { mediaId ->
                dao.addMediaToAlbum(AlbumMediaCrossRef(albumId, mediaId))
            }
            showToast("Added ${mediaIds.size} item(s) to album")
            if (_isSelectionMode.value) toggleSelectionMode(false)
        }
    }

    fun unlockHiddenAlbumWithPin(enteredPin: String): Boolean {
        val expected = settingsState.value.hiddenAlbumPin
        return if (enteredPin == expected) {
            _isHiddenAlbumUnlocked.value = true
            showToast("Hidden album unlocked")
            true
        } else {
            false
        }
    }

    fun unlockHiddenAlbumBiometricSuccess() {
        _isHiddenAlbumUnlocked.value = true
        showToast("Unlocked with Biometrics")
    }

    fun lockHiddenAlbum() {
        _isHiddenAlbumUnlocked.value = false
    }

    fun setAsWallpaper(item: MediaItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = runCatching {
                val file = File(item.filePath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp != null) {
                        WallpaperManager.getInstance(getApplication()).setBitmap(bmp)
                        true
                    } else false
                } else false
            }.getOrDefault(false)
            showToast(if (success) "Wallpaper updated!" else "Prepared wallpaper preview")
        }
    }

    fun shareMediaItems(items: List<MediaItem>) {
        if (items.isEmpty()) return
        runCatching {
            val context = getApplication<Application>()
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = items.first().mimeType
                putExtra(Intent.EXTRA_TEXT, "Shared from Glassly: ${items.joinToString { it.title }}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Share via Glassly").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }.onFailure {
            showToast("Sharing ${items.size} item(s)")
        }
    }

    fun clearImageCache() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getApplication<Application>().cacheDir.deleteRecursively()
            }
            showToast("Thumbnail & shader cache cleared")
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRepo.unregisterObserver()
    }
}
