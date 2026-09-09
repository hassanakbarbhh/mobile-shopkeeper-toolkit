package com.shopkeeper.mobileshop.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {
    private const val PREFS_FILE = "secure_auth_tokens"
    private const val KEY_GITHUB_TOKEN = "github_access_token"
    private const val KEY_AUTH_METADATA = "auth_metadata"

    private fun getSecurePrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveGitHubToken(context: Context, token: String) {
        getSecurePrefs(context).edit().putString(KEY_GITHUB_TOKEN, token).apply()
    }

    fun getGitHubToken(context: Context): String? {
        return getSecurePrefs(context).getString(KEY_GITHUB_TOKEN, null)
    }

    fun saveAuthMetadata(context: Context, metadata: String) {
        getSecurePrefs(context).edit().putString(KEY_AUTH_METADATA, metadata).apply()
    }

    fun getAuthMetadata(context: Context): String? {
        return getSecurePrefs(context).getString(KEY_AUTH_METADATA, null)
    }

    fun clearTokens(context: Context) {
        getSecurePrefs(context).edit().clear().apply()
    }
}
