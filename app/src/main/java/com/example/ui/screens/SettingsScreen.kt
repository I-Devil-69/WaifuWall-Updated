package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NsfwWarningDialog
import com.example.ui.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WallpaperViewModel
) {
    val settingsState by viewModel.settingsState.collectAsState()

    var unsplashInput by remember(settingsState.unsplashKey) { mutableStateOf(settingsState.unsplashKey) }
    var pexelsInput by remember(settingsState.pexelsKey) { mutableStateOf(settingsState.pexelsKey) }

    var unsplashVisible by remember { mutableStateOf(false) }
    var pexelsVisible by remember { mutableStateOf(false) }

    var showNsfwWarning by remember { mutableStateOf(false) }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(settingsState.statusMessage) {
        settingsState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    if (showNsfwWarning) {
        NsfwWarningDialog(
            onConfirm = {
                viewModel.setNsfwAllowed(true)
                showNsfwWarning = false
            },
            onDismiss = { showNsfwWarning = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("App Settings & Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // --- Section 1: API Keys (EncryptedSharedPreferences) ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Key, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Optional API Keys (Stored Encrypted)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Text(
                        "APIs like Waifu.im and Nekos.best work out-of-the-box without keys. You can optionally add keys for Unsplash or Pexels below.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Unsplash Key Field
                    OutlinedTextField(
                        value = unsplashInput,
                        onValueChange = { unsplashInput = it },
                        label = { Text("Unsplash Access Key") },
                        singleLine = true,
                        visualTransformation = if (unsplashVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { unsplashVisible = !unsplashVisible }) {
                                Icon(
                                    imageVector = if (unsplashVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle key visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.saveUnsplashKey(unsplashInput.trim()) },
                        enabled = !settingsState.isTestingKey && unsplashInput.isNotBlank(),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (settingsState.isTestingKey) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Verify & Save Unsplash Key")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Pexels Key Field
                    OutlinedTextField(
                        value = pexelsInput,
                        onValueChange = { pexelsInput = it },
                        label = { Text("Pexels API Key") },
                        singleLine = true,
                        visualTransformation = if (pexelsVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { pexelsVisible = !pexelsVisible }) {
                                Icon(
                                    imageVector = if (pexelsVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle key visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.savePexelsKey(pexelsInput.trim()) },
                        enabled = !settingsState.isTestingKey && pexelsInput.isNotBlank(),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (settingsState.isTestingKey) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Verify & Save Pexels Key")
                    }
                }
            }

            // --- Section 2: Preferred Default Source API & Safety ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Feed & Safety Preferences", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Source Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Primary Source API", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Choose default source for home feed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        val apis = listOf("All", "Waifu.im", "Nekos.best", "Nekos API", "Nekosia Cat", "Unsplash", "Pexels")
                        ExposedDropdownMenuBox(
                            expanded = sourceDropdownExpanded,
                            onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded }
                        ) {
                            OutlinedCard(
                                onClick = { sourceDropdownExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                            ) {
                                Text(
                                    text = settingsState.selectedSourceApi,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }

                            ExposedDropdownMenu(
                                expanded = sourceDropdownExpanded,
                                onDismissRequest = { sourceDropdownExpanded = false }
                            ) {
                                apis.forEach { api ->
                                    DropdownMenuItem(
                                        text = { Text(api) },
                                        onClick = {
                                            viewModel.setPreferredSourceApi(api)
                                            sourceDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // NSFW Safety Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Adult (NSFW) Content", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Requires age verification. Default is Safe For Work.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Switch(
                            checked = settingsState.nsfwAllowed,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showNsfwWarning = true
                                } else {
                                    viewModel.setNsfwAllowed(false)
                                }
                            }
                        )
                    }
                }
            }

            // --- Section 3: Caching & Memory ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Image Cache & Performance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Disk Cache", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Disk space occupied by Coil image cache", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(
                            text = settingsState.cacheSizeMb,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearCoilCache() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Clear Image Cache")
                    }
                }
            }

            // --- Section 4: About & Open Source Licenses ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("About Waifu Walls", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Text("Version 1.0 (Build 2026.08)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Built with Kotlin, Jetpack Compose, Material You Monet theme, Room DB, Retrofit, and Coil.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Data APIs: Waifu.im, Nekos.best, Nekos API, Nekosia Cat, Unsplash & Pexels.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
