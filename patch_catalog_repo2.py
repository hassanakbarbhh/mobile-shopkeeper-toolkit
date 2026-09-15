with open("app/src/main/java/com/shopkeeper/mobileshop/data/catalog/OnlineCatalogRepository.kt", "w") as f:
    f.write("""package com.shopkeeper.mobileshop.data.catalog

object OnlineCatalogRepository {
    // There are no open, unauthenticated, free API databases for global smartphones.
    // Instead of using a fake mock API (like dummyjson), this app uses a built-in,
    // offline pre-loaded database of popular smartphones.
    val allOnlineModels = listOf(
        // Apple
        OnlinePhoneModel(
            id = "iphone_15_pro_max",
            brand = "Apple",
            modelName = "iPhone 15 Pro Max",
            specs = "A17 Pro chip, Titanium design, Action button",
            ramOptions = "8GB RAM",
            storageOptions = "256GB / 512GB / 1TB",
            display = "6.7\\" Super Retina XDR OLED, 120Hz ProMotion",
            camera = "48MP Main + 12MP Ultra-Wide + 12MP 5x Telephoto",
            battery = "4422 mAh, MagSafe wireless charging",
            colors = "Natural Titanium, Blue Titanium, White Titanium, Black Titanium"
        ),
        OnlinePhoneModel(
            id = "iphone_13",
            brand = "Apple",
            modelName = "iPhone 13",
            specs = "A15 Bionic chip, Ceramic Shield",
            ramOptions = "4GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.1\\" Super Retina XDR OLED",
            camera = "12MP Main + 12MP Ultra-Wide",
            battery = "3240 mAh, MagSafe wireless charging",
            colors = "Starlight, Midnight, Blue, Pink, Green"
        ),
        
        // Samsung
        OnlinePhoneModel(
            id = "samsung_s24_ultra",
            brand = "Samsung",
            modelName = "Galaxy S24 Ultra",
            specs = "Snapdragon 8 Gen 3 for Galaxy, Galaxy AI, Titanium frame, Built-in S Pen",
            ramOptions = "12GB RAM",
            storageOptions = "256GB / 512GB / 1TB",
            display = "6.8\\" Dynamic AMOLED 2X, 120Hz, 2600 nits peak, Gorilla Armor",
            camera = "200MP Main + 50MP 5x Telephoto + 10MP 3x Telephoto + 12MP Ultra-Wide",
            battery = "5000 mAh, 45W wired, 15W wireless",
            colors = "Titanium Gray, Titanium Black, Titanium Violet, Titanium Yellow"
        ),
        OnlinePhoneModel(
            id = "samsung_a55",
            brand = "Samsung",
            modelName = "Galaxy A55 5G",
            specs = "Exynos 1480, Knox Vault, Premium metal frame",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.6\\" Super AMOLED, 120Hz, 1000 nits (HBM)",
            camera = "50MP OIS Main + 12MP Ultra-Wide + 5MP Macro, 32MP Selfie",
            battery = "5000 mAh, 25W fast charging",
            colors = "Awesome Iceblue, Awesome Lilac, Awesome Navy, Awesome Lemon"
        ),

        // Vivo
        OnlinePhoneModel(
            id = "vivo_x100_pro",
            brand = "Vivo",
            modelName = "Vivo X100 Pro",
            specs = "MediaTek Dimensity 9300, ZEISS APO Floating Telephoto Camera",
            ramOptions = "16GB RAM",
            storageOptions = "512GB",
            display = "6.78\\" LTPO AMOLED, 120Hz, 3000 nits peak",
            camera = "50MP 1-inch Main + 50MP ZEISS APO Telephoto + 50MP Ultra-Wide",
            battery = "5400 mAh, 100W FlashCharge, 50W Wireless FlashCharge",
            colors = "Startrail Blue, Asteroid Black, Sunset Orange"
        ),
        
        // Oppo
        OnlinePhoneModel(
            id = "oppo_reno12pro",
            brand = "Oppo",
            modelName = "Reno 12 Pro 5G",
            specs = "MediaTek Dimensity 7300 Energy, GenAI Portrait Eraser, Infinite View Screen",
            ramOptions = "12GB RAM",
            storageOptions = "256GB / 512GB",
            display = "6.7\\" Quad-Curved AMOLED, 120Hz, Gorilla Glass Victus 2",
            camera = "50MP Sony LYT-600 OIS + 50MP Telephoto 2x + 8MP UW + 50MP Selfie",
            battery = "5000 mAh, 80W SUPERVOOC",
            colors = "Sunset Gold, Space Brown, Nebula Silver"
        ),
        
        // Realme
        OnlinePhoneModel(
            id = "realme_12proplus",
            brand = "Realme",
            modelName = "Realme 12 Pro+ 5G",
            specs = "Snapdragon 7s Gen 2, Periscope Portrait Camera, Luxury Watch Design",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.7\\" 120Hz Curved Vision OLED, 2160Hz PWM Dimming",
            camera = "64MP Periscope OIS (3x Optical, 120x SuperZoom) + 50MP Sony IMX890 OIS",
            battery = "5000 mAh, 67W SUPERVOOC",
            colors = "Submarine Blue, Navigator Beige, Explorer Red"
        ),
        
        // Infinix
        OnlinePhoneModel(
            id = "infinix_note40pro",
            brand = "Infinix",
            modelName = "Note 40 Pro 5G",
            specs = "MediaTek Dimensity 7020, All-Round FastCharge 2.0 with MagCharge Wireless",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "256GB",
            display = "6.78\\" 3D-Curved AMOLED, 120Hz, Gorilla Glass",
            camera = "108MP OIS Super-Zoom Camera + 2MP + 2MP, 32MP Selfie",
            battery = "5000 mAh, 45W Multi-Speed FastCharge + 20W Wireless MagCharge",
            colors = "Vintage Green, Titan Gold"
        ),

        // OnePlus
        OnlinePhoneModel(
            id = "oneplus_12",
            brand = "OnePlus",
            modelName = "OnePlus 12",
            specs = "Snapdragon 8 Gen 3, 4th Gen Hasselblad Camera, Dual Cryo-velocity VC",
            ramOptions = "12GB / 16GB / 24GB RAM",
            storageOptions = "256GB / 512GB / 1TB",
            display = "6.82\\" 2K ProXDR Display with LTPO 120Hz, 4500 nits peak",
            camera = "50MP Sony LYT-808 OIS + 64MP 3x Periscope + 48MP Ultra-Wide",
            battery = "5400 mAh, 100W SUPERVOOC + 50W AIRVOOC",
            colors = "Silky Black, Flowy Emerald"
        ),
        
        // Google
        OnlinePhoneModel(
            id = "google_pixel9pro",
            brand = "Google",
            modelName = "Pixel 9 Pro XL",
            specs = "Google Tensor G4 (4nm), Google Gemini Nano built-in, 7 years OS updates",
            ramOptions = "16GB RAM",
            storageOptions = "128GB / 256GB / 512GB / 1TB",
            display = "6.8\\" Super Actua LTPO OLED 120Hz, 3000 nits peak",
            camera = "50MP Octa PD Main + 48MP 5x Telephoto + 48MP Ultra-Wide, 42MP Selfie",
            battery = "5060 mAh, 37W Wired + Fast Wireless",
            colors = "Obsidian, Porcelain, Hazel, Rose Quartz"
        )
    )

    fun getBrands(): List<String> = listOf("All") + allOnlineModels.map { it.brand }.distinct()

    fun filter(brand: String, query: String): List<OnlinePhoneModel> {
        return allOnlineModels.filter { model ->
            val matchesBrand = brand == "All" || model.brand.equals(brand, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                model.fullName.contains(query, ignoreCase = true) ||
                model.specs.contains(query, ignoreCase = true)
            matchesBrand && matchesQuery
        }
    }
}
""")
