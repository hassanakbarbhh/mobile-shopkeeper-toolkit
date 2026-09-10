with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/entity/Sale.kt", "r") as f:
    content = f.read()
content = content.replace('indices = [Index(value = ["customerId"]), Index(value = ["saleDate"])]', 'indices = [Index(value = ["customerId"]), Index(value = ["saleDate"]), Index(value = ["paymentStatus"])]')
with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/entity/Sale.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/entity/SaleItem.kt", "r") as f:
    content = f.read()
content = content.replace('@Entity(tableName = "sale_items", foreignKeys = [', '@Entity(tableName = "sale_items", indices = [Index(value = ["imei"])], foreignKeys = [')
with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/entity/SaleItem.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/entity/Repair.kt", "r") as f:
    content = f.read()
content = content.replace('@Entity(tableName = "repairs")', '@Entity(tableName = "repairs", indices = [androidx.room.Index(value = ["status"])])')
with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/entity/Repair.kt", "w") as f:
    f.write(content)
