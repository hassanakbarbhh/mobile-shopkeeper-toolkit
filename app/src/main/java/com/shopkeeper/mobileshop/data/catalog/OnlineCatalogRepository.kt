package com.shopkeeper.mobileshop.data.catalog

object OnlineCatalogRepository {

    val allOnlineModels: List<OnlinePhoneModel> = listOf(
        // Apple
        OnlinePhoneModel(
            id = "apple_ip16promax",
            brand = "Apple",
            modelName = "iPhone 16 Pro Max",
            specs = "Apple A18 Pro (3nm), Camera Control button, Grade 5 Titanium frame",
            ramOptions = "8GB RAM",
            storageOptions = "256GB / 512GB / 1TB",
            display = "6.9\" Super Retina XDR OLED, 120Hz ProMotion",
            camera = "48MP Fusion + 48MP Ultra-Wide + 12MP 5x Telephoto",
            battery = "4685 mAh, 25W MagSafe Fast Wireless",
            colors = "Desert Titanium, Natural Titanium, White Titanium, Black Titanium"
        ),
        OnlinePhoneModel(
            id = "apple_ip16pro",
            brand = "Apple",
            modelName = "iPhone 16 Pro",
            specs = "Apple A18 Pro, 4K 120fps Dolby Vision, Action Button",
            ramOptions = "8GB RAM",
            storageOptions = "128GB / 256GB / 512GB / 1TB",
            display = "6.3\" Super Retina XDR OLED, 120Hz ProMotion",
            camera = "48MP Fusion + 48MP Ultra-Wide + 12MP 5x Telephoto",
            battery = "3582 mAh, Fast USB-C Charging",
            colors = "Desert Titanium, Natural, White, Black Titanium"
        ),
        OnlinePhoneModel(
            id = "apple_ip16",
            brand = "Apple",
            modelName = "iPhone 16",
            specs = "Apple A18 (3nm), Camera Control, Action Button",
            ramOptions = "8GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.1\" Super Retina XDR OLED, 2000 nits peak",
            camera = "48MP Fusion Dual Camera with 2x Telephoto + 12MP Ultra-Wide",
            battery = "3561 mAh, Fast USB-C",
            colors = "Ultramarine, Teal, Pink, White, Black"
        ),
        OnlinePhoneModel(
            id = "apple_ip15pro",
            brand = "Apple",
            modelName = "iPhone 15 Pro",
            specs = "Apple A17 Pro (3nm), Aerospace-grade Titanium frame",
            ramOptions = "8GB RAM",
            storageOptions = "128GB / 256GB / 512GB / 1TB",
            display = "6.1\" OLED, 120Hz ProMotion, Always-On",
            camera = "48MP Main + 12MP 3x Telephoto + 12MP Ultra-Wide",
            battery = "3274 mAh, USB-C 3.0",
            colors = "Natural Titanium, Blue Titanium, White Titanium, Black"
        ),
        OnlinePhoneModel(
            id = "apple_ip15",
            brand = "Apple",
            modelName = "iPhone 15",
            specs = "Apple A16 Bionic, Dynamic Island, Color-infused back glass",
            ramOptions = "6GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.1\" Super Retina XDR OLED",
            camera = "48MP Main + 12MP Ultra-Wide with 2x Optical Zoom",
            battery = "3349 mAh, USB-C",
            colors = "Pink, Yellow, Green, Blue, Black"
        ),
        OnlinePhoneModel(
            id = "apple_ip14",
            brand = "Apple",
            modelName = "iPhone 14",
            specs = "Apple A15 Bionic, Ceramic Shield front, Emergency SOS",
            ramOptions = "6GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.1\" Super Retina XDR OLED",
            camera = "12MP Main + 12MP Ultra-Wide",
            battery = "3279 mAh, Lightning Fast Charge",
            colors = "Midnight, Starlight, Blue, Purple, Yellow, (PRODUCT)RED"
        ),

        // Samsung
        OnlinePhoneModel(
            id = "samsung_s24ultra",
            brand = "Samsung",
            modelName = "Galaxy S24 Ultra",
            specs = "Snapdragon 8 Gen 3 for Galaxy, Built-in S-Pen, Titanium Frame, Galaxy AI",
            ramOptions = "12GB RAM",
            storageOptions = "256GB / 512GB / 1TB",
            display = "6.8\" QHD+ Dynamic AMOLED 2X, 120Hz, Gorilla Armor anti-reflective",
            camera = "200MP Main + 50MP 5x Periscope + 10MP 3x + 12MP Ultra-Wide",
            battery = "5000 mAh, 45W Fast Charging",
            colors = "Titanium Gray, Titanium Black, Titanium Violet, Titanium Yellow"
        ),
        OnlinePhoneModel(
            id = "samsung_s24plus",
            brand = "Samsung",
            modelName = "Galaxy S24+",
            specs = "Exynos 2400 / Snapdragon 8 Gen 3, Armor Aluminum, Galaxy AI",
            ramOptions = "12GB RAM",
            storageOptions = "256GB / 512GB",
            display = "6.7\" QHD+ Dynamic AMOLED 2X, 120Hz Adaptive",
            camera = "50MP Main + 10MP 3x Telephoto + 12MP Ultra-Wide",
            battery = "4900 mAh, 45W Fast Charging",
            colors = "Onyx Black, Marble Gray, Cobalt Violet, Amber Yellow"
        ),
        OnlinePhoneModel(
            id = "samsung_s24",
            brand = "Samsung",
            modelName = "Galaxy S24",
            specs = "Exynos 2400 / Snapdragon 8 Gen 3, Compact Flagship, Galaxy AI",
            ramOptions = "8GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.2\" FHD+ Dynamic AMOLED 2X, 120Hz, 2600 nits peak",
            camera = "50MP Main + 10MP 3x Telephoto + 12MP Ultra-Wide",
            battery = "4000 mAh, 25W Fast Charging",
            colors = "Onyx Black, Marble Gray, Cobalt Violet, Amber Yellow"
        ),
        OnlinePhoneModel(
            id = "samsung_zfold6",
            brand = "Samsung",
            modelName = "Galaxy Z Fold 6",
            specs = "Snapdragon 8 Gen 3 for Galaxy, Foldable Dual-Screen, Galaxy AI",
            ramOptions = "12GB RAM",
            storageOptions = "256GB / 512GB / 1TB",
            display = "7.6\" Foldable AMOLED 2X 120Hz + 6.3\" Cover AMOLED 120Hz",
            camera = "50MP Main + 10MP 3x + 12MP Ultra-Wide",
            battery = "4400 mAh, 25W Wired + 15W Wireless",
            colors = "Silver Shadow, Pink, Navy"
        ),
        OnlinePhoneModel(
            id = "samsung_zflip6",
            brand = "Samsung",
            modelName = "Galaxy Z Flip 6",
            specs = "Snapdragon 8 Gen 3, Compact Clamshell, Vapor Chamber cooling",
            ramOptions = "12GB RAM",
            storageOptions = "256GB / 512GB",
            display = "6.7\" Foldable AMOLED 120Hz + 3.4\" FlexWindow Super AMOLED",
            camera = "50MP Main + 12MP Ultra-Wide",
            battery = "4000 mAh, 25W Fast Charging",
            colors = "Blue, Mint, Silver Shadow, Yellow"
        ),
        OnlinePhoneModel(
            id = "samsung_a55",
            brand = "Samsung",
            modelName = "Galaxy A55 5G",
            specs = "Exynos 1480 with AMD Xclipse 530 GPU, Metal Frame, Gorilla Glass Victus+",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.6\" Super AMOLED, 120Hz, 1000 nits HBM",
            camera = "50MP OIS + 12MP Ultra-Wide + 5MP Macro, 32MP Selfie",
            battery = "5000 mAh, 25W Fast Charging",
            colors = "Awesome Iceblue, Awesome Lilac, Awesome Lemon, Awesome Navy"
        ),
        OnlinePhoneModel(
            id = "samsung_a35",
            brand = "Samsung",
            modelName = "Galaxy A35 5G",
            specs = "Exynos 1380 (5nm), IP67 Water Resistant, Knox Vault",
            ramOptions = "6GB / 8GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.6\" Super AMOLED, 120Hz, Vision Booster",
            camera = "50MP OIS Main + 8MP Ultra-Wide + 5MP Macro",
            battery = "5000 mAh, 25W Fast Charging",
            colors = "Awesome Iceblue, Awesome Navy, Awesome Lilac"
        ),
        OnlinePhoneModel(
            id = "samsung_a15",
            brand = "Samsung",
            modelName = "Galaxy A15 4G/5G",
            specs = "MediaTek Helio G99 / Dimensity 6100+, Super AMOLED Display",
            ramOptions = "4GB / 6GB / 8GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.5\" Super AMOLED, 90Hz, 800 nits",
            camera = "50MP Main + 5MP Ultra-Wide + 2MP Macro",
            battery = "5000 mAh, 25W Fast Charging",
            colors = "Brave Black, Optimistic Blue, Magical Blue, Light Blue"
        ),

        // Xiaomi & Redmi
        OnlinePhoneModel(
            id = "xiaomi_14ultra",
            brand = "Xiaomi",
            modelName = "Xiaomi 14 Ultra",
            specs = "Snapdragon 8 Gen 3, Leica Quad 50MP Cameras, Stepless Variable Aperture",
            ramOptions = "16GB RAM",
            storageOptions = "512GB / 1TB",
            display = "6.73\" LTPO AMOLED, 120Hz, 3000 nits peak, WQHD+",
            camera = "50MP 1\" Sony LYT-900 + 50MP 3.2x Tele + 50MP 5x Periscope + 50MP UW",
            battery = "5000 mAh, 90W HyperCharge + 80W Wireless",
            colors = "Black, White, Titanium Edition"
        ),
        OnlinePhoneModel(
            id = "xiaomi_14",
            brand = "Xiaomi",
            modelName = "Xiaomi 14",
            specs = "Snapdragon 8 Gen 3, Leica Summilux Optical lens, Compact flagship",
            ramOptions = "12GB / 16GB RAM",
            storageOptions = "256GB / 512GB",
            display = "6.36\" LTPO OLED, 120Hz, 3000 nits",
            camera = "50MP Light Fusion 900 + 50MP 3.2x Telephoto + 50MP Ultra-Wide",
            battery = "4610 mAh, 90W HyperCharge",
            colors = "Black, White, Jade Green"
        ),
        OnlinePhoneModel(
            id = "redmi_note13proplus",
            brand = "Xiaomi",
            modelName = "Redmi Note 13 Pro+ 5G",
            specs = "MediaTek Dimensity 7200 Ultra (4nm), IP68 Water & Dust Resistance",
            ramOptions = "8GB / 12GB / 16GB RAM",
            storageOptions = "256GB / 512GB",
            display = "6.67\" Curved 1.5K CrystalRes AMOLED, 120Hz, Gorilla Glass Victus",
            camera = "200MP OIS Samsung HP3 + 8MP Ultra-Wide + 2MP Macro",
            battery = "5000 mAh, 120W HyperCharge (100% in 19 mins)",
            colors = "Midnight Black, Moonlight White, Aurora Purple"
        ),
        OnlinePhoneModel(
            id = "redmi_note13",
            brand = "Xiaomi",
            modelName = "Redmi Note 13 4G",
            specs = "Snapdragon 685 (6nm), Ultra-thin bezels, In-display fingerprint",
            ramOptions = "6GB / 8GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.67\" AMOLED, 120Hz, 1800 nits peak",
            camera = "108MP 3x Lossless Zoom + 8MP Ultra-Wide + 2MP Macro",
            battery = "5000 mAh, 33W Fast Charging",
            colors = "Midnight Black, Mint Green, Ice Blue, Ocean Sunset"
        ),
        OnlinePhoneModel(
            id = "poco_x6pro",
            brand = "Xiaomi",
            modelName = "Poco X6 Pro 5G",
            specs = "MediaTek Dimensity 8300 Ultra (4nm), WildBoost Gaming 2.0",
            ramOptions = "8GB / 12GB RAM LPDDR5X",
            storageOptions = "256GB / 512GB UFS 4.0",
            display = "6.67\" CrystalRes 1.5K Flow AMOLED, 120Hz",
            camera = "64MP OIS Triple Camera + 8MP Ultra-Wide + 2MP Macro",
            battery = "5000 mAh, 67W Turbo Charge",
            colors = "Poco Yellow, Black, Grey"
        ),

        // Vivo
        OnlinePhoneModel(
            id = "vivo_v40",
            brand = "Vivo",
            modelName = "Vivo V40 5G",
            specs = "Snapdragon 7 Gen 3, ZEISS Multifocal Portrait, IP68/IP69 rating",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "256GB / 512GB",
            display = "6.78\" 1.5K 3D Curved AMOLED, 120Hz, 4500 nits peak",
            camera = "50MP ZEISS OIS Main + 50MP ZEISS Ultra-Wide + 50MP ZEISS Group Selfie",
            battery = "5500 mAh BlueVolt Battery, 80W FlashCharge",
            colors = "Stellar Silver, Nebula Purple, Ganges Blue"
        ),
        OnlinePhoneModel(
            id = "vivo_v30",
            brand = "Vivo",
            modelName = "Vivo V30 5G",
            specs = "Snapdragon 7 Gen 3 (4nm), Studio-Quality Aura Light 3.0",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.78\" 1.5K Curved AMOLED, 120Hz, 2800 nits",
            camera = "50MP VCS True Color OIS + 50MP Ultra-Wide + 50MP AF Selfie",
            battery = "5000 mAh, 80W FlashCharge",
            colors = "Waving Aqua, Lush Green, Noble Black, Peacock Green"
        ),
        OnlinePhoneModel(
            id = "vivo_y28",
            brand = "Vivo",
            modelName = "Vivo Y28 4G",
            specs = "MediaTek Helio G85, Dual Stereo Speakers with 300% Audio Booster",
            ramOptions = "6GB / 8GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.68\" Sunlight Display, 90Hz, 1000 nits",
            camera = "50MP Main + 2MP Bokeh, 8MP Selfie",
            battery = "6000 mAh Huge Battery, 44W FlashCharge",
            colors = "Agate Green, Gleaming Orange"
        ),

        // Oppo
        OnlinePhoneModel(
            id = "oppo_reno12pro",
            brand = "Oppo",
            modelName = "Reno 12 Pro 5G",
            specs = "MediaTek Dimensity 7300 Energy, GenAI Portrait Eraser, Infinite View Screen",
            ramOptions = "12GB RAM",
            storageOptions = "256GB / 512GB",
            display = "6.7\" Quad-Curved AMOLED, 120Hz, Gorilla Glass Victus 2",
            camera = "50MP Sony LYT-600 OIS + 50MP Telephoto 2x + 8MP UW + 50MP Selfie",
            battery = "5000 mAh, 80W SUPERVOOC",
            colors = "Sunset Gold, Space Brown, Nebula Silver"
        ),
        OnlinePhoneModel(
            id = "oppo_a78",
            brand = "Oppo",
            modelName = "Oppo A78 4G",
            specs = "Snapdragon 680 (6nm), Dual Stereo Speakers, Ultra-Volume Mode",
            ramOptions = "8GB RAM (+8GB Extended)",
            storageOptions = "128GB / 256GB",
            display = "6.43\" FHD+ AMOLED, 90Hz",
            camera = "50MP AI Main + 2MP Depth",
            battery = "5000 mAh, 67W SUPERVOOC",
            colors = "Aqua Green, Mist Black"
        ),

        // Realme
        OnlinePhoneModel(
            id = "realme_12proplus",
            brand = "Realme",
            modelName = "Realme 12 Pro+ 5G",
            specs = "Snapdragon 7s Gen 2, Periscope Portrait Camera, Luxury Watch Design",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.7\" 120Hz Curved Vision OLED, 2160Hz PWM Dimming",
            camera = "64MP Periscope OIS (3x Optical, 120x SuperZoom) + 50MP Sony IMX890 OIS",
            battery = "5000 mAh, 67W SUPERVOOC",
            colors = "Submarine Blue, Navigator Beige, Explorer Red"
        ),
        OnlinePhoneModel(
            id = "realme_c67",
            brand = "Realme",
            modelName = "Realme C67 4G",
            specs = "Snapdragon 685 (6nm), Mini Capsule 2.0, 7.59mm Ultra Slim",
            ramOptions = "6GB / 8GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.72\" FHD+ 90Hz Display",
            camera = "108MP 3x In-Sensor Zoom Camera + 2MP Depth",
            battery = "5000 mAh, 33W SUPERVOOC",
            colors = "Sunny Oasis, Black Rock"
        ),

        // Infinix & Tecno
        OnlinePhoneModel(
            id = "infinix_note40pro",
            brand = "Infinix",
            modelName = "Note 40 Pro 5G",
            specs = "MediaTek Dimensity 7020, All-Round FastCharge 2.0 with MagCharge Wireless",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "256GB",
            display = "6.78\" 3D-Curved AMOLED, 120Hz, Gorilla Glass",
            camera = "108MP OIS Super-Zoom Camera + 2MP + 2MP, 32MP Selfie",
            battery = "5000 mAh, 45W Multi-Speed FastCharge + 20W Wireless MagCharge",
            colors = "Vintage Green, Titan Gold"
        ),
        OnlinePhoneModel(
            id = "tecno_camon30pro",
            brand = "Tecno",
            modelName = "Camon 30 Pro 5G",
            specs = "MediaTek Dimensity 8200 Ultimate, Sony Dual 50MP OIS Camera system",
            ramOptions = "12GB RAM",
            storageOptions = "256GB / 512GB",
            display = "6.78\" AMOLED, 144Hz Refresh Rate",
            camera = "50MP Sony IMX890 OIS + 50MP Ultra-Wide + 50MP Eye-tracking Selfie",
            battery = "5000 mAh, 70W Ultra Charge",
            colors = "Iceland Basaltic Dark, Alps Snowy Silver"
        ),

        // OnePlus & Google
        OnlinePhoneModel(
            id = "oneplus_12",
            brand = "OnePlus",
            modelName = "OnePlus 12",
            specs = "Snapdragon 8 Gen 3, 4th Gen Hasselblad Camera, Dual Cryo-velocity VC",
            ramOptions = "12GB / 16GB / 24GB RAM",
            storageOptions = "256GB / 512GB / 1TB",
            display = "6.82\" 2K ProXDR Display with LTPO 120Hz, 4500 nits peak",
            camera = "50MP Sony LYT-808 OIS + 64MP 3x Periscope + 48MP Ultra-Wide",
            battery = "5400 mAh, 100W SUPERVOOC + 50W AIRVOOC",
            colors = "Silky Black, Flowy Emerald"
        ),
        OnlinePhoneModel(
            id = "oneplus_nord4",
            brand = "OnePlus",
            modelName = "OnePlus Nord 4",
            specs = "Snapdragon 7+ Gen 3, All-metal unibody design, 4 years OS updates",
            ramOptions = "8GB / 12GB / 16GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.74\" 1.5K Super Fluid AMOLED, 120Hz, Ultra HDR",
            camera = "50MP Sony LYT-600 OIS + 8MP Ultra-Wide, 16MP Selfie",
            battery = "5500 mAh, 100W SUPERVOOC",
            colors = "Nordic Hangout, Oasis Green, Obsidian Midnight"
        ),
        OnlinePhoneModel(
            id = "google_pixel9pro",
            brand = "Google",
            modelName = "Pixel 9 Pro XL",
            specs = "Google Tensor G4 (4nm), Google Gemini Nano built-in, 7 years OS updates",
            ramOptions = "16GB RAM",
            storageOptions = "128GB / 256GB / 512GB / 1TB",
            display = "6.8\" Super Actua LTPO OLED 120Hz, 3000 nits peak",
            camera = "50MP Octa PD Main + 48MP 5x Telephoto + 48MP Ultra-Wide, 42MP Selfie",
            battery = "5060 mAh, 37W Wired + Fast Wireless",
            colors = "Obsidian, Porcelain, Hazel, Rose Quartz"
        ),
        OnlinePhoneModel(
            id = "google_pixel8a",
            brand = "Google",
            modelName = "Pixel 8a",
            specs = "Google Tensor G3, Best Take, Magic Audio Eraser, IP67 Water Resistance",
            ramOptions = "8GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.1\" Actua OLED, 120Hz, 2000 nits peak",
            camera = "64MP Quad PD Main + 13MP Ultra-Wide, 13MP Selfie",
            battery = "4492 mAh, Fast Charging + Qi Wireless",
            colors = "Aloe, Bay, Porcelain, Obsidian"
        )
    )

    fun getBrands(): List<String> = listOf("All") + allOnlineModels.map { it.brand }.distinct()

    fun filter(brand: String, query: String): List<OnlinePhoneModel> {
        return allOnlineModels.filter { model ->
            val matchesBrand = brand == "All" || model.brand.equals(brand, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                model.fullName.contains(query, ignoreCase = true) ||
                model.specs.contains(query, ignoreCase = true) ||
                model.display.contains(query, ignoreCase = true) ||
                model.ramOptions.contains(query, ignoreCase = true) ||
                model.storageOptions.contains(query, ignoreCase = true)
            matchesBrand && matchesQuery
        }
    }
}
