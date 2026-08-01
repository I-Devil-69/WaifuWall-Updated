package com.example.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class WallpaperTarget {
    HOME, LOCK, BOTH
}

object WallpaperManagerHelper {

    suspend fun setWallpaper(
        context: Context,
        imageUrl: String,
        target: WallpaperTarget
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Required to get accessible bitmap
                .build()

            val result = loader.execute(request)
            if (result !is SuccessResult) {
                return@withContext Result.failure(Exception("Failed to download wallpaper image for setting"))
            }

            val bitmap = (result.drawable as BitmapDrawable).bitmap
            val wallpaperManager = WallpaperManager.getInstance(context)

            val flag = when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, flag)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WallpaperManagerHelper", "Error setting wallpaper", e)
            Result.failure(e)
        }
    }
}
