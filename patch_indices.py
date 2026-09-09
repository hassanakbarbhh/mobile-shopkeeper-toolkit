import os

def patch_file(path, old, new):
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace(old, new)
    with open(path, 'w') as f:
        f.write(content)

patch_file(
    "app/src/main/java/com/shopkeeper/mobileshop/data/db/entity/Sale.kt",
    '@Entity(tableName = "sales")',
    'import androidx.room.Index\n\n@Entity(tableName = "sales", indices = [Index(value = ["customerId"]), Index(value = ["saleDate"])])'
)

patch_file(
    "app/src/main/java/com/shopkeeper/mobileshop/data/db/entity/Product.kt",
    '@Entity(tableName = "products")',
    'import androidx.room.Index\n\n@Entity(tableName = "products", indices = [Index(value = ["imei"], unique = true), Index(value = ["name"])])'
)
