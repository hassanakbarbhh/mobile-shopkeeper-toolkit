with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "r") as f:
    content = f.read()

# Add getPendingSales if not exists
if "fun getPendingSales()" not in content:
    content = content.replace("fun getTotalPendingAmount(): Flow<Double?>", "fun getTotalPendingAmount(): Flow<Double?>\n\n    @Query(\"SELECT * FROM sales WHERE paymentStatus = 'PENDING' OR paymentStatus = 'PARTIAL'\")\n    suspend fun getPendingSales(): List<Sale>")

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "w") as f:
    f.write(content)
