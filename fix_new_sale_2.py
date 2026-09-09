with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/NewSaleFragment.kt", "r") as f:
    content = f.read()

import re
content = content.replace("product.price", "product.sellingPrice")

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/NewSaleFragment.kt", "w") as f:
    f.write(content)
