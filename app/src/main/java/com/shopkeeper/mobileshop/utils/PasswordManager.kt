package com.shopkeeper.mobileshop.utils

import android.content.Context
import java.security.MessageDigest

object PasswordManager {

    private const val PREFS = "shop_lock_prefs"
    private const val KEY_HASH = "password_hash"
    private const val DEFAULT_PASSWORD = "Hassanisgreat"

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun storedHash(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_HASH, null)
        if (existing != null) return existing
        val default = sha256(DEFAULT_PASSWORD)
        prefs.edit().putString(KEY_HASH, default).apply()
        return default
    }

    fun verify(context: Context, input: String): Boolean =
        sha256(input) == storedHash(context)

    fun changePassword(context: Context, current: String, new: String): Boolean {
        if (!verify(context, current)) return false
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_HASH, sha256(new)).apply()
        return true
    }
}
