with open("app/src/main/java/com/shopkeeper/mobileshop/data/repository/ShopRepository.kt", "r") as f:
    content = f.read()

content = content.replace("    suspend fun insertProduct(p: Product) = db.productDao().insert(p)", "    suspend fun getProductByBarcode(barcode: String) = db.productDao().getByImei(barcode)\n    suspend fun insertProduct(p: Product) = db.productDao().insert(p)")

with open("app/src/main/java/com/shopkeeper/mobileshop/data/repository/ShopRepository.kt", "w") as f:
    f.write(content)
