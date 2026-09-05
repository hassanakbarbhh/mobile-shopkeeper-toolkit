package com.shopkeeper.mobileshop.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.shopkeeper.mobileshop.R

enum class AppTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val styleRes: Int,
    val primaryColorRes: Int,
    val badgeIcon: String
) {
    EMERALD(
        id = "EMERALD",
        title = "Emerald Green",
        subtitle = "Classic store & inventory pro look",
        styleRes = R.style.Theme_MobileShopkeeper,
        primaryColorRes = R.color.green_800,
        badgeIcon = "🌿"
    ),
    MIDNIGHT(
        id = "MIDNIGHT",
        title = "Midnight AMOLED",
        subtitle = "Pitch black OLED battery saver",
        styleRes = R.style.Theme_MobileShopkeeper_Midnight,
        primaryColorRes = R.color.amoled_primary,
        badgeIcon = "🌙"
    ),
    SAPPHIRE(
        id = "SAPPHIRE",
        title = "Royal Sapphire Blue",
        subtitle = "Corporate modern tech POS look",
        styleRes = R.style.Theme_MobileShopkeeper_Sapphire,
        primaryColorRes = R.color.sapphire_800,
        badgeIcon = "💎"
    ),
    SUNSET(
        id = "SUNSET",
        title = "Sunset Amber & Gold",
        subtitle = "Warm luxury retail & electronics",
        styleRes = R.style.Theme_MobileShopkeeper_Sunset,
        primaryColorRes = R.color.sunset_800,
        badgeIcon = "🌅"
    ),
    PURPLE(
        id = "PURPLE",
        title = "Cyberpunk Neon Violet",
        subtitle = "Modern electric gadget theme",
        styleRes = R.style.Theme_MobileShopkeeper_Purple,
        primaryColorRes = R.color.violet_800,
        badgeIcon = "⚡"
    ),
    SLATE(
        id = "SLATE",
        title = "Titanium Slate",
        subtitle = "Industrial sleek minimalist grey",
        styleRes = R.style.Theme_MobileShopkeeper_Slate,
        primaryColorRes = R.color.slate_800,
        badgeIcon = "⚙️"
    )
}

enum class NightModeOption(val id: String, val title: String, val modeValue: Int) {
    SYSTEM("SYSTEM", "Auto (Follow System)", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT("LIGHT", "Light Mode Always", AppCompatDelegate.MODE_NIGHT_NO),
    DARK("DARK", "Dark Mode Always", AppCompatDelegate.MODE_NIGHT_YES)
}

object ThemeManager {

    fun getCurrentTheme(context: Context): AppTheme {
        val savedName = AppPreferences.getAppTheme(context)
        return runCatching { AppTheme.valueOf(savedName) }.getOrDefault(AppTheme.EMERALD)
    }

    fun getCurrentNightMode(context: Context): NightModeOption {
        val savedMode = AppPreferences.getNightMode(context)
        return runCatching { NightModeOption.valueOf(savedMode) }.getOrDefault(NightModeOption.SYSTEM)
    }

    fun applyTheme(activity: Activity) {
        val theme = getCurrentTheme(activity)
        activity.setTheme(theme.styleRes)
    }

    fun applyNightMode(context: Context) {
        val mode = getCurrentNightMode(context)
        AppCompatDelegate.setDefaultNightMode(mode.modeValue)
    }

    fun setTheme(activity: Activity, theme: AppTheme) {
        AppPreferences.setAppTheme(activity, theme.name)
        activity.recreate()
    }

    fun setNightMode(context: Context, mode: NightModeOption) {
        AppPreferences.setNightMode(context, mode.name)
        AppCompatDelegate.setDefaultNightMode(mode.modeValue)
    }
}
