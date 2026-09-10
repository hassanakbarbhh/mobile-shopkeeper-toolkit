with open("app/src/main/java/com/shopkeeper/mobileshop/data/repository/ShopRepository.kt", "r") as f:
    content = f.read()

if "suspend fun getPendingSales()" not in content:
    content = content.replace("val totalPendingAmount: Flow<Double?> = db.saleDao().getTotalPendingAmount()", "val totalPendingAmount: Flow<Double?> = db.saleDao().getTotalPendingAmount()\n    suspend fun getPendingSales() = db.saleDao().getPendingSales()")

with open("app/src/main/java/com/shopkeeper/mobileshop/data/repository/ShopRepository.kt", "w") as f:
    f.write(content)
