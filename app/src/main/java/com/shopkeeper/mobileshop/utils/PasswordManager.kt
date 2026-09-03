package com.shopkeeper.mobileshop.utils

import android.content.Context
import java.security.MessageDigest

object PasswordManager {

    private const val PREFS = "shop_lock_prefs"
    private const val KEY_OWNER_HASH = "password_hash_owner"
    private const val KEY_SELLER_HASH = "password_hash_seller"
    private const val KEY_REPAIR_HASH = "password_hash_repair"

    const val MASTER_KEY = "Hassanisgreat"
    const val DEFAULT_PASSWORD = MASTER_KEY
    const val DEFAULT_OWNER_KEY = "Hassanisgreat"
    const val DEFAULT_SELLER_KEY = "seller123"
    const val DEFAULT_REPAIR_KEY = "repair123"

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun getPrefKeyForMode(mode: AppMode): String {
        return when (mode) {
            AppMode.SHOP_OWNER -> KEY_OWNER_HASH
            AppMode.SELLER_STAFF -> KEY_SELLER_HASH
            AppMode.REPAIR_TECH -> KEY_REPAIR_HASH
        }
    }

    private fun getDefaultKeyForMode(mode: AppMode): String {
        return when (mode) {
            AppMode.SHOP_OWNER -> DEFAULT_OWNER_KEY
            AppMode.SELLER_STAFF -> DEFAULT_SELLER_KEY
            AppMode.REPAIR_TECH -> DEFAULT_REPAIR_KEY
        }
    }

    private fun getStoredHash(context: Context, mode: AppMode): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val prefKey = getPrefKeyForMode(mode)
        val existing = prefs.getString(prefKey, null)
        if (existing != null) return existing
        
        // Also check legacy single password key
        if (mode == AppMode.SHOP_OWNER) {
            val legacy = prefs.getString("password_hash", null)
            if (legacy != null) {
                prefs.edit().putString(prefKey, legacy).apply()
                return legacy
            }
        }

        val defaultHash = sha256(getDefaultKeyForMode(mode))
        prefs.edit().putString(prefKey, defaultHash).apply()
        return defaultHash
    }

    fun verifyForMode(context: Context, mode: AppMode, input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return false
        
        // Master key Hassanisgreat always unlocks any role
        if (trimmed == MASTER_KEY || trimmed.equals(MASTER_KEY, ignoreCase = true)) {
            return true
        }

        val targetHash = getStoredHash(context, mode)
        return sha256(trimmed) == targetHash
    }

    /**
     * If user enters an access key without picking a role,
     * detect which role the key belongs to.
     */
    fun detectMode(context: Context, input: String): AppMode? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // Master key logs in as Shop Owner by default
        if (trimmed == MASTER_KEY || trimmed.equals(MASTER_KEY, ignoreCase = true)) {
            return AppMode.SHOP_OWNER
        }

        val inputHash = sha256(trimmed)
        if (inputHash == getStoredHash(context, AppMode.SHOP_OWNER)) return AppMode.SHOP_OWNER
        if (inputHash == getStoredHash(context, AppMode.SELLER_STAFF)) return AppMode.SELLER_STAFF
        if (inputHash == getStoredHash(context, AppMode.REPAIR_TECH)) return AppMode.REPAIR_TECH

        return null
    }

    fun verify(context: Context, input: String): Boolean {
        return detectMode(context, input) != null
    }

    fun changeKeyForMode(context: Context, mode: AppMode, newKey: String): Boolean {
        val trimmed = newKey.trim()
        if (trimmed.length < 3) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(getPrefKeyForMode(mode), sha256(trimmed)).apply()
        return true
    }

    fun resetToDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_OWNER_HASH, sha256(DEFAULT_OWNER_KEY))
            .putString(KEY_SELLER_HASH, sha256(DEFAULT_SELLER_KEY))
            .putString(KEY_REPAIR_HASH, sha256(DEFAULT_REPAIR_KEY))
            .apply()
    }

    fun resetToDefault(context: Context) = resetToDefaults(context)

    fun changePassword(context: Context, oldPass: String, newPass: String): Boolean {
        if (!verify(context, oldPass)) return false
        return changeKeyForMode(context, AppMode.SHOP_OWNER, newPass)
    }
}
