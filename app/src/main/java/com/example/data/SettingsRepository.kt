package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.GlassPalette
import com.example.ui.theme.GlassVariant
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.glasslyDataStore by preferencesDataStore(name = "glassly_settings")

data class GlasslySettingsState(
    // 1. Appearance & Liquid Glass
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentSwatchIndex: Int = 0,
    val glassVariant: GlassVariant = GlassVariant.REGULAR,
    val glassIntensity: Float = 0.86f,
    val reduceGlass: Boolean = false,
    val appIconVariant: String = "Layered Glass", // Light, Dark, Tinted, Clear

    // 2. Library
    val defaultGridColumns: Int = 4,
    val squareAspectGrid: Boolean = true,
    val showBadges: Boolean = true,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    val openAtLatest: Boolean = true,
    val showLocationInHeaders: Boolean = true,
    val collectionTileDensity: CollectionTileDensity = CollectionTileDensity.LARGE,

    // 3. Viewer & Video
    val autoplayMotionPhotos: Boolean = true,
    val autoplayVideos: Boolean = true,
    val loopVideos: Boolean = true,
    val defaultPlaybackSpeed: Float = 1.0f,
    val keepScreenOn: Boolean = true,
    val showInfoOnSwipeUp: Boolean = true,

    // 4. Privacy & Security
    val hiddenAlbumLockEnabled: Boolean = true,
    val biometricUnlockEnabled: Boolean = true,
    val hiddenAlbumPin: String = "2626",
    val hidePreviewsInRecents: Boolean = false,
    val secureScreenFlag: Boolean = false,

    // 5. Storage
    val trashRetentionDays: Int = 30,

    // 6. Language
    val language: AppLanguage = AppLanguage.ENGLISH,

    // 7. Accessibility
    val reduceMotion: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val hapticStrength: Float = 0.85f,
    val largerTouchTargets: Boolean = false,
    val highContrastGlass: Boolean = false
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_INDEX = intPreferencesKey("accent_index")
        val GLASS_VARIANT = stringPreferencesKey("glass_variant")
        val GLASS_INTENSITY = floatPreferencesKey("glass_intensity")
        val REDUCE_GLASS = booleanPreferencesKey("reduce_glass")
        val APP_ICON_VARIANT = stringPreferencesKey("app_icon_variant")

        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val SQUARE_GRID = booleanPreferencesKey("square_grid")
        val SHOW_BADGES = booleanPreferencesKey("show_badges")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val OPEN_AT_LATEST = booleanPreferencesKey("open_at_latest")
        val SHOW_LOCATION = booleanPreferencesKey("show_location")
        val TILE_DENSITY = stringPreferencesKey("tile_density")

        val AUTOPLAY_MOTION = booleanPreferencesKey("autoplay_motion")
        val AUTOPLAY_VIDEOS = booleanPreferencesKey("autoplay_videos")
        val LOOP_VIDEOS = booleanPreferencesKey("loop_videos")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SWIPE_UP_INFO = booleanPreferencesKey("swipe_up_info")

        val HIDDEN_LOCK = booleanPreferencesKey("hidden_lock")
        val BIOMETRIC_UNLOCK = booleanPreferencesKey("biometric_unlock")
        val HIDDEN_PIN = stringPreferencesKey("hidden_pin")
        val HIDE_RECENTS = booleanPreferencesKey("hide_recents")
        val SECURE_SCREEN = booleanPreferencesKey("secure_screen")

        val TRASH_RETENTION_DAYS = intPreferencesKey("trash_retention_days")
        val LANGUAGE = stringPreferencesKey("language")

        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val HAPTIC_STRENGTH = floatPreferencesKey("haptic_strength")
        val LARGER_TARGETS = booleanPreferencesKey("larger_targets")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
    }

    val settingsFlow: Flow<GlasslySettingsState> = context.glasslyDataStore.data.map { prefs ->
        GlasslySettingsState(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            accentSwatchIndex = (prefs[Keys.ACCENT_INDEX] ?: 0).coerceIn(0, GlassPalette.AccentSwatches.lastIndex),
            glassVariant = prefs[Keys.GLASS_VARIANT]?.let { runCatching { GlassVariant.valueOf(it) }.getOrNull() }
                ?: GlassVariant.REGULAR,
            glassIntensity = (prefs[Keys.GLASS_INTENSITY] ?: 0.86f).coerceIn(0.15f, 1.0f),
            reduceGlass = prefs[Keys.REDUCE_GLASS] ?: false,
            appIconVariant = prefs[Keys.APP_ICON_VARIANT] ?: "Layered Glass",
            defaultGridColumns = (prefs[Keys.GRID_COLUMNS] ?: 4).coerceIn(2, 6),
            squareAspectGrid = prefs[Keys.SQUARE_GRID] ?: true,
            showBadges = prefs[Keys.SHOW_BADGES] ?: true,
            sortOrder = prefs[Keys.SORT_ORDER]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() }
                ?: SortOrder.NEWEST_FIRST,
            openAtLatest = prefs[Keys.OPEN_AT_LATEST] ?: true,
            showLocationInHeaders = prefs[Keys.SHOW_LOCATION] ?: true,
            collectionTileDensity = prefs[Keys.TILE_DENSITY]?.let {
                runCatching { CollectionTileDensity.valueOf(it) }.getOrNull()
            } ?: CollectionTileDensity.LARGE,
            autoplayMotionPhotos = prefs[Keys.AUTOPLAY_MOTION] ?: true,
            autoplayVideos = prefs[Keys.AUTOPLAY_VIDEOS] ?: true,
            loopVideos = prefs[Keys.LOOP_VIDEOS] ?: true,
            defaultPlaybackSpeed = prefs[Keys.PLAYBACK_SPEED] ?: 1.0f,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            showInfoOnSwipeUp = prefs[Keys.SWIPE_UP_INFO] ?: true,
            hiddenAlbumLockEnabled = prefs[Keys.HIDDEN_LOCK] ?: true,
            biometricUnlockEnabled = prefs[Keys.BIOMETRIC_UNLOCK] ?: true,
            hiddenAlbumPin = prefs[Keys.HIDDEN_PIN] ?: "2626",
            hidePreviewsInRecents = prefs[Keys.HIDE_RECENTS] ?: false,
            secureScreenFlag = prefs[Keys.SECURE_SCREEN] ?: false,
            trashRetentionDays = (prefs[Keys.TRASH_RETENTION_DAYS] ?: 30).coerceIn(7, 90),
            language = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.ENGLISH,
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            hapticStrength = (prefs[Keys.HAPTIC_STRENGTH] ?: 0.85f).coerceIn(0.2f, 1.0f),
            largerTouchTargets = prefs[Keys.LARGER_TARGETS] ?: false,
            highContrastGlass = prefs[Keys.HIGH_CONTRAST] ?: false
        )
    }

    suspend fun update(transform: (GlasslySettingsState) -> GlasslySettingsState) {
        context.glasslyDataStore.edit { prefs ->
            val current = GlasslySettingsState(
                themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
                accentSwatchIndex = prefs[Keys.ACCENT_INDEX] ?: 0,
                glassVariant = prefs[Keys.GLASS_VARIANT]?.let { runCatching { GlassVariant.valueOf(it) }.getOrNull() } ?: GlassVariant.REGULAR,
                glassIntensity = prefs[Keys.GLASS_INTENSITY] ?: 0.86f,
                reduceGlass = prefs[Keys.REDUCE_GLASS] ?: false,
                appIconVariant = prefs[Keys.APP_ICON_VARIANT] ?: "Layered Glass",
                defaultGridColumns = prefs[Keys.GRID_COLUMNS] ?: 4,
                squareAspectGrid = prefs[Keys.SQUARE_GRID] ?: true,
                showBadges = prefs[Keys.SHOW_BADGES] ?: true,
                sortOrder = prefs[Keys.SORT_ORDER]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() } ?: SortOrder.NEWEST_FIRST,
                openAtLatest = prefs[Keys.OPEN_AT_LATEST] ?: true,
                showLocationInHeaders = prefs[Keys.SHOW_LOCATION] ?: true,
                collectionTileDensity = prefs[Keys.TILE_DENSITY]?.let { runCatching { CollectionTileDensity.valueOf(it) }.getOrNull() } ?: CollectionTileDensity.LARGE,
                autoplayMotionPhotos = prefs[Keys.AUTOPLAY_MOTION] ?: true,
                autoplayVideos = prefs[Keys.AUTOPLAY_VIDEOS] ?: true,
                loopVideos = prefs[Keys.LOOP_VIDEOS] ?: true,
                defaultPlaybackSpeed = prefs[Keys.PLAYBACK_SPEED] ?: 1.0f,
                keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
                showInfoOnSwipeUp = prefs[Keys.SWIPE_UP_INFO] ?: true,
                hiddenAlbumLockEnabled = prefs[Keys.HIDDEN_LOCK] ?: true,
                biometricUnlockEnabled = prefs[Keys.BIOMETRIC_UNLOCK] ?: true,
                hiddenAlbumPin = prefs[Keys.HIDDEN_PIN] ?: "2626",
                hidePreviewsInRecents = prefs[Keys.HIDE_RECENTS] ?: false,
                secureScreenFlag = prefs[Keys.SECURE_SCREEN] ?: false,
                trashRetentionDays = prefs[Keys.TRASH_RETENTION_DAYS] ?: 30,
                language = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.ENGLISH,
                reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
                hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
                hapticStrength = prefs[Keys.HAPTIC_STRENGTH] ?: 0.85f,
                largerTouchTargets = prefs[Keys.LARGER_TARGETS] ?: false,
                highContrastGlass = prefs[Keys.HIGH_CONTRAST] ?: false
            )
            val updated = transform(current)
            prefs[Keys.THEME_MODE] = updated.themeMode.name
            prefs[Keys.ACCENT_INDEX] = updated.accentSwatchIndex
            prefs[Keys.GLASS_VARIANT] = updated.glassVariant.name
            prefs[Keys.GLASS_INTENSITY] = updated.glassIntensity
            prefs[Keys.REDUCE_GLASS] = updated.reduceGlass
            prefs[Keys.APP_ICON_VARIANT] = updated.appIconVariant
            prefs[Keys.GRID_COLUMNS] = updated.defaultGridColumns
            prefs[Keys.SQUARE_GRID] = updated.squareAspectGrid
            prefs[Keys.SHOW_BADGES] = updated.showBadges
            prefs[Keys.SORT_ORDER] = updated.sortOrder.name
            prefs[Keys.OPEN_AT_LATEST] = updated.openAtLatest
            prefs[Keys.SHOW_LOCATION] = updated.showLocationInHeaders
            prefs[Keys.TILE_DENSITY] = updated.collectionTileDensity.name
            prefs[Keys.AUTOPLAY_MOTION] = updated.autoplayMotionPhotos
            prefs[Keys.AUTOPLAY_VIDEOS] = updated.autoplayVideos
            prefs[Keys.LOOP_VIDEOS] = updated.loopVideos
            prefs[Keys.PLAYBACK_SPEED] = updated.defaultPlaybackSpeed
            prefs[Keys.KEEP_SCREEN_ON] = updated.keepScreenOn
            prefs[Keys.SWIPE_UP_INFO] = updated.showInfoOnSwipeUp
            prefs[Keys.HIDDEN_LOCK] = updated.hiddenAlbumLockEnabled
            prefs[Keys.BIOMETRIC_UNLOCK] = updated.biometricUnlockEnabled
            prefs[Keys.HIDDEN_PIN] = updated.hiddenAlbumPin
            prefs[Keys.HIDE_RECENTS] = updated.hidePreviewsInRecents
            prefs[Keys.SECURE_SCREEN] = updated.secureScreenFlag
            prefs[Keys.TRASH_RETENTION_DAYS] = updated.trashRetentionDays
            prefs[Keys.LANGUAGE] = updated.language.name
            prefs[Keys.REDUCE_MOTION] = updated.reduceMotion
            prefs[Keys.HAPTICS_ENABLED] = updated.hapticsEnabled
            prefs[Keys.HAPTIC_STRENGTH] = updated.hapticStrength
            prefs[Keys.LARGER_TARGETS] = updated.largerTouchTargets
            prefs[Keys.HIGH_CONTRAST] = updated.highContrastGlass
        }
    }

    suspend fun resetAll() {
        context.glasslyDataStore.edit { it.clear() }
    }
}
