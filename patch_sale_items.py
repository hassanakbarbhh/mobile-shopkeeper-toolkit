with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "r") as f:
    text = f.read()

replacement = """    @Query("SELECT * FROM sale_items_table")
    suspend fun getAllSaleItemsList(): List<SaleItem>"""

if "getAllSaleItemsList" not in text:
    text = text.replace("interface SaleDao {", "interface SaleDao {\n" + replacement)

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/SaleDao.kt", "w") as f:
    f.write(text)
