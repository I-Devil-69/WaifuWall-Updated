package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : Screen("home", "Waifus", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : Screen("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    object Favorites : Screen("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        const val PREVIEW_ROUTE = "preview?id={id}&url={url}&sourceApi={sourceApi}&category={category}&type={type}&artist={artist}"

        fun createPreviewRoute(
            id: String,
            url: String,
            sourceApi: String = "Waifu.im",
            category: String = "waifu",
            type: String = "sfw",
            artist: String? = null
        ): String {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            val encodedArtist = URLEncoder.encode(artist ?: "", StandardCharsets.UTF_8.toString())
            val encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString())
            return "preview?id=$encodedId&url=$encodedUrl&sourceApi=$sourceApi&category=$category&type=$type&artist=$encodedArtist"
        }
    }
}
