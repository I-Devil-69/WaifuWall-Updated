package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Wallpaper

@Entity(tableName = "favorites")
data class FavoriteWallpaperEntity(
    @PrimaryKey val id: String,
    val url: String,
    val previewUrl: String,
    val type: String,
    val tagsCsv: String,
    val category: String,
    val sourceApi: String,
    val artist: String?,
    val artistUrl: String?,
    val extension: String,
    val width: Int,
    val height: Int,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Wallpaper {
        val tagList = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(",").map { it.trim() }
        return Wallpaper(
            id = id,
            url = url,
            previewUrl = previewUrl,
            type = type,
            tags = tagList,
            category = category,
            sourceApi = sourceApi,
            artist = artist,
            artistUrl = artistUrl,
            extension = extension,
            width = width,
            height = height,
            isFavorite = true
        )
    }

    companion object {
        fun fromDomain(wallpaper: Wallpaper): FavoriteWallpaperEntity {
            return FavoriteWallpaperEntity(
                id = wallpaper.id,
                url = wallpaper.url,
                previewUrl = wallpaper.previewUrl,
                type = wallpaper.type,
                tagsCsv = wallpaper.tags.joinToString(","),
                category = wallpaper.category,
                sourceApi = wallpaper.sourceApi,
                artist = wallpaper.artist,
                artistUrl = wallpaper.artistUrl,
                extension = wallpaper.extension,
                width = wallpaper.width,
                height = wallpaper.height
            )
        }
    }
}
