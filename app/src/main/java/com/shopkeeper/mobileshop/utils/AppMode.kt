package com.shopkeeper.mobileshop.utils

import android.content.Context

enum class AppMode(val displayName: String, val roleSubtitle: String, val defaultKey: String) {
    OWNER("App Owner", "👑 Super Admin", "owner123"),
    SHOP_OWNER("Shop Owner", "👑 Shop Owner (Full Master Access)", "Hassanisgreat"),
    SELLER_STAFF("Seller / Staff", "💼 Counter Seller & Cashier Mode", "seller123"),
    REPAIR_TECH("Repair Technician", "🔧 Workshop & Repair Mode", "repair123"),
    BASIC_USER("Basic User", "Pending Approval", "")
}
