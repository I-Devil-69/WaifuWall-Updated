package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DownloadHelper {
    private const val CHANNEL_ID = "waifu_walls_downloads"
    private const val CHANNEL_NAME = "Wallpaper Downloads"

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when wallpapers finish downloading"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun downloadWallpaper(
        context: Context,
        imageUrl: String,
        fileName: String = "Waifu_${System.currentTimeMillis()}"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            ensureNotificationChannel(context)

            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result !is SuccessResult) {
                return@withContext Result.failure(Exception("Failed to download image file"))
            }

            val bitmap = (result.drawable as BitmapDrawable).bitmap
            val uri = saveBitmapToGallery(context, bitmap, fileName)

            if (uri != null) {
                showDownloadNotification(context, fileName, uri)
                Result.success(uri)
            } else {
                Result.failure(Exception("Failed to save image to gallery"))
            }
        } catch (e: Exception) {
            Log.e("DownloadHelper", "Error downloading wallpaper", e)
            Result.failure(e)
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val cleanName = "${fileName.replace("[^a-zA-Z0-9_]".toRegex(), "_")}.jpg"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/WaifuWalls")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
            }
            uri
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val waifuDir = File(picturesDir, "WaifuWalls")
            if (!waifuDir.exists()) waifuDir.mkdirs()

            val file = File(waifuDir, cleanName)
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            Uri.fromFile(file)
        }
    }

    private fun showDownloadNotification(context: Context, fileName: String, uri: Uri) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Wallpaper Downloaded")
                .setContentText("Saved $fileName to Pictures/WaifuWalls")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        } catch (e: Exception) {
            Log.e("DownloadHelper", "Failed to post notification", e)
        }
    }
}
