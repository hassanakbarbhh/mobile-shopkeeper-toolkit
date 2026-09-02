package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val model: String,
    val imei: String = "",
    val category: ProductCategory,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val quantity: Int,
    val ram: String = "",
    val storage: String = "",
    val color: String = "",
    val condition: ProductCondition = ProductCondition.NEW,
    val warrantyMonths: Int = 0,
    val imageUrl: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ProductCategory {
    SMARTPHONE, FEATURE_PHONE, TABLET, SMARTWATCH,
    CHARGER, CABLE, CASE, SCREEN_PROTECTOR,
    EARPHONE, POWER_BANK, BATTERY, LCD_SPARE_PART,
    REPAIR_TOOL, MEMORY_CARD, SIM_CARD, ACCESSORY, OTHER
}

enum class ProductCondition { NEW, REFURBISHED, USED, OPEN_BOX }
