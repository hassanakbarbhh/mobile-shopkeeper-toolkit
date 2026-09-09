with open("app/src/main/java/com/shopkeeper/mobileshop/data/repository/ShopRepository.kt", "r") as f:
    content = f.read()

import re
old = """    suspend fun getProduct(id: Long) = db.productDao().getById(id)"""
new = """    suspend fun getProduct(id: Long) = db.productDao().getById(id)
    suspend fun getProductByBarcode(barcode: String): com.shopkeeper.mobileshop.data.db.entity.Product? {
        return db.productDao().getByImei(barcode)
    }"""
if old in content:
    content = content.replace(old, new)

with open("app/src/main/java/com/shopkeeper/mobileshop/data/repository/ShopRepository.kt", "w") as f:
    f.write(content)
