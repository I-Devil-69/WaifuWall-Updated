package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "waifu_walls_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SecurityPreferences", "Failed to create EncryptedSharedPreferences, fallback to standard", e)
            context.getSharedPreferences("waifu_walls_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    fun getUnsplashKey(): String = prefs.getString(KEY_UNSPLASH, "") ?: ""
    fun setUnsplashKey(key: String) = prefs.edit().putString(KEY_UNSPLASH, key).apply()

    fun getPexelsKey(): String = prefs.getString(KEY_PEXELS, "") ?: ""
    fun setPexelsKey(key: String) = prefs.edit().putString(KEY_PEXELS, key).apply()

    fun isNsfwAllowed(): Boolean = prefs.getBoolean(KEY_NSFW_ALLOWED, false)
    fun setNsfwAllowed(allowed: Boolean) = prefs.edit().putBoolean(KEY_NSFW_ALLOWED, allowed).apply()

    fun getSelectedSourceApi(): String = prefs.getString(KEY_SOURCE_API, "All") ?: "All"
    fun setSelectedSourceApi(source: String) = prefs.edit().putString(KEY_SOURCE_API, source).apply()

    companion object {
        private const val KEY_UNSPLASH = "key_unsplash"
        private const val KEY_PEXELS = "key_pexels"
        private const val KEY_NSFW_ALLOWED = "key_nsfw_allowed"
        private const val KEY_SOURCE_API = "key_source_api"
    }
}
