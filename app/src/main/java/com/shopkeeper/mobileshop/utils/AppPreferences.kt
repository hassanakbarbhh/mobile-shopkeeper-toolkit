package com.shopkeeper.mobileshop.utils

import android.content.Context

enum class AppMode(val displayName: String, val roleSubtitle: String, val defaultKey: String) {
    SHOP_OWNER("Shop Owner", "👑 Shop Owner (Full Master Access)", "Hassanisgreat"),
    SELLER_STAFF("Seller / Staff", "💼 Counter Seller & Cashier Mode", "seller123"),
    REPAIR_TECH("Repair Technician", "🔧 Workshop & Repair Mode", "repair123")
}

object AppPreferences {
    private const val PREFS = "shop_app_prefs"
    private const val KEY_MODE = "app_mode"
    private const val KEY_REMEMBER = "remember_mode"
    private const val KEY_LOCK_ENABLED = "lock_security_enabled"
    private const val KEY_DEV_PHOTO = "developer_custom_photo_path"
    private const val KEY_THERMAL_WIDTH = "thermal_paper_width_mm"

    fun saveMode(ctx: Context, mode: AppMode, remember: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_MODE, mode.name)
            .putBoolean(KEY_REMEMBER, remember)
            .apply()
    }

    fun getMode(ctx: Context): AppMode? {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.getBoolean(KEY_REMEMBER, false)) return null
        return runCatching { AppMode.valueOf(p.getString(KEY_MODE, "") ?: "") }.getOrNull()
    }

    fun getActiveMode(ctx: Context): AppMode {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val modeStr = p.getString(KEY_MODE, AppMode.SHOP_OWNER.name) ?: AppMode.SHOP_OWNER.name
        return runCatching { AppMode.valueOf(modeStr) }.getOrDefault(AppMode.SHOP_OWNER)
    }

    fun setActiveMode(ctx: Context, mode: AppMode) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_MODE, mode.name)
            .apply()
    }

    fun clearMode(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .remove(KEY_MODE)
            .remove(KEY_REMEMBER)
            .apply()
    }

    fun isLockEnabled(ctx: Context): Boolean {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LOCK_ENABLED, false)
    }

    fun setLockEnabled(ctx: Context, enabled: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_LOCK_ENABLED, enabled).apply()
    }

    fun getDeveloperPhotoPath(ctx: Context): String? {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DEV_PHOTO, null)
    }

    fun setDeveloperPhotoPath(ctx: Context, path: String?) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_DEV_PHOTO, path).apply()
    }

    fun getThermalPaperWidth(ctx: Context): Int {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_THERMAL_WIDTH, 58)
    }

    fun setThermalPaperWidth(ctx: Context, widthMm: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_THERMAL_WIDTH, widthMm).apply()
    }
}
