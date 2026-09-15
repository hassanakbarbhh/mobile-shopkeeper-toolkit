with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "r") as f:
    text = f.read()

replacement = """    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>
    
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    suspend fun getAllSalesList(): List<Sale>"""

text = text.replace('    @Query("SELECT * FROM sales ORDER BY saleDate DESC")\n    fun getAllSales(): Flow<List<Sale>>', replacement)

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "w") as f:
    f.write(text)
