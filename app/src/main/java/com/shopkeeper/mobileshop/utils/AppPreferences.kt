package com.shopkeeper.mobileshop.utils

import android.content.Context

enum class AppMode { SHOP_OWNER, REPAIR_TECH }

object AppPreferences {
    private const val PREFS = "shop_app_prefs"
    private const val KEY_MODE = "app_mode"
    private const val KEY_REMEMBER = "remember_mode"

    fun saveMode(ctx: Context, mode: AppMode, remember: Boolean) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_MODE, mode.name).putBoolean(KEY_REMEMBER, remember).apply()

    fun getMode(ctx: Context): AppMode? {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.getBoolean(KEY_REMEMBER, false)) return null
        return runCatching { AppMode.valueOf(p.getString(KEY_MODE, "") ?: "") }.getOrNull()
    }

    fun clearMode(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_MODE).apply()
}
