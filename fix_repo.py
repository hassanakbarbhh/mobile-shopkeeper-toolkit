with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/ProductDao.kt", "r") as f:
    content = f.read()

if "fun getProductById" not in content:
    content = content.replace("fun getByImei(imei: String): Product?", "fun getByImei(imei: String): Product?\n\n    @Query(\"SELECT * FROM products WHERE id = :id\")\n    suspend fun getProductById(id: Long): Product?")
with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/ProductDao.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/shopkeeper/mobileshop/data/repository/ShopRepository.kt", "r") as f:
    content = f.read()

if "fun getProduct(" not in content:
    content = content.replace("suspend fun getProductByBarcode", "suspend fun getProduct(id: Long) = db.productDao().getProductById(id)\n    suspend fun getProductByBarcode")
with open("app/src/main/java/com/shopkeeper/mobileshop/data/repository/ShopRepository.kt", "w") as f:
    f.write(content)
