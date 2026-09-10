with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "r") as f:
    content = f.read()

if "fun getSaleItemsByProductId" not in content:
    content = content.replace("suspend fun getSaleItems(saleId: Long): List<SaleItem>", "suspend fun getSaleItems(saleId: Long): List<SaleItem>\n\n    @Query(\"SELECT * FROM sale_items WHERE productId = :productId\")\n    suspend fun getSaleItemsByProductId(productId: Long): List<SaleItem>")

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "w") as f:
    f.write(content)
