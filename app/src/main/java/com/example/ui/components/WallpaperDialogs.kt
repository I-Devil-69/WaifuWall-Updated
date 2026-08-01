package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Wallpaper
import com.example.util.WallpaperTarget

@Composable
fun SetWallpaperDialog(
    onDismiss: () -> Unit,
    onSelectTarget: (WallpaperTarget) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Apply Wallpaper", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select where you want to set this wallpaper:", fontSize = 14.sp)

                OutlinedButton(
                    onClick = { onSelectTarget(WallpaperTarget.HOME) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Home Screen")
                }

                OutlinedButton(
                    onClick = { onSelectTarget(WallpaperTarget.LOCK) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lock Screen")
                }

                Button(
                    onClick = { onSelectTarget(WallpaperTarget.BOTH) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Both Home & Lock Screen")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WallpaperInfoDialog(
    wallpaper: Wallpaper,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text("Wallpaper Metadata", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(label = "ID", value = wallpaper.id)
                InfoRow(label = "Source API", value = wallpaper.sourceApi)
                InfoRow(label = "Category", value = wallpaper.category)
                InfoRow(label = "Rating", value = wallpaper.type.uppercase())

                if (wallpaper.width > 0 && wallpaper.height > 0) {
                    InfoRow(label = "Dimensions", value = "${wallpaper.width} x ${wallpaper.height}")
                }

                if (wallpaper.tags.isNotEmpty()) {
                    InfoRow(label = "Tags", value = wallpaper.tags.take(6).joinToString(", "))
                }

                if (!wallpaper.artist.isNull_or_blank()) {
                    InfoRow(label = "Artist", value = wallpaper.artist ?: "")
                }

                if (!wallpaper.artistUrl.isNull_or_blank()) {
                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wallpaper.artistUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) { }
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Visit Artist Source", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

@Composable
fun NsfwWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red)
        },
        title = {
            Text("Age Verification & Safety Warning", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "You are about to enable adult (NSFW) wallpaper content. " +
                "By enabling this, you confirm that you are at least 18 years old or of legal age in your jurisdiction. " +
                "Always respect local content safety guidelines.",
                fontSize = 13.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("I Confirm (Enable)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
