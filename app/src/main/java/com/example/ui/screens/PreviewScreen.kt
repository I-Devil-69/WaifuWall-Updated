package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Wallpaper
import com.example.ui.components.SetWallpaperDialog
import com.example.ui.components.WallpaperInfoDialog
import com.example.ui.viewmodel.WallpaperViewModel
import com.example.util.WallpaperTarget

import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewScreen(
    wallpaper: Wallpaper,
    viewModel: WallpaperViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isFav by viewModel.isFavoriteFlow(wallpaper.id).collectAsState(initial = false)

    var showSetWallpaperDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (showSetWallpaperDialog) {
        SetWallpaperDialog(
            onDismiss = { showSetWallpaperDialog = false },
            onSelectTarget = { target ->
                showSetWallpaperDialog = false
                isProcessing = true
                viewModel.applyWallpaper(wallpaper.url, target) { result ->
                    isProcessing = false
                    result.fold(
                        onSuccess = {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Wallpaper set successfully!") }
                        },
                        onFailure = { err ->
                            coroutineScope.launch { snackbarHostState.showSnackbar("Error setting wallpaper: ${err.localizedMessage}") }
                        }
                    )
                }
            }
        )
    }

    if (showInfoDialog) {
        WallpaperInfoDialog(
            wallpaper = wallpaper,
            onDismiss = { showInfoDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
        modifier = Modifier
            .fillMaxSize()
            .testTag("preview_screen")
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Blurred Background Image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaper.previewUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(25.dp)
            )

            // Dark tint overlay over blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )

            // 2. Center Full-Bleed Wallpaper Image with Double-Tap Info Gesture
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onDoubleClick = { showInfoDialog = true },
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(wallpaper.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full-screen wallpaper preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 40.dp, horizontal = 8.dp)
                )

                if (isProcessing) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // 3. Top Action Header (Back Arrow & Info Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.55f), shape = CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.55f), shape = CircleShape)
                ) {
                    Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.White)
                }
            }

            // 4. Bottom Glassmorphism Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like (Favorite) Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(wallpaper) }
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) Color.Red else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(text = if (isFav) "Liked" else "Like", color = Color.White, fontSize = 10.sp)
                    }

                    // Download Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                isProcessing = true
                                viewModel.downloadWallpaper(wallpaper.url, "Waifu_${wallpaper.id}") { result ->
                                    isProcessing = false
                                    result.fold(
                                        onSuccess = {
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Saved to Pictures/WaifuWalls!") }
                                        },
                                        onFailure = { err ->
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Download failed: ${err.localizedMessage}") }
                                        }
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Text(text = "Download", color = Color.White, fontSize = 10.sp)
                    }

                    // Set Wallpaper Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { showSetWallpaperDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Filled.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Set Wallpaper", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Share Deep Link Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                try {
                                    val deepLink = "waifuwalls://wallpaper?id=${wallpaper.id}&url=${wallpaper.url}&sourceApi=${wallpaper.sourceApi}&category=${wallpaper.category}&type=${wallpaper.type}"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Check out this waifu wallpaper!")
                                        putExtra(Intent.EXTRA_TEXT, "Look at this wallpaper on Waifu Walls:\n$deepLink\n\nImage Direct Link:\n${wallpaper.url}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Wallpaper"))
                                } catch (e: Exception) { }
                            }
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Text(text = "Share", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
