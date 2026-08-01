package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Wallpaper
import com.example.ui.components.ShimmerItem
import com.example.ui.components.WallpaperCard
import com.example.ui.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WallpaperViewModel,
    onNavigateToPreview: (Wallpaper) -> Unit
) {
    val homeState by viewModel.homeState.collectAsState()
    val favorites by viewModel.favoritesState.collectAsState()
    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }

    val tags = listOf("waifu", "neko", "maid", "marin-kitagawa", "mori-calliope", "raiden-shogun", "uniform", "ecchi", "ero", "hentai")
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // Tag selector pills horizontal carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                val isSelected = homeState.selectedTag == tag
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setHomeTag(tag) },
                    label = { Text("#$tag", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // Pull to refresh & grid layout
        PullToRefreshBox(
            isRefreshing = homeState.isLoading && homeState.wallpapers.isNotEmpty(),
            onRefresh = { viewModel.loadHomeFeed(refresh = true) },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (homeState.isLoading && homeState.wallpapers.isEmpty()) {
                // Initial Shimmer Loading Grid
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(10) { index ->
                        val shimmerHeight = if (index % 2 == 0) 240.dp else 190.dp
                        ShimmerItem(height = shimmerHeight)
                    }
                }
            } else if (homeState.wallpapers.isEmpty() && !homeState.isLoading) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Wallpaper,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No wallpapers found for #${homeState.selectedTag}",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = homeState.errorMessage ?: "Try pulling down to refresh or select another category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { viewModel.loadHomeFeed(refresh = true) }) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Retry")
                        }
                    }
                }
            } else {
                val gridState = rememberLazyStaggeredGridState()

                // Endless scroll check
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val layoutInfo = gridState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        totalItems > 0 && lastVisibleItem >= totalItems - 4
                    }
                }

                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore && !homeState.isLoading) {
                        viewModel.loadHomeFeed(refresh = false)
                    }
                }

                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(homeState.wallpapers, key = { it.id }) { wallpaper ->
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
}
