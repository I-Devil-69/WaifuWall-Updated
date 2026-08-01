package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.example.data.model.Wallpaper
import com.example.data.repository.WallpaperRepository
import com.example.util.DownloadHelper
import com.example.util.WallpaperManagerHelper
import com.example.util.WallpaperTarget
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class HomeUiState(
    val wallpapers: List<Wallpaper> = emptyList(),
    val selectedTag: String = "waifu",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SearchUiState(
    val query: String = "",
    val selectedTags: List<String> = listOf("waifu"),
    val isNsfw: Boolean = false,
    val selectedSourceApi: String = "All",
    val results: List<Wallpaper> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)

data class SettingsUiState(
    val unsplashKey: String = "",
    val pexelsKey: String = "",
    val nsfwAllowed: Boolean = false,
    val selectedSourceApi: String = "All",
    val cacheSizeMb: String = "0 MB",
    val statusMessage: String? = null,
    val isTestingKey: Boolean = false
)

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {
    val repository = WallpaperRepository(application)

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()

    // Room DB Favorites
    val favoritesState: StateFlow<List<Wallpaper>> = repository.favoritesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadSettings()
        loadHomeFeed()
        calculateCacheSize()
    }

    private fun loadSettings() {
        val prefs = repository.securityPrefs
        _settingsState.update {
            it.copy(
                unsplashKey = prefs.getUnsplashKey(),
                pexelsKey = prefs.getPexelsKey(),
                nsfwAllowed = prefs.isNsfwAllowed(),
                selectedSourceApi = prefs.getSelectedSourceApi()
            )
        }
    }

    fun loadHomeFeed(refresh: Boolean = false) {
        viewModelScope.launch {
            if (_homeState.value.isLoading && !refresh) return@launch
            _homeState.update { it.copy(isLoading = true, errorMessage = null) }

            val tag = _homeState.value.selectedTag
            val isNsfw = repository.securityPrefs.isNsfwAllowed()
            val source = repository.securityPrefs.getSelectedSourceApi()

            val result = repository.fetchWallpapers(
                tags = if (tag == "all") emptyList() else listOf(tag),
                isNsfw = isNsfw,
                sourceApi = source,
                limit = 30
            )

            result.fold(
                onSuccess = { newItems ->
                    _homeState.update { state ->
                        val combined = if (refresh) newItems else (state.wallpapers + newItems).distinctBy { it.id }
                        state.copy(wallpapers = combined, isLoading = false)
                    }
                },
                onFailure = { error ->
                    _homeState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Failed to load wallpapers") }
                }
            )
        }
    }

    fun setHomeTag(tag: String) {
        _homeState.update { it.copy(selectedTag = tag, wallpapers = emptyList()) }
        loadHomeFeed(refresh = true)
    }

    fun updateSearchQuery(query: String) {
        _searchState.update { it.copy(query = query) }
    }

    fun toggleSearchTag(tag: String) {
        _searchState.update { state ->
            val tags = state.selectedTags.toMutableList()
            if (tags.contains(tag)) {
                if (tags.size > 1) tags.remove(tag)
            } else {
                tags.add(tag)
            }
            state.copy(selectedTags = tags)
        }
    }

    fun setSearchNsfw(isNsfw: Boolean) {
        _searchState.update { it.copy(isNsfw = isNsfw) }
    }

    fun setSearchSourceApi(source: String) {
        _searchState.update { it.copy(selectedSourceApi = source) }
    }

    fun performSearch() {
        viewModelScope.launch {
            _searchState.update { it.copy(isSearching = true, errorMessage = null) }
            val state = _searchState.value

            val searchTags = mutableListOf<String>()
            searchTags.addAll(state.selectedTags)
            if (state.query.isNotBlank()) {
                searchTags.add(state.query.trim().lowercase())
            }

            val result = repository.fetchWallpapers(
                tags = searchTags,
                isNsfw = state.isNsfw,
                sourceApi = state.selectedSourceApi,
                limit = 30
            )

            result.fold(
                onSuccess = { items ->
                    _searchState.update { it.copy(results = items, isSearching = false) }
                },
                onFailure = { error ->
                    _searchState.update { it.copy(isSearching = false, errorMessage = error.localizedMessage ?: "Search failed") }
                }
            )
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.toggleFavorite(wallpaper)
        }
    }

    fun isFavoriteFlow(id: String): Flow<Boolean> = repository.isFavoriteFlow(id)

    // Settings actions
    fun setNsfwAllowed(allowed: Boolean) {
        repository.securityPrefs.setNsfwAllowed(allowed)
        _settingsState.update { it.copy(nsfwAllowed = allowed, statusMessage = "NSFW safety setting updated") }
    }

    fun setPreferredSourceApi(source: String) {
        repository.securityPrefs.setSelectedSourceApi(source)
        _settingsState.update { it.copy(selectedSourceApi = source, statusMessage = "Preferred source updated") }
    }

    fun saveUnsplashKey(key: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(isTestingKey = true, statusMessage = null) }
            val result = repository.testUnsplashKey(key)
            result.fold(
                onSuccess = {
                    _settingsState.update { it.copy(unsplashKey = key, isTestingKey = false, statusMessage = "Unsplash API key verified and saved!") }
                },
                onFailure = { err ->
                    _settingsState.update { it.copy(isTestingKey = false, statusMessage = "Unsplash key verification failed: ${err.localizedMessage}") }
                }
            )
        }
    }

    fun savePexelsKey(key: String) {
        viewModelScope.launch {
            _settingsState.update { it.copy(isTestingKey = true, statusMessage = null) }
            val result = repository.testPexelsKey(key)
            result.fold(
                onSuccess = {
                    _settingsState.update { it.copy(pexelsKey = key, isTestingKey = false, statusMessage = "Pexels API key verified and saved!") }
                },
                onFailure = { err ->
                    _settingsState.update { it.copy(isTestingKey = false, statusMessage = "Pexels key verification failed: ${err.localizedMessage}") }
                }
            )
        }
    }

    fun clearCoilCache() {
        viewModelScope.launch {
            try {
                val imageLoader = ImageLoader(getApplication())
                imageLoader.diskCache?.clear()
                imageLoader.memoryCache?.clear()
                calculateCacheSize()
                _settingsState.update { it.copy(statusMessage = "Cache cleared successfully") }
            } catch (e: Exception) {
                Log.e("WallpaperViewModel", "Error clearing cache", e)
            }
        }
    }

    private fun calculateCacheSize() {
        viewModelScope.launch {
            try {
                val cacheDir = File(getApplication<Application>().cacheDir, "image_cache")
                var sizeBytes = 0L
                if (cacheDir.exists()) {
                    cacheDir.walkTopDown().forEach { file ->
                        if (file.isFile) sizeBytes += file.length()
                    }
                }
                val mb = String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
                _settingsState.update { it.copy(cacheSizeMb = mb) }
            } catch (e: Exception) {
                _settingsState.update { it.copy(cacheSizeMb = "0.0 MB") }
            }
        }
    }

    fun clearStatusMessage() {
        _settingsState.update { it.copy(statusMessage = null) }
    }

    // Wallpaper action calls
    fun applyWallpaper(imageUrl: String, target: WallpaperTarget, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = WallpaperManagerHelper.setWallpaper(getApplication(), imageUrl, target)
            onResult(result)
        }
    }

    fun downloadWallpaper(imageUrl: String, fileName: String, onResult: (Result<Uri>) -> Unit) {
        viewModelScope.launch {
            val result = DownloadHelper.downloadWallpaper(getApplication(), imageUrl, fileName)
            onResult(result)
        }
    }
}
