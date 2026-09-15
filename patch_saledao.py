import re

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "r") as f:
    text = f.read()

replacement = """    @Query("SELECT * FROM sales_table ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales_table ORDER BY saleDate DESC")
    suspend fun getAllSalesList(): List<Sale>"""

if "suspend fun getAllSalesList()" not in text:
    text = text.replace('    @Query("SELECT * FROM sales_table ORDER BY saleDate DESC")\n    fun getAllSales(): Flow<List<Sale>>', replacement)

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "w") as f:
    f.write(text)
