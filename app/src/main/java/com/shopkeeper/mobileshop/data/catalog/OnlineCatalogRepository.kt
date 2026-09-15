package com.shopkeeper.mobileshop.data.catalog

object OnlineCatalogRepository {
    val allOnlineModels: List<OnlinePhoneModel> = listOf(
        // ==========================================
        // 1. APPLE (5 models)
        // ==========================================
        OnlinePhoneModel(
            id = "apple_iphone_15_pro_max",
            brand = "Apple",
            modelName = "iPhone 15 Pro Max",
            specs = "A17 Pro 3nm chip, Aerospace-grade Titanium frame, Action Button, USB-C 3.0",
            ramOptions = "8GB Unified RAM",
            storageOptions = "256GB / 512GB / 1TB NVMe",
            display = "6.7\" Super Retina XDR OLED, 120Hz ProMotion, 2000 nits peak, Always-On",
            camera = "48MP Main (OIS) + 12MP 5x Periscope Telephoto + 12MP Ultra-Wide + LiDAR",
            battery = "4422 mAh, 20W Fast Wired, 15W MagSafe Wireless",
            colors = "Natural Titanium, Blue Titanium, White Titanium, Black Titanium"
        ),
        OnlinePhoneModel(
            id = "apple_iphone_15_pro",
            brand = "Apple",
            modelName = "iPhone 15 Pro",
            specs = "A17 Pro 3nm chip, Titanium design, Dynamic Island, Action Button",
            ramOptions = "8GB Unified RAM",
            storageOptions = "128GB / 256GB / 512GB / 1TB NVMe",
            display = "6.1\" Super Retina XDR OLED, 120Hz ProMotion, 2000 nits peak",
            camera = "48MP Main + 12MP 3x Telephoto + 12MP Ultra-Wide + LiDAR",
            battery = "3274 mAh, 20W Fast Wired, 15W MagSafe Wireless",
            colors = "Natural Titanium, Blue Titanium, White Titanium, Black Titanium"
        ),
        OnlinePhoneModel(
            id = "apple_iphone_15",
            brand = "Apple",
            modelName = "iPhone 15",
            specs = "A16 Bionic chip, Dynamic Island, Color-infused back glass",
            ramOptions = "6GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.1\" Super Retina XDR OLED, 2000 nits peak",
            camera = "48MP Main with 2x Telephoto + 12MP Ultra-Wide",
            battery = "3349 mAh, 20W Fast Charging, MagSafe",
            colors = "Black, Blue, Green, Yellow, Pink"
        ),
        OnlinePhoneModel(
            id = "apple_iphone_14",
            brand = "Apple",
            modelName = "iPhone 14",
            specs = "A15 Bionic (5-core GPU), Crash Detection, Ceramic Shield",
            ramOptions = "6GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.1\" Super Retina XDR OLED, HDR10, 1200 nits peak",
            camera = "12MP Main (sensor-shift OIS) + 12MP Ultra-Wide",
            battery = "3279 mAh, 20W Fast Wired, 15W MagSafe",
            colors = "Midnight, Starlight, Blue, Purple, Red, Yellow"
        ),
        OnlinePhoneModel(
            id = "apple_iphone_13",
            brand = "Apple",
            modelName = "iPhone 13",
            specs = "A15 Bionic chip, Cinematic mode 1080p, Ceramic Shield",
            ramOptions = "4GB RAM",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.1\" Super Retina XDR OLED, 800 nits (1200 nits HDR)",
            camera = "12MP Dual Pixel Main + 12MP Ultra-Wide",
            battery = "3240 mAh, 20W Fast Wired, 15W MagSafe",
            colors = "Starlight, Midnight, Blue, Pink, Green, Red"
        ),

        // ==========================================
        // 2. SAMSUNG (5 models)
        // ==========================================
        OnlinePhoneModel(
            id = "samsung_s24_ultra",
            brand = "Samsung",
            modelName = "Galaxy S24 Ultra",
            specs = "Snapdragon 8 Gen 3 for Galaxy, Galaxy AI, Titanium frame, Built-in S Pen",
            ramOptions = "12GB LPDDR5X",
            storageOptions = "256GB / 512GB / 1TB UFS 4.0",
            display = "6.8\" Dynamic AMOLED 2X, 1-120Hz LTPO, 2600 nits peak, Gorilla Armor",
            camera = "200MP Main OIS + 50MP 5x Periscope + 10MP 3x Telephoto + 12MP Ultra-Wide",
            battery = "5000 mAh, 45W Wired, 15W Wireless, 4.5W Reverse",
            colors = "Titanium Gray, Titanium Black, Titanium Violet, Titanium Yellow"
        ),
        OnlinePhoneModel(
            id = "samsung_s24_plus",
            brand = "Samsung",
            modelName = "Galaxy S24+",
            specs = "Exynos 2400 / Snapdragon 8 Gen 3, Galaxy AI suite, Armor Aluminum 2",
            ramOptions = "12GB LPDDR5X",
            storageOptions = "256GB / 512GB UFS 4.0",
            display = "6.7\" Dynamic AMOLED 2X, QHD+, 1-120Hz LTPO, 2600 nits",
            camera = "50MP Dual Pixel OIS + 10MP 3x Telephoto + 12MP Ultra-Wide",
            battery = "4900 mAh, 45W Wired, 15W Wireless",
            colors = "Onyx Black, Marble Gray, Cobalt Violet, Amber Yellow"
        ),
        OnlinePhoneModel(
            id = "samsung_s24",
            brand = "Samsung",
            modelName = "Galaxy S24",
            specs = "Compact Flagship, Galaxy AI, Armor Aluminum frame, IP68",
            ramOptions = "8GB LPDDR5X",
            storageOptions = "128GB / 256GB / 512GB",
            display = "6.2\" Dynamic AMOLED 2X, FHD+, 1-120Hz LTPO, 2600 nits",
            camera = "50MP Main OIS + 10MP 3x Telephoto + 12MP Ultra-Wide",
            battery = "4000 mAh, 25W Wired, 15W Wireless",
            colors = "Onyx Black, Marble Gray, Cobalt Violet, Amber Yellow"
        ),
        OnlinePhoneModel(
            id = "samsung_a55",
            brand = "Samsung",
            modelName = "Galaxy A55 5G",
            specs = "Exynos 1480 with AMD Xclipse 530 GPU, Metal Frame, Samsung Knox Vault",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB (microSD up to 1TB)",
            display = "6.6\" Super AMOLED, 120Hz, 1000 nits HBM, Gorilla Glass Victus+",
            camera = "50MP OIS Main + 12MP Ultra-Wide + 5MP Macro, 32MP Front",
            battery = "5000 mAh, 25W Fast Charging",
            colors = "Awesome Iceblue, Awesome Lilac, Awesome Navy, Awesome Lemon"
        ),
        OnlinePhoneModel(
            id = "samsung_a35",
            brand = "Samsung",
            modelName = "Galaxy A35 5G",
            specs = "Exynos 1380 (5nm), Knox Vault, IP67 water/dust resistance",
            ramOptions = "6GB / 8GB RAM",
            storageOptions = "128GB / 256GB (microSD expandable)",
            display = "6.6\" Super AMOLED, 120Hz, 1000 nits HBM",
            camera = "50MP OIS Main + 8MP Ultra-Wide + 5MP Macro",
            battery = "5000 mAh, 25W Fast Charging",
            colors = "Awesome Navy, Awesome Iceblue, Awesome Lilac, Awesome Lemon"
        ),

        // ==========================================
        // 3. XIAOMI (4 models)
        // ==========================================
        OnlinePhoneModel(
            id = "xiaomi_14_ultra",
            brand = "Xiaomi",
            modelName = "Xiaomi 14 Ultra",
            specs = "Snapdragon 8 Gen 3, Leica Quad 50MP Cameras, Stepless Variable Aperture",
            ramOptions = "16GB LPDDR5X",
            storageOptions = "512GB / 1TB UFS 4.0",
            display = "6.73\" LTPO AMOLED, 1-120Hz, WQHD+, 3000 nits peak, Dolby Vision",
            camera = "50MP 1-inch LYT-900 OIS + 50MP 3.2x OIS + 50MP 5x Periscope OIS + 50MP Ultra-Wide",
            battery = "5000 mAh, 90W HyperCharge, 80W Wireless HyperCharge",
            colors = "Black, White"
        ),
        OnlinePhoneModel(
            id = "xiaomi_14",
            brand = "Xiaomi",
            modelName = "Xiaomi 14",
            specs = "Snapdragon 8 Gen 3, Compact Leica Optics, Xiaomi HyperOS",
            ramOptions = "12GB / 16GB RAM",
            storageOptions = "256GB / 512GB UFS 4.0",
            display = "6.36\" LTPO OLED, 1-120Hz, 3000 nits peak, Ultra-thin bezels",
            camera = "50MP Light Fusion 900 OIS + 50MP 75mm Floating Telephoto + 50MP Ultra-Wide",
            battery = "4610 mAh, 90W Wired HyperCharge, 50W Wireless",
            colors = "Black, White, Jade Green"
        ),
        OnlinePhoneModel(
            id = "xiaomi_redmi_note_13_pro_plus",
            brand = "Xiaomi",
            modelName = "Redmi Note 13 Pro+ 5G",
            specs = "MediaTek Dimensity 7200-Ultra (4nm), 200MP OIS, IP68 Waterproof",
            ramOptions = "8GB / 12GB / 16GB RAM",
            storageOptions = "256GB / 512GB UFS 3.1",
            display = "6.67\" 1.5K 3D Curved AMOLED, 120Hz, 1800 nits peak, Gorilla Glass Victus",
            camera = "200MP Samsung ISOCELL HP3 OIS + 8MP Ultra-Wide + 2MP Macro",
            battery = "5000 mAh, 120W HyperCharge (100% in 19 mins)",
            colors = "Midnight Black, Moonlight White, Aurora Purple"
        ),
        OnlinePhoneModel(
            id = "xiaomi_redmi_note_13",
            brand = "Xiaomi",
            modelName = "Redmi Note 13 4G",
            specs = "Snapdragon 685 (6nm), 108MP Triple Camera, In-display Fingerprint",
            ramOptions = "6GB / 8GB RAM",
            storageOptions = "128GB / 256GB UFS 2.2",
            display = "6.67\" AMOLED, 120Hz, 1800 nits peak, Ultra-slim bezel",
            camera = "108MP 3x Lossless Zoom + 8MP Ultra-Wide + 2MP Macro",
            battery = "5000 mAh, 33W Fast Charging",
            colors = "Midnight Black, Mint Green, Ice Blue, Ocean Sunset"
        ),

        // ==========================================
        // 4. INFINIX (4 models)
        // ==========================================
        OnlinePhoneModel(
            id = "infinix_note_40_pro",
            brand = "Infinix",
            modelName = "Note 40 Pro 5G",
            specs = "Dimensity 7020 5G, Cheetah X1 Power Management, Active Halo AI Lighting",
            ramOptions = "8GB / 12GB LPDDR4X",
            storageOptions = "256GB UFS 2.2",
            display = "6.78\" 3D Curved AMOLED, 120Hz, 1300 nits peak, Corning Gorilla Glass",
            camera = "108MP OIS 3x Super-Zoom + 2MP Macro + 2MP Depth, 32MP Selfie",
            battery = "5000 mAh, 45W Multi-Speed FastCharge, 20W Wireless MagCharge",
            colors = "Vintage Green, Titan Gold"
        ),
        OnlinePhoneModel(
            id = "infinix_gt_20_pro",
            brand = "Infinix",
            modelName = "GT 20 Pro 5G",
            specs = "Dimensity 8200 Ultimate 4nm, Dedicated Gaming Display Chip Pixelworks X5 Turbo",
            ramOptions = "8GB / 12GB LPDDR5X",
            storageOptions = "256GB UFS 3.1",
            display = "6.78\" Bezel-less AMOLED, 144Hz Gaming Refresh Rate, 1300 nits",
            camera = "108MP OIS Main + 2MP Macro + 2MP Depth, 32MP Front",
            battery = "5000 mAh, 45W Hyper Charge, Bypass Charging 2.0",
            colors = "Mecha Blue, Mecha Orange, Mecha Silver"
        ),
        OnlinePhoneModel(
            id = "infinix_zero_30_5g",
            brand = "Infinix",
            modelName = "Zero 30 5G",
            specs = "Dimensity 8020 (6nm), 4K 60FPS Front Video Vlog Camera",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "256GB UFS 3.1",
            display = "6.78\" 3D Curved AMOLED, 144Hz, 950 nits peak",
            camera = "108MP OIS Main + 13MP Ultra-Wide + 2MP, 50MP 4K60 Front",
            battery = "5000 mAh, 68W Super Charge (80% in 30 mins)",
            colors = "Rome Green, Golden Hour, Fantasy Purple"
        ),
        OnlinePhoneModel(
            id = "infinix_hot_40_pro",
            brand = "Infinix",
            modelName = "Hot 40 Pro",
            specs = "MediaTek Helio G99 (6nm), Magic Ring Notification, XBOOST Gaming Engine",
            ramOptions = "8GB RAM (+8GB Extended)",
            storageOptions = "128GB / 256GB",
            display = "6.78\" FHD+ IPS LCD, 120Hz, Touch Sampling 1200Hz",
            camera = "108MP Main + 2MP Macro + AI Lens, 32MP Front",
            battery = "5000 mAh, 33W Enduring FastCharge",
            colors = "Horizon Gold, Starlit Black, Palm Blue, Starfall Green"
        ),

        // ==========================================
        // 5. TECNO (4 models)
        // ==========================================
        OnlinePhoneModel(
            id = "tecno_camon_30_pro",
            brand = "Tecno",
            modelName = "Camon 30 Pro 5G",
            specs = "Dimensity 8200 Ultimate, Sony IMX890 50MP OIS 1/1.56\", Dual 50MP System",
            ramOptions = "12GB LPDDR5X",
            storageOptions = "256GB / 512GB UFS 3.1",
            display = "6.78\" AMOLED, 144Hz, 1.5K Resolution, Wet Touch Control",
            camera = "50MP Sony IMX890 OIS + 50MP Ultra-Wide + 2MP Depth, 50MP Eye-AF Selfie",
            battery = "5000 mAh, 70W Ultra Charge",
            colors = "Iceland Basalt Dark, Alps Snowy Silver"
        ),
        OnlinePhoneModel(
            id = "tecno_phantom_x2_pro",
            brand = "Tecno",
            modelName = "Phantom X2 Pro 5G",
            specs = "Dimensity 9000 Flagship, World First Retractable Portrait Lens",
            ramOptions = "12GB LPDDR5X",
            storageOptions = "256GB UFS 3.1",
            display = "6.8\" Curved AMOLED, 120Hz, Gorilla Glass Victus",
            camera = "50MP Retractable Portrait (f/1.49) + 50MP Main + 13MP Ultra-Wide",
            battery = "5160 mAh, 45W Flash Charge",
            colors = "Mars Orange, Stardust Gray"
        ),
        OnlinePhoneModel(
            id = "tecno_spark_20_pro_plus",
            brand = "Tecno",
            modelName = "Spark 20 Pro+",
            specs = "MediaTek Helio G99 Ultimate 6nm, Dynamic Port, Dual Stereo Speakers DTS",
            ramOptions = "8GB (+8GB Virtual RAM)",
            storageOptions = "256GB",
            display = "6.78\" Curved AMOLED, 120Hz, 1000 nits peak, Gorilla Glass 5",
            camera = "108MP Ultra Sensing Main + Quad Flash, 32MP Glowing Selfie",
            battery = "5000 mAh, 33W Super Charge",
            colors = "Temporal Orbits, Lunar Frost, Radiant Starstream, Magic Skin 2.0 Green"
        ),
        OnlinePhoneModel(
            id = "tecno_pova_6_pro",
            brand = "Tecno",
            modelName = "Pova 6 Pro 5G",
            specs = "Dimensity 6080 5G, Dynamic-Light Mecha Design with 210 Mini LEDs",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "256GB",
            display = "6.78\" FHD+ AMOLED, 120Hz, 1300 nits peak",
            camera = "108MP 10x In-Sensor Zoom Main + 2MP Light Sensor + AI Lens",
            battery = "6000 mAh Mega Battery, 70W Ultra Charge + 10W Reverse",
            colors = "Comet Green, Meteorite Grey"
        ),

        // ==========================================
        // 6. VIVO (4 models)
        // ==========================================
        OnlinePhoneModel(
            id = "vivo_x100_pro",
            brand = "Vivo",
            modelName = "Vivo X100 Pro",
            specs = "Dimensity 9300 (All Big Core), ZEISS 1-inch Main Camera, V3 Imaging Chip",
            ramOptions = "16GB LPDDR5T",
            storageOptions = "512GB / 1TB UFS 4.0",
            display = "6.78\" LTPO AMOLED, 120Hz, 3000 nits peak, 2160Hz PWM Dimming",
            camera = "50MP 1-inch Sony IMX989 OIS + 50MP ZEISS APO Telephoto OIS + 50MP Ultra-Wide",
            battery = "5400 mAh BlueOcean Battery, 100W Dual-Cell FlashCharge, 50W Wireless",
            colors = "Startrail Blue, Asteroid Black, Sunset Orange"
        ),
        OnlinePhoneModel(
            id = "vivo_v30_pro",
            brand = "Vivo",
            modelName = "Vivo V30 Pro 5G",
            specs = "MediaTek Dimensity 8200 4nm, ZEISS Triple 50MP Main Cameras, Studio Aura Light",
            ramOptions = "12GB LPDDR5X",
            storageOptions = "512GB UFS 3.1",
            display = "6.78\" 1.5K Curved 3D AMOLED, 120Hz, 2800 nits peak",
            camera = "50MP Sony IMX920 OIS + 50MP ZEISS Telephoto 2x + 50MP Ultra-Wide, 50MP AF Front",
            battery = "5000 mAh, 80W FlashCharge (7.45mm Ultra-Slim Body)",
            colors = "Andaman Blue, Classic Black"
        ),
        OnlinePhoneModel(
            id = "vivo_v30",
            brand = "Vivo",
            modelName = "Vivo V30 5G",
            specs = "Snapdragon 7 Gen 3 (4nm), Upgraded Smart Aura Light Portrait, IP54",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB / 512GB UFS 2.2",
            display = "6.78\" 1.5K 3D Curved AMOLED, 120Hz, 2800 nits peak",
            camera = "50MP VCS True Color OIS + 50MP 119° Ultra-Wide, 50MP AF Group Selfie",
            battery = "5000 mAh, 80W FlashCharge",
            colors = "Peacock Green, Waving Aqua, Lush Green, Noble Black"
        ),
        OnlinePhoneModel(
            id = "vivo_y200e",
            brand = "Vivo",
            modelName = "Vivo Y200e 5G",
            specs = "Snapdragon 4 Gen 2 (4nm), EcoFiber Leather Back with Anti-Stain Coating",
            ramOptions = "6GB / 8GB LPDDR4X",
            storageOptions = "128GB UFS 2.2 (expandable 1TB)",
            display = "6.67\" FHD+ AMOLED, 120Hz, 1200 nits HBM, Dual Stereo Speakers 300%",
            camera = "50MP Main + 2MP Bokeh + Flicker Sensor, 16MP Selfie",
            battery = "5000 mAh, 44W FlashCharge",
            colors = "Saffron Delight (Leather), Black Onyx"
        ),

        // ==========================================
        // 7. OPPO (4 models)
        // ==========================================
        OnlinePhoneModel(
            id = "oppo_find_x7_ultra",
            brand = "Oppo",
            modelName = "Find X7 Ultra",
            specs = "Snapdragon 8 Gen 3, World's First Dual Periscope Cameras, Hasselblad HyperTone",
            ramOptions = "16GB LPDDR5X",
            storageOptions = "256GB / 512GB UFS 4.0",
            display = "6.82\" QHD+ Curved LTPO AMOLED, 1-120Hz, 4500 nits peak, Dolby Vision",
            camera = "50MP 1-inch LYT-900 OIS + 50MP 3x Periscope OIS + 50MP 6x Periscope OIS + 50MP UW",
            battery = "5000 mAh, 100W SUPERVOOC, 50W AIRVOOC Wireless",
            colors = "Ocean Blue, Sepia Brown, Tailored Black"
        ),
        OnlinePhoneModel(
            id = "oppo_reno12_pro",
            brand = "Oppo",
            modelName = "Reno 12 Pro 5G",
            specs = "Dimensity 7300-Energy, GenAI Features (AI Eraser 2.0, AI Studio), Splash Touch",
            ramOptions = "12GB LPDDR4X",
            storageOptions = "256GB / 512GB UFS 3.1",
            display = "6.7\" Quad-Curved Infinite View AMOLED, 120Hz, Gorilla Glass Victus 2",
            camera = "50MP Sony LYT-600 OIS + 50MP Telephoto 2x Portrait + 8MP UW, 50MP Eye-AF Front",
            battery = "5000 mAh, 80W SUPERVOOC",
            colors = "Sunset Gold, Space Brown, Nebula Silver"
        ),
        OnlinePhoneModel(
            id = "oppo_reno_11",
            brand = "Oppo",
            modelName = "Reno 11 5G",
            specs = "MediaTek Dimensity 7050 6nm, 32MP Telephoto Portrait Camera, ColorOS 14",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB UFS 2.2",
            display = "6.7\" 3D Curved OLED, 120Hz, 950 nits peak, 1.07 Billion Colors",
            camera = "50MP Sony LYT-600 OIS + 32MP 2x Telephoto + 8MP Ultra-Wide",
            battery = "5000 mAh, 67W SUPERVOOC",
            colors = "Wave Green, Rock Grey"
        ),
        OnlinePhoneModel(
            id = "oppo_a79_5g",
            brand = "Oppo",
            modelName = "Oppo A79 5G",
            specs = "MediaTek Dimensity 6020 7nm, Glowing Feather Design, 300% Ultra Volume Mode",
            ramOptions = "4GB / 8GB RAM",
            storageOptions = "128GB / 256GB (microSD expandable)",
            display = "6.72\" FHD+ Sunlight Display, 90Hz, 680 nits peak",
            camera = "50MP AI Main + 2MP Portrait, 8MP Selfie",
            battery = "5000 mAh, 33W SUPERVOOC",
            colors = "Mystery Black, Dazzling Purple, Glowing Green"
        ),

        // ==========================================
        // 8. REALME (4 models)
        // ==========================================
        OnlinePhoneModel(
            id = "realme_12_pro_plus",
            brand = "Realme",
            modelName = "Realme 12 Pro+ 5G",
            specs = "Snapdragon 7s Gen 2 (4nm), 64MP Periscope OIS (120x SuperZoom), Luxury Watch Dial",
            ramOptions = "8GB / 12GB LPDDR4X",
            storageOptions = "128GB / 256GB / 512GB UFS 3.1",
            display = "6.7\" 120Hz Curved Vision OLED, 2160Hz PWM Dimming, 950 nits",
            camera = "64MP OmniVision OV64B 3x Periscope OIS + 50MP Sony IMX890 OIS + 8MP UW, 32MP Front",
            battery = "5000 mAh, 67W SUPERVOOC",
            colors = "Submarine Blue, Navigator Beige, Explorer Red"
        ),
        OnlinePhoneModel(
            id = "realme_12_plus",
            brand = "Realme",
            modelName = "Realme 12+ 5G",
            specs = "MediaTek Dimensity 7050 5G, Sony LYT-600 OIS, Rainwater Smart Touch",
            ramOptions = "8GB / 12GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.67\" 120Hz Ultra Smooth AMOLED, 2000 nits peak, HDR10+",
            camera = "50MP Sony LYT-600 OIS + 8MP Ultra-Wide + 2MP Macro, 16MP Selfie",
            battery = "5000 mAh, 67W SUPERVOOC",
            colors = "Pioneer Green, Navigator Beige"
        ),
        OnlinePhoneModel(
            id = "realme_gt_6",
            brand = "Realme",
            modelName = "Realme GT 6",
            specs = "Snapdragon 8s Gen 3 (4nm), AI Flagship Killer, 6000 nits Ultra Bright Display",
            ramOptions = "8GB / 12GB / 16GB LPDDR5X",
            storageOptions = "256GB / 512GB UFS 4.0",
            display = "6.78\" 8T LTPO AMOLED, 1-120Hz, 6000 nits peak, Gorilla Glass Victus 2",
            camera = "50MP Sony LYT-808 OIS + 50MP Samsung JN5 2x Telephoto + 8MP Ultra-Wide",
            battery = "5500 mAh Dual-Cell, 120W SUPERVOOC",
            colors = "Fluid Silver, Razor Green"
        ),
        OnlinePhoneModel(
            id = "realme_c67",
            brand = "Realme",
            modelName = "Realme C67",
            specs = "Snapdragon 685 (6nm), 108MP 3x In-sensor Zoom, Mini Capsule 2.0",
            ramOptions = "6GB / 8GB LPDDR4X",
            storageOptions = "128GB / 256GB (microSD up to 2TB)",
            display = "6.72\" 90Hz FHD+ Display, 950 nits peak",
            camera = "108MP Ultra-Clear Camera + 2MP Depth, 8MP Selfie",
            battery = "5000 mAh, 33W SUPERVOOC",
            colors = "Sunny Oasis, Black Rock"
        ),

        // ==========================================
        // 9. ONEPLUS (4 models)
        // ==========================================
        OnlinePhoneModel(
            id = "oneplus_12",
            brand = "OnePlus",
            modelName = "OnePlus 12",
            specs = "Snapdragon 8 Gen 3, 4th Gen Hasselblad Camera, Dual Cryo-velocity VC Cooling",
            ramOptions = "12GB / 16GB / 24GB LPDDR5X",
            storageOptions = "256GB / 512GB / 1TB UFS 4.0",
            display = "6.82\" 2K ProXDR Display, LTPO 1-120Hz, 4500 nits peak, Aqua Touch",
            camera = "50MP Sony LYT-808 OIS + 64MP 3x Periscope OIS + 48MP Ultra-Wide, 32MP Front",
            battery = "5400 mAh Dual-Cell, 100W SUPERVOOC + 50W AIRVOOC Wireless",
            colors = "Flowy Emerald, Silky Black"
        ),
        OnlinePhoneModel(
            id = "oneplus_12r",
            brand = "OnePlus",
            modelName = "OnePlus 12R",
            specs = "Snapdragon 8 Gen 2 (4nm), 4th Gen LTPO Display, Largest OnePlus Battery",
            ramOptions = "8GB / 16GB LPDDR5X",
            storageOptions = "128GB UFS 3.1 / 256GB UFS 4.0",
            display = "6.78\" 1.5K ProXDR AMOLED, LTPO 1-120Hz, 4500 nits peak",
            camera = "50MP Sony IMX890 OIS + 8MP Ultra-Wide + 2MP Macro",
            battery = "5500 mAh, 100W SUPERVOOC (1-100% in 26 mins)",
            colors = "Cool Blue, Iron Gray"
        ),
        OnlinePhoneModel(
            id = "oneplus_nord_4",
            brand = "OnePlus",
            modelName = "OnePlus Nord 4 5G",
            specs = "Snapdragon 7+ Gen 3 4nm, All-Metal Unibody Design, 4 Android Updates",
            ramOptions = "8GB / 12GB / 16GB LPDDR5X",
            storageOptions = "128GB / 256GB / 512GB UFS 4.0",
            display = "6.74\" 1.5K Ultra Clear AMOLED, 120Hz, 2150 nits peak, Aqua Touch",
            camera = "50MP Sony LYT-600 OIS + 8MP Ultra-Wide, 16MP Selfie",
            battery = "5500 mAh, 100W SUPERVOOC",
            colors = "Nordic Hangout (Silver), Mercurial Silver, Obsidian Midnight"
        ),
        OnlinePhoneModel(
            id = "oneplus_open",
            brand = "OnePlus",
            modelName = "OnePlus Open",
            specs = "Foldable Flagship, Snapdragon 8 Gen 2, Hasselblad Imaging, Open Canvas Multitasking",
            ramOptions = "16GB LPDDR5X",
            storageOptions = "512GB UFS 4.0",
            display = "Main: 7.82\" 2K LTPO 120Hz Flexi-fluid AMOLED; Cover: 6.31\" FHD+ 120Hz Ceramic Guard",
            camera = "48MP Sony LYT-T808 Pixel Stacked OIS + 64MP 3x Periscope OIS + 48MP Ultra-Wide",
            battery = "4805 mAh, 67W SUPERVOOC",
            colors = "Emerald Dusk, Voyager Black"
        ),

        // ==========================================
        // 10. GOOGLE (5 models)
        // ==========================================
        OnlinePhoneModel(
            id = "google_pixel_9_pro_xl",
            brand = "Google",
            modelName = "Pixel 9 Pro XL",
            specs = "Google Tensor G4 (4nm), Titan M2, Gemini Nano AI built-in, 7 years OS upgrades",
            ramOptions = "16GB RAM",
            storageOptions = "128GB / 256GB / 512GB / 1TB UFS 3.1",
            display = "6.8\" Super Actua LTPO OLED, 1-120Hz, 3000 nits peak, Gorilla Glass Victus 2",
            camera = "50MP Octa PD Main OIS + 48MP 5x Quad PD Telephoto OIS + 48MP Ultra-Wide, 42MP Selfie",
            battery = "5060 mAh, 37W Wired Fast Charge, Fast Wireless",
            colors = "Obsidian, Porcelain, Hazel, Rose Quartz"
        ),
        OnlinePhoneModel(
            id = "google_pixel_9_pro",
            brand = "Google",
            modelName = "Pixel 9 Pro",
            specs = "Google Tensor G4, Compact Pro, Pro Triple Camera System, Satellite SOS",
            ramOptions = "16GB RAM",
            storageOptions = "128GB / 256GB / 512GB / 1TB",
            display = "6.3\" Super Actua LTPO OLED, 1-120Hz, 3000 nits peak",
            camera = "50MP Main OIS + 48MP 5x Telephoto OIS + 48MP Ultra-Wide, 42MP Front",
            battery = "4700 mAh, 27W Fast Wired, Fast Wireless",
            colors = "Obsidian, Porcelain, Hazel, Rose Quartz"
        ),
        OnlinePhoneModel(
            id = "google_pixel_9",
            brand = "Google",
            modelName = "Pixel 9",
            specs = "Google Tensor G4, Advanced AI Camera (Add Me, Magic Editor), 7 years software support",
            ramOptions = "12GB RAM",
            storageOptions = "128GB / 256GB",
            display = "6.3\" Actua OLED, 60-120Hz, 2700 nits peak, Gorilla Glass Victus 2",
            camera = "50MP Octa PD Main OIS + 48MP Quad PD Ultra-Wide Macro, 10.5MP Selfie",
            battery = "4700 mAh, 27W Fast Charging, Wireless Charging",
            colors = "Obsidian, Porcelain, Wintergreen, Peony"
        ),
        OnlinePhoneModel(
            id = "google_pixel_8a",
            brand = "Google",
            modelName = "Pixel 8a",
            specs = "Google Tensor G3, Best-in-class A-series camera, 7 years feature drops",
            ramOptions = "8GB LPDDR5x",
            storageOptions = "128GB / 256GB UFS 3.1",
            display = "6.1\" Actua OLED, 120Hz, 2000 nits peak, Gorilla Glass 3",
            camera = "64MP Quad PD Main OIS + 13MP Ultra-Wide, 13MP Front",
            battery = "4492 mAh, 18W Wired, Wireless Charging",
            colors = "Obsidian, Porcelain, Bay, Aloe"
        ),
        OnlinePhoneModel(
            id = "google_pixel_8_pro",
            brand = "Google",
            modelName = "Pixel 8 Pro",
            specs = "Google Tensor G3, Temperature Sensor, Pro Camera Controls, Best Take",
            ramOptions = "12GB LPDDR5X",
            storageOptions = "128GB / 256GB / 512GB / 1TB",
            display = "6.7\" Super Actua LTPO OLED, 1-120Hz, 2400 nits peak",
            camera = "50MP Octa PD OIS + 48MP 5x Telephoto OIS + 48MP Ultra-Wide, 10.5MP Selfie",
            battery = "5050 mAh, 30W Fast Wired, 23W Wireless",
            colors = "Bay, Obsidian, Porcelain, Mint"
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
