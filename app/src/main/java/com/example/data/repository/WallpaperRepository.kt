package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteWallpaperEntity
import com.example.data.model.Wallpaper
import com.example.data.remote.*
import com.example.data.security.SecurityPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

class WallpaperRepository(
    private val context: Context,
    val securityPrefs: SecurityPreferences = SecurityPreferences(context)
) {
    private val db = AppDatabase.getDatabase(context)
    private val favoriteDao = db.favoriteDao()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", "WaifuWallsNative/1.0 (Android; Kotlin)")
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val waifuImApi: WaifuImApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.waifu.im/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WaifuImApi::class.java)
    }

    private val nekosBestApi: NekosBestApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.nekos.best/api/v2/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NekosBestApi::class.java)
    }

    private val nekosApi: NekosApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.nekosapi.com/v5/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NekosApi::class.java)
    }

    private val nekosiaCatApi: NekosiaCatApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.nekosia.cat/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NekosiaCatApi::class.java)
    }

    private val unsplashApi: UnsplashApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UnsplashApi::class.java)
    }

    private val pexelsApi: PexelsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.pexels.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PexelsApi::class.java)
    }

    // Room Favorites
    val favoritesFlow: Flow<List<Wallpaper>> = favoriteDao.getAllFavorites().map { entities ->
        entities.map { it.toDomain() }
    }

    fun isFavoriteFlow(id: String): Flow<Boolean> = favoriteDao.isFavoriteFlow(id)

    suspend fun toggleFavorite(wallpaper: Wallpaper): Boolean = withContext(Dispatchers.IO) {
        val isFav = favoriteDao.isFavorite(wallpaper.id)
        if (isFav) {
            favoriteDao.deleteFavoriteById(wallpaper.id)
            false
        } else {
            favoriteDao.insertFavorite(FavoriteWallpaperEntity.fromDomain(wallpaper))
            true
        }
    }

    suspend fun fetchWallpapers(
        tags: List<String> = emptyList(),
        isNsfw: Boolean = false,
        sourceApi: String = "All",
        limit: Int = 30
    ): Result<List<Wallpaper>> = withContext(Dispatchers.IO) {
        try {
            val list = mutableListOf<Wallpaper>()

            when (sourceApi) {
                "Waifu.im" -> {
                    list.addAll(fetchFromWaifuIm(tags, isNsfw, limit))
                }
                "Nekos.best" -> {
                    list.addAll(fetchFromNekosBest(tags, limit))
                }
                "Nekos API" -> {
                    list.addAll(fetchFromNekosApi(tags, isNsfw, limit))
                }
                "Nekosia Cat" -> {
                    list.addAll(fetchFromNekosiaCat(limit))
                }
                "Unsplash" -> {
                    list.addAll(fetchFromUnsplash(tags))
                }
                "Pexels" -> {
                    list.addAll(fetchFromPexels(tags))
                }
                else -> { // "All" - Fetch concurrently with fallback
                    coroutineScope {
                        val waifuImDeferred = async { runCatching { fetchFromWaifuIm(tags, isNsfw, limit) }.getOrDefault(emptyList()) }
                        val nekosBestDeferred = async { runCatching { fetchFromNekosBest(tags, limit) }.getOrDefault(emptyList()) }
                        val nekosApiDeferred = async { runCatching { fetchFromNekosApi(tags, isNsfw, limit) }.getOrDefault(emptyList()) }

                        val unsplashKey = securityPrefs.getUnsplashKey()
                        val unsplashDeferred = async {
                            if (unsplashKey.isNotBlank()) {
                                runCatching { fetchFromUnsplash(tags) }.getOrDefault(emptyList())
                            } else emptyList()
                        }

                        val pexelsKey = securityPrefs.getPexelsKey()
                        val pexelsDeferred = async {
                            if (pexelsKey.isNotBlank()) {
                                runCatching { fetchFromPexels(tags) }.getOrDefault(emptyList())
                            } else emptyList()
                        }

                        list.addAll(waifuImDeferred.await())
                        list.addAll(nekosBestDeferred.await())
                        list.addAll(nekosApiDeferred.await())
                        list.addAll(unsplashDeferred.await())
                        list.addAll(pexelsDeferred.await())
                    }

                    // Fallback to Nekosia Cat if primary APIs produced empty results
                    if (list.isEmpty()) {
                        runCatching { list.addAll(fetchFromNekosiaCat(5)) }
                    }
                }
            }

            // Shuffle results for fresh feel
            Result.success(list.shuffled())
        } catch (e: Exception) {
            Log.e("WallpaperRepository", "Error fetching wallpapers", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchFromWaifuIm(tags: List<String>, isNsfw: Boolean, limit: Int): List<Wallpaper> {
        val cleanTags = tags.map { it.lowercase().trim() }.filter { it.isNotBlank() }
        val response = waifuImApi.search(
            tags = if (cleanTags.isNotEmpty()) cleanTags else null,
            isNsfw = isNsfw,
            limit = limit
        )

        return response.images?.map { img ->
            val tagNames = img.tags?.mapNotNull { it.name } ?: emptyList()
            Wallpaper(
                id = "waifu_im_${img.imageId ?: UUID.randomUUID().hashCode()}",
                url = img.url,
                previewUrl = img.url,
                type = if (img.isNsfw == true) "nsfw" else "sfw",
                tags = tagNames,
                category = tagNames.firstOrNull() ?: "waifu",
                sourceApi = "Waifu.im",
                artist = img.artist?.name,
                artistUrl = img.artist?.url ?: img.source,
                extension = img.extension ?: "jpg",
                width = img.width ?: 1080,
                height = img.height ?: 1920
            )
        } ?: emptyList()
    }

    private suspend fun fetchFromNekosBest(tags: List<String>, limit: Int): List<Wallpaper> {
        val validCategories = listOf("waifu", "neko", "kitsune", "hug", "kiss", "pat", "smile", "cuddle", "dance", "wave")
        val category = tags.firstOrNull { it.lowercase() in validCategories }?.lowercase() ?: "waifu"

        val response = nekosBestApi.getCategoryImages(category = category, amount = limit.coerceAtMost(20))
        return response.results?.map { img ->
            val uniqueId = img.url.hashCode().toString()
            Wallpaper(
                id = "nekos_best_$uniqueId",
                url = img.url,
                previewUrl = img.url,
                type = "sfw",
                tags = listOf(category, "anime"),
                category = category,
                sourceApi = "Nekos.best",
                artist = img.artistName,
                artistUrl = img.artistHref ?: img.sourceUrl,
                extension = "png",
                width = 1080,
                height = 1920
            )
        } ?: emptyList()
    }

    private suspend fun fetchFromNekosApi(tags: List<String>, isNsfw: Boolean, limit: Int): List<Wallpaper> {
        val ratingList = if (isNsfw) listOf("suggestive", "erotica", "hentai") else listOf("safe")
        val response = nekosApi.getRandomImages(limit = limit, rating = ratingList)
        val items = response.items ?: response.data ?: emptyList()

        return items.map { img ->
            val tagNames = img.tags?.mapNotNull { it.name } ?: listOf("anime")
            Wallpaper(
                id = "nekos_api_${img.id ?: UUID.randomUUID().toString()}",
                url = img.url,
                previewUrl = img.url,
                type = if (img.rating == "safe") "sfw" else "nsfw",
                tags = tagNames,
                category = tagNames.firstOrNull() ?: "anime",
                sourceApi = "Nekos API",
                artist = img.artist?.name,
                extension = "jpg",
                width = 1080,
                height = 1920
            )
        }
    }

    private suspend fun fetchFromNekosiaCat(count: Int = 3): List<Wallpaper> {
        val list = mutableListOf<Wallpaper>()
        for (i in 0 until count) {
            val response = try {
                nekosiaCatApi.getCatgirlImage()
            } catch (e: Exception) {
                nekosiaCatApi.getAnimeImage()
            }
            response.image?.let { img ->
                val imgUrl = img.original?.url ?: img.compressed?.url ?: ""
                if (imgUrl.isNotBlank()) {
                    list.add(
                        Wallpaper(
                            id = "nekosia_${img.id ?: UUID.randomUUID().toString()}",
                            url = imgUrl,
                            previewUrl = img.compressed?.url ?: imgUrl,
                            type = if (img.rating == "nsfw") "nsfw" else "sfw",
                            tags = listOf("neko", "catgirl", "anime"),
                            category = "neko",
                            sourceApi = "Nekosia Cat",
                            artist = img.artist?.name,
                            extension = "jpg",
                            width = 1080,
                            height = 1920
                        )
                    )
                }
            }
        }
        return list
    }

    private suspend fun fetchFromUnsplash(tags: List<String>): List<Wallpaper> {
        val key = securityPrefs.getUnsplashKey()
        if (key.isBlank()) return emptyList()

        val query = if (tags.isNotEmpty()) "anime ${tags.first()}" else "anime waifu"
        val response = unsplashApi.searchPhotos("Client-ID $key", query = query, perPage = 30)

        return response.results?.map { photo ->
            val imgUrl = photo.urls?.full ?: photo.urls?.regular ?: ""
            Wallpaper(
                id = "unsplash_${photo.id}",
                url = imgUrl,
                previewUrl = photo.urls?.small ?: imgUrl,
                type = "sfw",
                tags = listOf("unsplash", "anime", "wallpaper"),
                category = "unsplash",
                sourceApi = "Unsplash",
                artist = photo.user?.name ?: photo.user?.username,
                extension = "jpg",
                width = photo.width ?: 1080,
                height = photo.height ?: 1920
            )
        } ?: emptyList()
    }

    private suspend fun fetchFromPexels(tags: List<String>): List<Wallpaper> {
        val key = securityPrefs.getPexelsKey()
        if (key.isBlank()) return emptyList()

        val query = if (tags.isNotEmpty()) "anime ${tags.first()}" else "anime artwork"
        val response = pexelsApi.searchPhotos(key, query = query, perPage = 30)

        return response.photos?.map { photo ->
            val imgUrl = photo.src?.large2x ?: photo.src?.original ?: ""
            Wallpaper(
                id = "pexels_${photo.id}",
                url = imgUrl,
                previewUrl = photo.src?.medium ?: imgUrl,
                type = "sfw",
                tags = listOf("pexels", "anime", "wallpaper"),
                category = "pexels",
                sourceApi = "Pexels",
                artist = photo.photographer,
                extension = "jpg",
                width = photo.width ?: 1080,
                height = photo.height ?: 1920
            )
        } ?: emptyList()
    }

    // Testing API Keys for Settings Screen
    suspend fun testUnsplashKey(key: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = unsplashApi.searchPhotos("Client-ID $key", query = "anime", perPage = 1)
            if (response.results != null) {
                securityPrefs.setUnsplashKey(key)
                Result.success(true)
            } else {
                Result.failure(Exception("Invalid response from Unsplash"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testPexelsKey(key: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = pexelsApi.searchPhotos(key, query = "anime", perPage = 1)
            if (response.photos != null) {
                securityPrefs.setPexelsKey(key)
                Result.success(true)
            } else {
                Result.failure(Exception("Invalid response from Pexels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
