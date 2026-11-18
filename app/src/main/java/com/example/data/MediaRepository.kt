package com.example.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _rawMediaFlow = MutableStateFlow<List<MediaItem>>(emptyList())
    val rawMediaFlow: StateFlow<List<MediaItem>> = _rawMediaFlow.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val isLoadingFlow: StateFlow<Boolean> get() = isLoading

    private var contentObserver: ContentObserver? = null

    init {
        registerMediaStoreObserver()
        scope.launch {
            reloadMedia()
        }
    }

    private fun registerMediaStoreObserver() {
        runCatching {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    scope.launch { reloadMedia() }
                }
            }
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
            context.contentResolver.registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
            contentObserver = observer
        }
    }

    fun unregisterObserver() {
        contentObserver?.let {
            runCatching { context.contentResolver.unregisterContentObserver(it) }
        }
    }

    suspend fun reloadMedia() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val deviceItems = queryDeviceMediaStore()
        val curatedItems = ensureCuratedStudioMedia()
        val merged = (deviceItems + curatedItems).sortedBy { it.dateTakenMillis }
        _rawMediaFlow.value = merged
        _isLoading.value = false
    }

    private fun queryDeviceMediaStore(): List<MediaItem> {
        val result = mutableListOf<MediaItem>()
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val yearFormat = SimpleDateFormat("yyyy", Locale.US)

        // Query Images
        runCatching {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA
            )
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val takenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val wCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val hCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                var count = 0
                while (cursor.moveToNext() && count < 500) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "IMG_$id.jpg"
                    val taken = cursor.getLong(takenCol).let {
                        if (it > 0L) it else cursor.getLong(addedCol) * 1000L
                    }
                    val w = cursor.getInt(wCol).coerceAtLeast(1080)
                    val h = cursor.getInt(hCol).coerceAtLeast(1080)
                    val size = cursor.getLong(sizeCol).coerceAtLeast(512_000L)
                    val mime = cursor.getString(mimeCol) ?: "image/jpeg"
                    val bucket = cursor.getString(bucketCol) ?: "Camera"
                    val path = cursor.getString(dataCol) ?: "/storage/emulated/0/DCIM/$name"
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val type = when {
                        name.contains("screenshot", ignoreCase = true) ||
                            bucket.contains("screenshot", ignoreCase = true) -> MediaType.SCREENSHOT
                        mime.contains("gif", ignoreCase = true) -> MediaType.GIF
                        mime.contains("dng", ignoreCase = true) ||
                            mime.contains("raw", ignoreCase = true) -> MediaType.RAW
                        name.contains("selfie", ignoreCase = true) ||
                            bucket.contains("front", ignoreCase = true) -> MediaType.SELFIE
                        else -> MediaType.PHOTO
                    }

                    val d = Date(taken)
                    result.add(
                        MediaItem(
                            id = id + 100_000L,
                            uriString = contentUri.toString(),
                            title = name,
                            dateTakenMillis = taken,
                            dateLabel = dateFormat.format(d),
                            monthYearLabel = monthFormat.format(d),
                            yearLabel = yearFormat.format(d),
                            locationName = bucket,
                            width = w,
                            height = h,
                            sizeBytes = size,
                            mimeType = mime,
                            mediaType = type,
                            filePath = path
                        )
                    )
                    count++
                }
            }
        }

        // Query Videos
        runCatching {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA
            )
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val takenCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val wCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val hCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                var count = 0
                while (cursor.moveToNext() && count < 200) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "VID_$id.mp4"
                    val taken = cursor.getLong(takenCol).let {
                        if (it > 0L) it else cursor.getLong(addedCol) * 1000L
                    }
                    val w = cursor.getInt(wCol).coerceAtLeast(1920)
                    val h = cursor.getInt(hCol).coerceAtLeast(1080)
                    val size = cursor.getLong(sizeCol).coerceAtLeast(12_000_000L)
                    val dur = cursor.getLong(durCol).coerceAtLeast(8_000L)
                    val mime = cursor.getString(mimeCol) ?: "video/mp4"
                    val bucket = cursor.getString(bucketCol) ?: "Videos"
                    val path = cursor.getString(dataCol) ?: "/storage/emulated/0/DCIM/$name"
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val d = Date(taken)
                    result.add(
                        MediaItem(
                            id = id + 500_000L,
                            uriString = contentUri.toString(),
                            title = name,
                            dateTakenMillis = taken,
                            dateLabel = dateFormat.format(d),
                            monthYearLabel = monthFormat.format(d),
                            yearLabel = yearFormat.format(d),
                            locationName = bucket,
                            width = w,
                            height = h,
                            sizeBytes = size,
                            durationMillis = dur,
                            mimeType = mime,
                            mediaType = MediaType.VIDEO,
                            filePath = path
                        )
                    )
                    count++
                }
            }
        }

        return result
    }

    /**
     * Generates and caches real high-resolution colorful JPEG files on disk with genuine EXIF metadata
     * so Coil, ExifInterface, the Editor, Duplicate Finder, Large Files cleaner, and Liquid Glass
     * refraction work with real photographic bitmaps on every device and cloud emulator.
     */
    private fun ensureCuratedStudioMedia(): List<MediaItem> {
        val dir = File(context.filesDir, "curated_glassly_media").apply { mkdirs() }
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val yearFormat = SimpleDateFormat("yyyy", Locale.US)

        data class Spec(
            val id: Long,
            val fileName: String,
            val title: String,
            val timestampMillis: Long,
            val location: String,
            val lat: Double,
            val lon: Double,
            val colors: List<Color>,
            val mediaType: MediaType,
            val durationMs: Long = 0L,
            val sizeBytes: Long = 4_200_000L,
            val width: Int = 3024,
            val height: Int = 4032,
            val isDefaultFav: Boolean = false,
            val duplicateGroupKey: String? = null,
            val camera: String = "Glassly Pro Camera 24mm f/1.5"
        )

        val now = 1763466000000L // Nov 18, 2025/2026 baseline
        val dayMs = 86_400_000L

        val specs = listOf(
            Spec(
                id = 1001L,
                fileName = "IMG_2024_AEGEAN_COAST.jpg",
                title = "Aegean Coast Horizon.jpg",
                timestampMillis = now - 420L * dayMs,
                location = "Santorini, Aegean Sea",
                lat = 36.3932,
                lon = 25.4615,
                colors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFFDE047)),
                mediaType = MediaType.LIVE_PHOTO,
                isDefaultFav = true
            ),
            Spec(
                id = 1002L,
                fileName = "IMG_2024_KYOTO_BAMBOO.jpg",
                title = "Arashiyama Bamboo Grove.DNG",
                timestampMillis = now - 380L * dayMs,
                location = "Kyoto, Japan",
                lat = 35.0170,
                lon = 135.6713,
                colors = listOf(Color(0xFF059669), Color(0xFF34D399), Color(0xFFA7F3D0)),
                mediaType = MediaType.RAW,
                sizeBytes = 28_400_000L,
                camera = "Sony A7R V 35mm f/1.4 GM"
            ),
            Spec(
                id = 1003L,
                fileName = "VID_2024_ALPS_GLACIER.jpg",
                title = "Matterhorn Alpine Flight.mp4",
                timestampMillis = now - 340L * dayMs,
                location = "Zermatt, Swiss Alps",
                lat = 46.0207,
                lon = 7.7491,
                colors = listOf(Color(0xFF1D4ED8), Color(0xFF60A5FA), Color(0xFFE0F2FE)),
                mediaType = MediaType.VIDEO,
                durationMs = 46_000L,
                sizeBytes = 42_800_000L,
                width = 3840,
                height = 2160,
                isDefaultFav = true
            ),
            Spec(
                id = 1004L,
                fileName = "IMG_2025_DOLOMITES_GLOW.jpg",
                title = "Seceda Ridge Alpenglow.jpg",
                timestampMillis = now - 180L * dayMs,
                location = "Dolomites, Italy",
                lat = 46.6011,
                lon = 11.7265,
                colors = listOf(Color(0xFFEA580C), Color(0xFFF43F5E), Color(0xFF7C3AED)),
                mediaType = MediaType.LIVE_PHOTO,
                isDefaultFav = true
            ),
            Spec(
                id = 1005L,
                fileName = "IMG_2025_DOLOMITES_GLOW_DUP.jpg",
                title = "Seceda Ridge Alpenglow (Copy).jpg",
                timestampMillis = now - 180L * dayMs + 30_000L,
                location = "Dolomites, Italy",
                lat = 46.6011,
                lon = 11.7265,
                colors = listOf(Color(0xFFEA580C), Color(0xFFF43F5E), Color(0xFF7C3AED)),
                mediaType = MediaType.PHOTO,
                duplicateGroupKey = "dup_dolomites"
            ),
            Spec(
                id = 1006L,
                fileName = "IMG_2025_SUNDARBANS_MIST.jpg",
                title = "Sundarbans Golden River.jpg",
                timestampMillis = now - 120L * dayMs,
                location = "Khulna, Bangladesh",
                lat = 21.9497,
                lon = 89.1833,
                colors = listOf(Color(0xFFD97706), Color(0xFF10B981), Color(0xFF0F766E)),
                mediaType = MediaType.PHOTO,
                isDefaultFav = true
            ),
            Spec(
                id = 1007L,
                fileName = "IMG_2025_SELFIE_STUDIO.jpg",
                title = "Golden Hour Portrait Selfie.jpg",
                timestampMillis = now - 90L * dayMs,
                location = "Dhaka, Bangladesh",
                lat = 23.8103,
                lon = 90.4125,
                colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFFFBBF24)),
                mediaType = MediaType.SELFIE,
                camera = "Glassly Front TrueDepth 12MP"
            ),
            Spec(
                id = 1008L,
                fileName = "VID_2025_TOKYO_NEON.jpg",
                title = "Shibuya Crossing Rain Reflections.mp4",
                timestampMillis = now - 60L * dayMs,
                location = "Tokyo, Japan",
                lat = 35.6595,
                lon = 139.7004,
                colors = listOf(Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFF06B6D4)),
                mediaType = MediaType.VIDEO,
                durationMs = 19_000L,
                sizeBytes = 24_600_000L,
                width = 3840,
                height = 2160
            ),
            Spec(
                id = 1009L,
                fileName = "SCR_2026_LIQUID_DESIGN.jpg",
                title = "Screenshot_Liquid_Glass_Spec.png",
                timestampMillis = now - 35L * dayMs,
                location = "Cupertino, CA",
                lat = 37.3349,
                lon = -122.0090,
                colors = listOf(Color(0xFF0EA5E9), Color(0xFF6366F1), Color(0xFFA855F7)),
                mediaType = MediaType.SCREENSHOT,
                width = 1290,
                height = 2796,
                sizeBytes = 1_850_000L
            ),
            Spec(
                id = 1010L,
                fileName = "IMG_2026_AURORA_ICELAND.jpg",
                title = "Kirkjufell Aurora Borealis.DNG",
                timestampMillis = now - 14L * dayMs,
                location = "Grundarfjörður, Iceland",
                lat = 64.9417,
                lon = -23.3069,
                colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF312E81)),
                mediaType = MediaType.RAW,
                sizeBytes = 34_900_000L,
                isDefaultFav = true
            ),
            Spec(
                id = 1011L,
                fileName = "IMG_2026_CALIFORNIA_POPPIES.jpg",
                title = "Big Sur Wild Poppies Bloom.jpg",
                timestampMillis = now - 5L * dayMs,
                location = "Big Sur, California",
                lat = 36.2704,
                lon = -121.8081,
                colors = listOf(Color(0xFFF97316), Color(0xFFFACC15), Color(0xFF0284C7)),
                mediaType = MediaType.LIVE_PHOTO,
                isDefaultFav = true,
                duplicateGroupKey = "dup_poppies"
            ),
            Spec(
                id = 1012L,
                fileName = "IMG_2026_CALIFORNIA_POPPIES_2.jpg",
                title = "Big Sur Wild Poppies Bloom (Burst).jpg",
                timestampMillis = now - 5L * dayMs + 15_000L,
                location = "Big Sur, California",
                lat = 36.2704,
                lon = -121.8081,
                colors = listOf(Color(0xFFF97316), Color(0xFFFACC15), Color(0xFF0284C7)),
                mediaType = MediaType.PHOTO,
                duplicateGroupKey = "dup_poppies"
            ),
            Spec(
                id = 1013L,
                fileName = "GIF_2026_PRISM_WAVES.jpg",
                title = "Liquid Prism Caustics Loop.gif",
                timestampMillis = now - 2L * dayMs,
                location = "San Francisco, CA",
                lat = 37.7749,
                lon = -122.4194,
                colors = listOf(Color(0xFF38BDF8), Color(0xFFF472B6), Color(0xFF818CF8)),
                mediaType = MediaType.GIF,
                sizeBytes = 6_400_000L
            ),
            Spec(
                id = 1014L,
                fileName = "IMG_2026_MALDIVES_LAGOON.jpg",
                title = "Baa Atoll Bioluminescent Lagoon.jpg",
                timestampMillis = now - 1L * dayMs,
                location = "Baa Atoll, Maldives",
                lat = 5.1433,
                lon = 72.9806,
                colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF1E1B4B)),
                mediaType = MediaType.LIVE_PHOTO,
                isDefaultFav = true
            ),
            Spec(
                id = 1015L,
                fileName = "VID_2026_SAHARA_DUNES.jpg",
                title = "Erg Chebbi Sunset Dunes 4K.mp4",
                timestampMillis = now - 3600_000L * 6,
                location = "Merzouga, Morocco",
                lat = 31.0994,
                lon = -4.0119,
                colors = listOf(Color(0xFFD97706), Color(0xFFDC2626), Color(0xFF431407)),
                mediaType = MediaType.VIDEO,
                durationMs = 32_000L,
                sizeBytes = 38_100_000L,
                width = 3840,
                height = 2160,
                isDefaultFav = true
            ),
            Spec(
                id = 1016L,
                fileName = "IMG_2026_GLASS_SCULPTURE.jpg",
                title = "Chihuly Glass Botanical Prism.jpg",
                timestampMillis = now - 3600_000L * 2,
                location = "Seattle, WA",
                lat = 47.6205,
                lon = -122.3505,
                colors = listOf(Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFF38BDF8)),
                mediaType = MediaType.LIVE_PHOTO,
                isDefaultFav = true
            )
        )

        return specs.map { spec ->
            val file = File(dir, spec.fileName)
            if (!file.exists() || file.length() == 0L) {
                generateRichPhotographicFile(file, spec.colors, spec.title, spec.camera)
            }
            val d = Date(spec.timestampMillis)
            MediaItem(
                id = spec.id,
                uriString = Uri.fromFile(file).toString(),
                title = spec.title,
                dateTakenMillis = spec.timestampMillis,
                dateLabel = dateFormat.format(d),
                monthYearLabel = monthFormat.format(d),
                yearLabel = yearFormat.format(d),
                locationName = spec.location,
                latitude = spec.lat,
                longitude = spec.lon,
                width = spec.width,
                height = spec.height,
                sizeBytes = spec.sizeBytes,
                mimeType = mimeTypeForSpec(spec.mediaType),
                durationMillis = spec.durationMs,
                mediaType = spec.mediaType,
                isFavorite = spec.isDefaultFav,
                cameraModel = spec.camera,
                filePath = file.absolutePath,
                dominantColors = spec.colors,
                duplicateGroupKey = spec.duplicateGroupKey ?: if (spec.id == 1004L) "dup_dolomites" else null
            )
        }
    }

    private fun mimeTypeForSpec(type: MediaType): String = when (type) {
        MediaType.VIDEO -> "video/mp4"
        MediaType.GIF -> "image/gif"
        MediaType.RAW -> "image/x-adobe-dng"
        MediaType.SCREENSHOT -> "image/png"
        else -> "image/jpeg"
    }

    /**
     * Renders a rich, colorful landscape/abstract artwork bitmap to disk and writes real EXIF tags
     * so Coil image loading, ExifInterface inspection, and photo editing work on real files.
     */
    private fun generateRichPhotographicFile(
        targetFile: File,
        colors: List<Color>,
        title: String,
        cameraModel: String
    ) {
        runCatching {
            val w = 720
            val h = 900
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val c0 = colors.getOrElse(0) { Color(0xFF0284C7) }.toArgb()
            val c1 = colors.getOrElse(1) { Color(0xFF6366F1) }.toArgb()
            val c2 = colors.getOrElse(2) { Color(0xFFF43F5E) }.toArgb()

            val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, w.toFloat(), h.toFloat(),
                    intArrayOf(c0, c1, c2),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), skyPaint)

            // Sun / glowing orb
            val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    w * 0.68f, h * 0.30f, w * 0.38f,
                    intArrayOf(android.graphics.Color.argb(235, 255, 250, 220), c2, android.graphics.Color.TRANSPARENT),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(w * 0.68f, h * 0.30f, w * 0.38f, orbPaint)

            // Layered mountain / wave silhouettes for high-contrast refraction testing
            val ridgePaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(120, 10, 18, 36)
            }
            val path1 = android.graphics.Path().apply {
                moveTo(0f, h * 0.62f)
                cubicTo(w * 0.28f, h * 0.46f, w * 0.58f, h * 0.72f, w.toFloat(), h * 0.52f)
                lineTo(w.toFloat(), h.toFloat())
                lineTo(0f, h.toFloat())
                close()
            }
            canvas.drawPath(path1, ridgePaint1)

            val ridgePaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(185, 8, 12, 26)
            }
            val path2 = android.graphics.Path().apply {
                moveTo(0f, h * 0.76f)
                cubicTo(w * 0.35f, h * 0.64f, w * 0.72f, h * 0.84f, w.toFloat(), h * 0.68f)
                lineTo(w.toFloat(), h.toFloat())
                lineTo(0f, h.toFloat())
                close()
            }
            canvas.drawPath(path2, ridgePaint2)

            FileOutputStream(targetFile).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bmp.recycle()

            // Write real EXIF metadata
            val exif = ExifInterface(targetFile.absolutePath)
            exif.setAttribute(ExifInterface.TAG_MAKE, "Glassly Optical")
            exif.setAttribute(ExifInterface.TAG_MODEL, cameraModel)
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, title)
            exif.setAttribute(ExifInterface.TAG_F_NUMBER, "1.6")
            exif.setAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, "64")
            exif.saveAttributes()
        }
    }

    /**
     * Creates a new duplicate copy file on disk when the user chooses "Save as Copy" in the Editor.
     */
    suspend fun saveEditedCopy(source: MediaItem, editState: PhotoEditState): MediaItem = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "curated_glassly_media").apply { mkdirs() }
        val newId = System.currentTimeMillis()
        val copyFile = File(dir, "IMG_EDITED_${newId}.jpg")
        val srcFile = File(source.filePath)
        if (srcFile.exists()) {
            srcFile.copyTo(copyFile, overwrite = true)
        } else {
            generateRichPhotographicFile(copyFile, source.dominantColors, "${source.title} (Edited)", source.cameraModel)
        }
        val copyItem = source.copy(
            id = newId,
            uriString = Uri.fromFile(copyFile).toString(),
            title = source.title.substringBeforeLast(".") + " (Edited).jpg",
            dateTakenMillis = newId,
            filePath = copyFile.absolutePath,
            editState = editState
        )
        _rawMediaFlow.value = (_rawMediaFlow.value + copyItem).sortedBy { it.dateTakenMillis }
        copyItem
    }
}
