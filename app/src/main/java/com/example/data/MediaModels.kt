package com.example.data

import androidx.compose.ui.graphics.Color

enum class MediaType {
    PHOTO,
    VIDEO,
    LIVE_PHOTO,
    GIF,
    RAW,
    SCREENSHOT,
    SELFIE
}

enum class LibrarySegment(val labelEn: String, val labelBn: String) {
    YEARS("Years", "বছর"),
    MONTHS("Months", "মাস"),
    DAYS("Days", "দিন"),
    ALL("All Photos", "সব ছবি")
}

enum class SortOrder(val labelEn: String, val labelBn: String) {
    NEWEST_FIRST("Newest First", "নতুন আগে"),
    OLDEST_FIRST("Oldest First", "পুরানো আগে"),
    NAME_ASC("Name (A–Z)", "নাম (A–Z)"),
    SIZE_DESC("Largest Size", "বড় ফাইল")
}

enum class CollectionTileDensity(val labelEn: String, val labelBn: String) {
    LARGE("Large Tiles", "বড় টাইলস"),
    DENSE("Dense Grid", "ঘন গ্রিড"),
    TOP_ROW_EMPHASIS("Top-Row Emphasis", "শীর্ষ সারি প্রাধান্য")
}

/**
 * Non-destructive photo edit state stored in Room so the user can always Revert
 * to original, Compare by holding, Save as Copy, or Overwrite.
 */
data class PhotoEditState(
    val cropRatio: Float? = null,        // null = Free, 1f = 1:1, 1.333f = 4:3, 1.777f = 16:9, 0.8f = 4:5
    val rotationDegrees: Float = 0f,     // 0, 90, 180, 270
    val straightenDegrees: Float = 0f,   // -45f..+45f dial
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val brightness: Float = 0f,          // -1f..+1f
    val contrast: Float = 1f,            // 0.5f..1.5f
    val saturation: Float = 1f,          // 0f..2f
    val warmth: Float = 0f,              // -1f..+1f
    val sharpness: Float = 0f,           // 0f..1f
    val vignette: Float = 0f,            // 0f..1f
    val filterName: String = "Original", // Original, Vivid, WarmGlass, Noir, CyanPrism, Sunset
    val overlayText: String = "",
    val markupStrokesCount: Int = 0,
    val strokePoints: List<Pair<Float, Float>> = emptyList()
) {
    val isModified: Boolean
        get() = cropRatio != null ||
            rotationDegrees != 0f ||
            straightenDegrees != 0f ||
            flipHorizontal ||
            flipVertical ||
            brightness != 0f ||
            contrast != 1f ||
            saturation != 1f ||
            warmth != 0f ||
            sharpness != 0f ||
            vignette != 0f ||
            filterName != "Original" ||
            overlayText.isNotBlank() ||
            strokePoints.isNotEmpty()

    val hasEdits: Boolean get() = isModified
}

data class MediaItem(
    val id: Long,
    val uriString: String,
    val title: String,
    val dateTakenMillis: Long,
    val dateLabel: String,
    val monthYearLabel: String,
    val yearLabel: String,
    val locationName: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val width: Int = 1920,
    val height: Int = 1440,
    val sizeBytes: Long = 3_450_000L,
    val mimeType: String = "image/jpeg",
    val durationMillis: Long = 0L,
    val mediaType: MediaType = MediaType.PHOTO,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAtMillis: Long? = null,
    val cameraModel: String = "Glassly Optics 24mm f/1.6",
    val iso: Int = 64,
    val exposureTime: String = "1/420s",
    val focalLength: String = "24mm",
    val filePath: String = "/storage/emulated/0/DCIM/Glassly/IMG_0001.jpg",
    val dominantColors: List<Color> = listOf(Color(0xFF0284C7), Color(0xFF6366F1)),
    val editState: PhotoEditState = PhotoEditState(),
    val duplicateGroupKey: String? = null
) {
    val uri: String get() = uriString
    val itemAspectRatio: Float get() = if (height > 0) width.toFloat() / height.toFloat() else 1f
    val isVideo: Boolean get() = mediaType == MediaType.VIDEO
    val isLivePhoto: Boolean get() = mediaType == MediaType.LIVE_PHOTO
    val isScreenshot: Boolean get() = mediaType == MediaType.SCREENSHOT
    val isSelfie: Boolean get() = mediaType == MediaType.SELFIE
    val isRaw: Boolean get() = mediaType == MediaType.RAW
    val isGif: Boolean get() = mediaType == MediaType.GIF
    val isLargeFile: Boolean get() = sizeBytes >= 15_000_000L

    val formattedDuration: String
        get() {
            if (durationMillis <= 0L) return ""
            val totalSeconds = (durationMillis / 1000L).toInt()
            val mins = totalSeconds / 60
            val secs = totalSeconds % 60
            return "%d:%02d".format(mins, secs)
        }

    val formattedSize: String
        get() {
            val mb = sizeBytes / (1024f * 1024f)
            return "%.1f MB".format(mb)
        }

    val resolutionLabel: String
        get() = "${width} × ${height}"

    val megapixelsLabel: String
        get() = "%.1f MP".format((width * height) / 1_000_000f)

    val timeLabel: String
        get() = "16:42"

    val lensInfo: String
        get() = "${focalLength} • ISO ${iso} • ${exposureTime}"

    val resolutionString: String
        get() = "${width} × ${height} (${((width * height) / 1_000_000f).let { "%.1f MP".format(it) }})"
}

data class AlbumCollection(
    val id: Long,
    val name: String,
    val isPinned: Boolean = false,
    val isSmartGroup: Boolean = false,
    val smartGroupKey: String? = null,
    val sortOrderIndex: Int = 0,
    val itemIds: List<Long> = emptyList(),
    val coverItem: MediaItem? = null,
    val itemCount: Int = 0
)
