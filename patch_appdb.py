with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/AppDatabase.kt", "r") as f:
    text = f.read()

text = text.replace("PurchaseItem::class, Expense::class, Seller::class", "PurchaseItem::class, Expense::class, Seller::class, CashClosing::class")
text = text.replace("version = 5,", "version = 6,")
text = text.replace("abstract fun sellerDao(): SellerDao", "abstract fun sellerDao(): SellerDao\n    abstract fun cashClosingDao(): CashClosingDao")

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/AppDatabase.kt", "w") as f:
    f.write(text)
