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
    private const val KEY_THERMAL_HEADER = "thermal_custom_header"
    private const val KEY_THERMAL_FOOTER = "thermal_custom_footer"
    private const val KEY_THERMAL_SHOW_BARCODE = "thermal_show_barcode"
    private const val KEY_THERMAL_SHOW_IMEI = "thermal_show_imei"
    private const val KEY_THERMAL_SHOW_PHONE = "thermal_show_phone"
    private const val KEY_THERMAL_COPIES = "thermal_copies_count"
    private const val KEY_THERMAL_CUSTOM_TEXT = "thermal_custom_text_preset"
    private const val KEY_APP_THEME = "app_theme_choice"
    private const val KEY_NIGHT_MODE = "app_night_mode_choice"

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

    fun getThermalCustomHeader(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THERMAL_HEADER, "TAX INVOICE / CASH MEMO") ?: "TAX INVOICE / CASH MEMO"
    }

    fun setThermalCustomHeader(ctx: Context, header: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_THERMAL_HEADER, header.trim()).apply()
    }

    fun getThermalCustomFooter(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THERMAL_FOOTER, "Thank you for your business!\nNo cash refund without original receipt.\nWarranty valid 3 days.")
            ?: "Thank you for your business!\nNo cash refund without original receipt.\nWarranty valid 3 days."
    }

    fun setThermalCustomFooter(ctx: Context, footer: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_THERMAL_FOOTER, footer.trim()).apply()
    }

    fun isThermalShowBarcode(ctx: Context): Boolean {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_THERMAL_SHOW_BARCODE, true)
    }

    fun setThermalShowBarcode(ctx: Context, show: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_THERMAL_SHOW_BARCODE, show).apply()
    }

    fun isThermalShowImei(ctx: Context): Boolean {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_THERMAL_SHOW_IMEI, true)
    }

    fun setThermalShowImei(ctx: Context, show: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_THERMAL_SHOW_IMEI, show).apply()
    }

    fun isThermalShowPhone(ctx: Context): Boolean {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_THERMAL_SHOW_PHONE, true)
    }

    fun setThermalShowPhone(ctx: Context, show: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_THERMAL_SHOW_PHONE, show).apply()
    }

    fun getThermalCopiesCount(ctx: Context): Int {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_THERMAL_COPIES, 1)
    }

    fun getThermalCopies(ctx: Context): Int = getThermalCopiesCount(ctx)

    fun setThermalCopiesCount(ctx: Context, copies: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_THERMAL_COPIES, copies.coerceIn(1, 5)).apply()
    }

    fun setThermalCopies(ctx: Context, copies: Int) = setThermalCopiesCount(ctx, copies)

    private const val KEY_GITHUB_REPO = "github_repo_slug"

    fun getGitHubRepo(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_GITHUB_REPO, "hassanakbarbhh/shopkeeper") ?: "hassanakbarbhh/shopkeeper"
    }

    fun setGitHubRepo(ctx: Context, repo: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_GITHUB_REPO, repo.trim()).apply()
    }

    fun getThermalCustomTextPreset(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(
            KEY_THERMAL_CUSTOM_TEXT,
            "--- CUSTOM NOTICE ---\nThank you for visiting!\nSmartphones • Repairs • Accessories"
        ) ?: "--- CUSTOM NOTICE ---\nThank you for visiting!\nSmartphones • Repairs • Accessories"
    }

    fun setThermalCustomTextPreset(ctx: Context, text: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_THERMAL_CUSTOM_TEXT, text).apply()
    }

    fun getAppTheme(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_APP_THEME, "EMERALD") ?: "EMERALD"
    }

    fun setAppTheme(ctx: Context, themeName: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_APP_THEME, themeName).apply()
    }

    fun getNightMode(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_NIGHT_MODE, "SYSTEM") ?: "SYSTEM"
    }

    fun setNightMode(ctx: Context, modeName: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_NIGHT_MODE, modeName).apply()
    }
}
