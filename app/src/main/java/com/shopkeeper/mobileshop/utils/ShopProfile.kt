package com.shopkeeper.mobileshop.utils

import android.content.Context

object ShopProfile {
    private const val PREFS = "shop_profile"

    fun save(ctx: Context, name: String, phone: String, address: String) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("name", name).putString("phone", phone)
            .putString("address", address).apply()

    fun name(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString("name", "Mobile Inventory Toolkit")!!
    fun phone(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString("phone", "")!!
    fun address(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString("address", "")!!
}
