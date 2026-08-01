package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Wallpaper
import com.example.ui.components.NsfwWarningDialog
import com.example.ui.components.ShimmerItem
import com.example.ui.components.WallpaperCard
import com.example.ui.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: WallpaperViewModel,
    onNavigateToPreview: (Wallpaper) -> Unit
) {
    val searchState by viewModel.searchState.collectAsState()
    val favorites by viewModel.favoritesState.collectAsState()
    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }

    val popularTags = listOf("waifu", "neko", "maid", "marin-kitagawa", "mori-calliope", "raiden-shogun", "uniform", "ecchi", "ero", "hentai")
    val sourceApis = listOf("All", "Waifu.im", "Nekos.best", "Nekos API", "Nekosia Cat", "Unsplash", "Pexels")

    var showNsfwDialog by remember { mutableStateOf(false) }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }

    if (showNsfwDialog) {
        NsfwWarningDialog(
            onConfirm = {
                viewModel.setSearchNsfw(true)
                showNsfwDialog = false
            },
            onDismiss = { showNsfwDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen")
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchState.query,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search waifu tags or character...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Tag autocomplete chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            popularTags.forEach { tag ->
                val isSelected = searchState.selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleSearchTag(tag) },
                    label = { Text("#$tag", fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Filter Controls: SFW/NSFW Toggle & API Source Dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source API Dropdown Selector
            ExposedDropdownMenuBox(
                expanded = sourceDropdownExpanded,
                onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded }
            ) {
                OutlinedCard(
                    onClick = { sourceDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(text = "Source: ${searchState.selectedSourceApi}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                ExposedDropdownMenu(
                    expanded = sourceDropdownExpanded,
                    onDismissRequest = { sourceDropdownExpanded = false }
                ) {
                    sourceApis.forEach { api ->
                        DropdownMenuItem(
                            text = { Text(api) },
                            onClick = {
                                viewModel.setSearchSourceApi(api)
                                sourceDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // SFW / NSFW Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (searchState.isNsfw) "NSFW" else "SFW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (searchState.isNsfw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Switch(
                    checked = searchState.isNsfw,
                    onCheckedChange = { checked ->
                        if (checked) {
                            showNsfwDialog = true
                        } else {
                            viewModel.setSearchNsfw(false)
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Search action button
        Button(
            onClick = { viewModel.performSearch() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Search Wallpapers", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        // Results Staggered Grid
        if (searchState.isSearching) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(8) { index ->
                    ShimmerItem(height = if (index % 2 == 0) 240.dp else 190.dp)
                }
            }
        } else if (searchState.results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = searchState.errorMessage ?: "Search by tag or keyword above to discover waifus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchState.results, key = { it.id }) { wallpaper ->
                    val isFav = favoriteIds.contains(wallpaper.id)
                    WallpaperCard(
                        wallpaper = wallpaper,
                        isFavorite = isFav,
                        onFavoriteToggle = { viewModel.toggleFavorite(wallpaper) },
                        onClick = { onNavigateToPreview(wallpaper) }
                    )
                }
            }
        }
    }
}
