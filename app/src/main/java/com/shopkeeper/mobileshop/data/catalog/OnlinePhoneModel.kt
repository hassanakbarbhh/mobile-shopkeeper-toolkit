package com.shopkeeper.mobileshop.data.catalog

import com.shopkeeper.mobileshop.data.db.entity.ProductCategory

data class OnlinePhoneModel(
    val id: String,
    val brand: String,
    val modelName: String,
    val specs: String,
    val ramOptions: String,
    val storageOptions: String,
    val display: String,
    val camera: String,
    val battery: String,
    val colors: String,
    val marketStatus: String = "Online Catalog • No Stock • Price Not Set",
    val category: ProductCategory = ProductCategory.SMARTPHONE
) {
    val fullName: String get() = "$brand $modelName"

    fun toProductNoPrice(): com.shopkeeper.mobileshop.data.db.entity.Product {
        return com.shopkeeper.mobileshop.data.db.entity.Product(
            name = fullName,
            brand = brand,
            model = modelName,
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 0.0,
            sellingPrice = 0.0,
            quantity = 0,
            ram = ramOptions,
            storage = storageOptions,
            color = colors,
            condition = com.shopkeeper.mobileshop.data.db.entity.ProductCondition.NEW,
            warrantyMonths = 12,
            notes = "Specs: $specs • Display: $display • Cam: $camera"
        )
    }

    fun toShareableSpecs(): String {
        return """
            📱 $brand $modelName
            • Display: $display
            • RAM Variants: $ramOptions
            • Storage Variants: $storageOptions
            • Camera Setup: $camera
            • Battery & Charging: $battery
            • Available Colors: $colors
            
            ⚠️ Status: Online Model (Request a quote or order in advance)
        """.trimIndent()
    }
}
