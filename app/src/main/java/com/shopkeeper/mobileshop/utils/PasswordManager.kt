package com.shopkeeper.mobileshop.utils

import android.content.Context
import com.shopkeeper.mobileshop.security.SecureStorage

object PasswordManager {

    private const val PREFS = "shop_lock_prefs"
    private const val KEY_OWNER_HASH = "password_hash_owner"
    private const val KEY_SELLER_HASH = "password_hash_seller"
    private const val KEY_REPAIR_HASH = "password_hash_repair"

    private fun getPrefKeyForMode(mode: AppMode): String {
        return when (mode) {
            AppMode.SHOP_OWNER -> KEY_OWNER_HASH
            AppMode.SELLER_STAFF -> KEY_SELLER_HASH
            AppMode.REPAIR_TECH -> KEY_REPAIR_HASH
        }
    }

    private fun getStoredHash(context: Context, mode: AppMode): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val prefKey = getPrefKeyForMode(mode)
        return prefs.getString(prefKey, null)
    }

    fun isConfiguredForMode(context: Context, mode: AppMode): Boolean {
        return !getStoredHash(context, mode).isNullOrBlank()
    }

    fun verifyForMode(context: Context, mode: AppMode, input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return false

        val storedHash = getStoredHash(context, mode) ?: return false
        return SecureStorage.verifyPassword(context, trimmed, storedHash)
    }

    fun changeKeyForMode(context: Context, mode: AppMode, newKey: String): Boolean {
        val trimmed = newKey.trim()
        if (trimmed.length < 4) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saltedHash = SecureStorage.hashPassword(context, trimmed)
        prefs.edit().putString(getPrefKeyForMode(mode), saltedHash).apply()
        return true
    }

    fun clearAllKeys(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_OWNER_HASH)
            .remove(KEY_SELLER_HASH)
            .remove(KEY_REPAIR_HASH)
            .apply()
    }

    fun resetToDefaults(context: Context) = clearAllKeys(context)
    fun resetToDefault(context: Context) = clearAllKeys(context)
}

