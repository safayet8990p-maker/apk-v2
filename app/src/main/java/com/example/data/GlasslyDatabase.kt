package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "media_overrides")
data class MediaOverrideEntity(
    @PrimaryKey val mediaId: Long,
    val customTitle: String? = null,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAtMillis: Long? = null,
    val cropRatio: Float? = null,
    val rotationDegrees: Float = 0f,
    val straightenDegrees: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val warmth: Float = 0f,
    val sharpness: Float = 0f,
    val vignette: Float = 0f,
    val filterName: String = "Original",
    val overlayText: String = "",
    val markupStrokesCount: Int = 0
)

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val albumId: Long = 0,
    val name: String,
    val isPinned: Boolean = false,
    val sortOrderIndex: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "album_media_cross_ref", primaryKeys = ["albumId", "mediaId"])
data class AlbumMediaCrossRef(
    val albumId: Long,
    val mediaId: Long
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Dao
interface GlasslyDao {
    @Query("SELECT * FROM media_overrides")
    fun observeMediaOverrides(): Flow<List<MediaOverrideEntity>>

    @Query("SELECT * FROM media_overrides WHERE mediaId = :mediaId LIMIT 1")
    suspend fun getMediaOverride(mediaId: Long): MediaOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMediaOverride(entity: MediaOverrideEntity)

    @Query("DELETE FROM media_overrides WHERE mediaId = :mediaId")
    suspend fun deleteMediaOverride(mediaId: Long)

    @Query("SELECT * FROM albums ORDER BY sortOrderIndex ASC, createdAtMillis DESC")
    fun observeAlbums(): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Query("UPDATE albums SET name = :newName WHERE albumId = :albumId")
    suspend fun renameAlbum(albumId: Long, newName: String)

    @Query("UPDATE albums SET isPinned = :pinned WHERE albumId = :albumId")
    suspend fun setAlbumPinned(albumId: Long, pinned: Boolean)

    @Query("UPDATE albums SET sortOrderIndex = :orderIndex WHERE albumId = :albumId")
    suspend fun updateAlbumOrder(albumId: Long, orderIndex: Int)

    @Query("DELETE FROM albums WHERE albumId = :albumId")
    suspend fun deleteAlbum(albumId: Long)

    @Query("SELECT * FROM album_media_cross_ref")
    fun observeAlbumCrossRefs(): Flow<List<AlbumMediaCrossRef>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMediaToAlbum(ref: AlbumMediaCrossRef)

    @Query("DELETE FROM album_media_cross_ref WHERE albumId = :albumId AND mediaId = :mediaId")
    suspend fun removeMediaFromAlbum(albumId: Long, mediaId: Long)

    @Query("SELECT * FROM recent_searches ORDER BY timestampMillis DESC LIMIT 12")
    fun observeRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()
}

@Database(
    entities = [
        MediaOverrideEntity::class,
        AlbumEntity::class,
        AlbumMediaCrossRef::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GlasslyDatabase : RoomDatabase() {
    abstract fun glasslyDao(): GlasslyDao

    companion object {
        @Volatile
        private var INSTANCE: GlasslyDatabase? = null

        fun getInstance(context: Context): GlasslyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GlasslyDatabase::class.java,
                    "glassly_gallery.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
