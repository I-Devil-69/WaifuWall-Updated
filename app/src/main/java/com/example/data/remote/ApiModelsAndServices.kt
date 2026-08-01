package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

// --- Waifu.im ---
@JsonClass(generateAdapter = true)
data class WaifuImResponse(
    @Json(name = "images") val images: List<WaifuImImage>? = null
)

@JsonClass(generateAdapter = true)
data class WaifuImImage(
    @Json(name = "image_id") val imageId: Long? = null,
    @Json(name = "url") val url: String = "",
    @Json(name = "signature") val signature: String? = null,
    @Json(name = "extension") val extension: String? = null,
    @Json(name = "is_nsfw") val isNsfw: Boolean? = false,
    @Json(name = "width") val width: Int? = 0,
    @Json(name = "height") val height: Int? = 0,
    @Json(name = "dominant_color") val dominantColor: String? = null,
    @Json(name = "source") val source: String? = null,
    @Json(name = "artist") val artist: WaifuImArtist? = null,
    @Json(name = "tags") val tags: List<WaifuImTag>? = null
)

@JsonClass(generateAdapter = true)
data class WaifuImArtist(
    @Json(name = "name") val name: String? = null,
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class WaifuImTag(
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null
)

interface WaifuImApi {
    @GET("search")
    suspend fun search(
        @Query("included_tags") tags: List<String>? = null,
        @Query("is_nsfw") isNsfw: Boolean = false,
        @Query("limit") limit: Int = 30
    ): WaifuImResponse
}

// --- Nekos.best ---
@JsonClass(generateAdapter = true)
data class NekosBestResponse(
    @Json(name = "results") val results: List<NekosBestImage>? = null
)

@JsonClass(generateAdapter = true)
data class NekosBestImage(
    @Json(name = "url") val url: String = "",
    @Json(name = "artist_name") val artistName: String? = null,
    @Json(name = "artist_href") val artistHref: String? = null,
    @Json(name = "source_url") val sourceUrl: String? = null
)

interface NekosBestApi {
    @GET("{category}")
    suspend fun getCategoryImages(
        @Path("category") category: String = "waifu",
        @Query("amount") amount: Int = 20
    ): NekosBestResponse
}

// --- Nekos API ---
@JsonClass(generateAdapter = true)
data class NekosApiResponse(
    @Json(name = "items") val items: List<NekosApiImage>? = null,
    @Json(name = "data") val data: List<NekosApiImage>? = null
)

@JsonClass(generateAdapter = true)
data class NekosApiImage(
    @Json(name = "id") val id: String? = null,
    @Json(name = "url") val url: String = "",
    @Json(name = "rating") val rating: String? = null,
    @Json(name = "artist") val artist: NekosApiArtist? = null,
    @Json(name = "tags") val tags: List<NekosApiTag>? = null
)

@JsonClass(generateAdapter = true)
data class NekosApiArtist(
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class NekosApiTag(
    @Json(name = "name") val name: String? = null
)

interface NekosApi {
    @GET("images/random")
    suspend fun getRandomImages(
        @Query("limit") limit: Int = 20,
        @Query("rating") rating: List<String>? = null
    ): NekosApiResponse
}

// --- Nekosia Cat ---
@JsonClass(generateAdapter = true)
data class NekosiaResponse(
    @Json(name = "image") val image: NekosiaImage? = null,
    @Json(name = "success") val success: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class NekosiaImage(
    @Json(name = "original") val original: NekosiaUrl? = null,
    @Json(name = "compressed") val compressed: NekosiaUrl? = null,
    @Json(name = "id") val id: String? = null,
    @Json(name = "rating") val rating: String? = null,
    @Json(name = "artist") val artist: NekosiaArtist? = null
)

@JsonClass(generateAdapter = true)
data class NekosiaUrl(
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class NekosiaArtist(
    @Json(name = "name") val name: String? = null
)

interface NekosiaCatApi {
    @GET("api/v1/images/catgirl")
    suspend fun getCatgirlImage(): NekosiaResponse

    @GET("api/v1/images/anime")
    suspend fun getAnimeImage(): NekosiaResponse
}

// --- Unsplash ---
@JsonClass(generateAdapter = true)
data class UnsplashResponse(
    @Json(name = "results") val results: List<UnsplashPhoto>? = null
)

@JsonClass(generateAdapter = true)
data class UnsplashPhoto(
    @Json(name = "id") val id: String = "",
    @Json(name = "width") val width: Int? = 0,
    @Json(name = "height") val height: Int? = 0,
    @Json(name = "urls") val urls: UnsplashUrls? = null,
    @Json(name = "user") val user: UnsplashUser? = null
)

@JsonClass(generateAdapter = true)
data class UnsplashUrls(
    @Json(name = "regular") val regular: String? = null,
    @Json(name = "full") val full: String? = null,
    @Json(name = "small") val small: String? = null
)

@JsonClass(generateAdapter = true)
data class UnsplashUser(
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null
)

interface UnsplashApi {
    @GET("search/photos")
    suspend fun searchPhotos(
        @Header("Authorization") authHeader: String,
        @Query("query") query: String = "anime waifu",
        @Query("per_page") perPage: Int = 30
    ): UnsplashResponse
}

// --- Pexels ---
@JsonClass(generateAdapter = true)
data class PexelsResponse(
    @Json(name = "photos") val photos: List<PexelsPhoto>? = null
)

@JsonClass(generateAdapter = true)
data class PexelsPhoto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "width") val width: Int? = 0,
    @Json(name = "height") val height: Int? = 0,
    @Json(name = "url") val url: String? = null,
    @Json(name = "photographer") val photographer: String? = null,
    @Json(name = "src") val src: PexelsSrc? = null
)

@JsonClass(generateAdapter = true)
data class PexelsSrc(
    @Json(name = "large2x") val large2x: String? = null,
    @Json(name = "original") val original: String? = null,
    @Json(name = "medium") val medium: String? = null
)

interface PexelsApi {
    @GET("search")
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String = "anime",
        @Query("per_page") perPage: Int = 30
    ): PexelsResponse
}
