package com.example.data.model

data class Wallpaper(
    val id: String,
    val url: String,
    val previewUrl: String = url,
    val type: String = "sfw", // "sfw" or "nsfw"
    val tags: List<String> = emptyList(),
    val category: String = "waifu",
    val sourceApi: String = "Waifu.im",
    val artist: String? = null,
    val artistUrl: String? = null,
    val extension: String = "jpg",
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false
)
