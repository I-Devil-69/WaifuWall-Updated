package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.data.model.Wallpaper
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WallpaperViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: WallpaperViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val screens = listOf(Screen.Home, Screen.Search, Screen.Favorites, Screen.Settings)
                val isPreviewScreen = currentRoute?.startsWith("preview") == true

                Scaffold(
                    topBar = {
                        if (!isPreviewScreen) {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = "Waifu Walls",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = !isPreviewScreen,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .testTag("bottom_nav_bar")
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                screens.forEach { screen ->
                                    val isSelected = currentRoute == screen.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = { Text(screen.title) }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToPreview = { wallpaper ->
                                    navController.navigate(
                                        Screen.createPreviewRoute(
                                            id = wallpaper.id,
                                            url = wallpaper.url,
                                            sourceApi = wallpaper.sourceApi,
                                            category = wallpaper.category,
                                            type = wallpaper.type,
                                            artist = wallpaper.artist
                                        )
                                    )
                                }
                            )
                        }

                        composable(Screen.Search.route) {
                            SearchScreen(
                                viewModel = viewModel,
                                onNavigateToPreview = { wallpaper ->
                                    navController.navigate(
                                        Screen.createPreviewRoute(
                                            id = wallpaper.id,
                                            url = wallpaper.url,
                                            sourceApi = wallpaper.sourceApi,
                                            category = wallpaper.category,
                                            type = wallpaper.type,
                                            artist = wallpaper.artist
                                        )
                                    )
                                }
                            )
                        }

                        composable(Screen.Favorites.route) {
                            FavoritesScreen(
                                viewModel = viewModel,
                                onNavigateToPreview = { wallpaper ->
                                    navController.navigate(
                                        Screen.createPreviewRoute(
                                            id = wallpaper.id,
                                            url = wallpaper.url,
                                            sourceApi = wallpaper.sourceApi,
                                            category = wallpaper.category,
                                            type = wallpaper.type,
                                            artist = wallpaper.artist
                                        )
                                    )
                                },
                                onExploreHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().id)
                                    }
                                }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(viewModel = viewModel)
                        }

                        // Preview Destination with Deep Link Support (waifuwalls://wallpaper?id=...&url=...)
                        composable(
                            route = Screen.PREVIEW_ROUTE,
                            arguments = listOf(
                                navArgument("id") { type = NavType.StringType; defaultValue = "" },
                                navArgument("url") { type = NavType.StringType; defaultValue = "" },
                                navArgument("sourceApi") { type = NavType.StringType; defaultValue = "Waifu.im" },
                                navArgument("category") { type = NavType.StringType; defaultValue = "waifu" },
                                navArgument("type") { type = NavType.StringType; defaultValue = "sfw" },
                                navArgument("artist") { type = NavType.StringType; defaultValue = "" }
                            ),
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern = "waifuwalls://wallpaper?id={id}&url={url}&sourceApi={sourceApi}&category={category}&type={type}&artist={artist}"
                                }
                            )
                        ) { backStackEntry ->
                            val rawId = backStackEntry.arguments?.getString("id") ?: ""
                            val rawUrl = backStackEntry.arguments?.getString("url") ?: ""
                            val rawSourceApi = backStackEntry.arguments?.getString("sourceApi") ?: "Waifu.im"
                            val rawCategory = backStackEntry.arguments?.getString("category") ?: "waifu"
                            val rawType = backStackEntry.arguments?.getString("type") ?: "sfw"
                            val rawArtist = backStackEntry.arguments?.getString("artist") ?: ""

                            val id = decodeUrlParam(rawId)
                            val url = decodeUrlParam(rawUrl)
                            val sourceApi = decodeUrlParam(rawSourceApi)
                            val category = decodeUrlParam(rawCategory)
                            val type = decodeUrlParam(rawType)
                            val artist = decodeUrlParam(rawArtist)

                            val wallpaper = Wallpaper(
                                id = if (id.isBlank()) "wallpaper_${url.hashCode()}" else id,
                                url = url,
                                previewUrl = url,
                                type = type,
                                category = category,
                                sourceApi = sourceApi,
                                artist = if (artist.isBlank()) null else artist
                            )

                            PreviewScreen(
                                wallpaper = wallpaper,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun decodeUrlParam(param: String): String {
        return try {
            URLDecoder.decode(param, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            param
        }
    }
}
