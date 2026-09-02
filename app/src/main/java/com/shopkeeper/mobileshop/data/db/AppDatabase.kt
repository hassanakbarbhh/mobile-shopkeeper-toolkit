package com.shopkeeper.mobileshop.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shopkeeper.mobileshop.data.db.dao.*
import com.shopkeeper.mobileshop.data.db.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class, Customer::class, Sale::class, SaleItem::class,
        Repair::class, Payment::class, Supplier::class, Purchase::class,
        PurchaseItem::class, Expense::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun repairDao(): RepairDao
    abstract fun paymentDao(): PaymentDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobile_shop_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { seedInitialData(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val p1 = Product(
                name = "iPhone 15 Pro (128GB)",
                brand = "Apple",
                model = "A2848",
                imei = "359876543210981",
                category = ProductCategory.SMARTPHONE,
                purchasePrice = 110000.0,
                sellingPrice = 129900.0,
                quantity = 4,
                ram = "8GB",
                storage = "128GB",
                color = "Natural Titanium"
            )
            val p2 = Product(
                name = "Samsung Galaxy S24 Ultra",
                brand = "Samsung",
                model = "SM-S928B",
                imei = "359876543210982",
                category = ProductCategory.SMARTPHONE,
                purchasePrice = 112000.0,
                sellingPrice = 129999.0,
                quantity = 3,
                ram = "12GB",
                storage = "256GB",
                color = "Titanium Gray"
            )
            val p3 = Product(
                name = "Redmi Note 13 Pro+",
                brand = "Xiaomi",
                model = "23090RA98G",
                imei = "869876543210983",
                category = ProductCategory.SMARTPHONE,
                purchasePrice = 26000.0,
                sellingPrice = 31999.0,
                quantity = 8,
                ram = "8GB",
                storage = "256GB",
                color = "Midnight Black"
            )
            val p4 = Product(
                name = "Apple 20W USB-C Power Adapter",
                brand = "Apple",
                model = "MHJE3HN/A",
                category = ProductCategory.CHARGER,
                purchasePrice = 1400.0,
                sellingPrice = 1900.0,
                quantity = 15
            )
            val p5 = Product(
                name = "boAt Airdopes 141 ANC",
                brand = "boAt",
                model = "Airdopes 141",
                category = ProductCategory.EARPHONE,
                purchasePrice = 999.0,
                sellingPrice = 1699.0,
                quantity = 12
            )
            val p6 = Product(
                name = "Tempered Glass (Universal 6.7\")",
                brand = "Generic",
                model = "TG-67",
                category = ProductCategory.SCREEN_PROTECTOR,
                purchasePrice = 30.0,
                sellingPrice = 150.0,
                quantity = 45
            )
            val p7 = Product(
                name = "Mi 10000mAh Power Bank 3i",
                brand = "Xiaomi",
                model = "PB100LZM",
                category = ProductCategory.POWER_BANK,
                purchasePrice = 850.0,
                sellingPrice = 1299.0,
                quantity = 6
            )
            listOf(p1, p2, p3, p4, p5, p6, p7).forEach { db.productDao().insert(it) }

            val c1 = Customer(name = "Amit Kumar", phone = "+91 98765 43210", address = "Sector 14, Main Market")
            val c2 = Customer(name = "Pooja Sharma", phone = "+91 98123 45678", address = "Civil Lines")
            val c3 = Customer(name = "Rahul Verma", phone = "+91 99988 77665", address = "Model Town")
            listOf(c1, c2, c3).forEach { db.customerDao().insert(it) }

            val sup1 = Supplier(name = "Shree Balaji Mobile Dist", phone = "+91 98222 11111", company = "Balaji Dist, Nehru Place")
            val sup2 = Supplier(name = "Supreme Telecom Spares", phone = "+91 98333 22222", company = "Gaffar Market, Karol Bagh")
            listOf(sup1, sup2).forEach { db.supplierDao().insert(it) }

            val r1 = Repair(
                customerName = "Pooja Sharma",
                customerPhone = "+91 98123 45678",
                deviceBrand = "Samsung",
                deviceModel = "Galaxy A52",
                imei = "354411223344556",
                issueDescription = "Cracked Super AMOLED display, touch responsive",
                estimatedCost = 4200.0,
                status = RepairStatus.IN_REPAIR
            )
            val r2 = Repair(
                customerName = "Vikas Patel",
                customerPhone = "+91 97777 66666",
                deviceBrand = "iPhone",
                deviceModel = "12",
                imei = "351122334455667",
                issueDescription = "Battery health 68%, replacement needed",
                estimatedCost = 3500.0,
                status = RepairStatus.RECEIVED
            )
            listOf(r1, r2).forEach { db.repairDao().insert(it) }

            val exp1 = Expense(title = "Shop Rent - Main Hall", category = ExpenseCategory.RENT, amount = 18000.0)
            val exp2 = Expense(title = "Electricity Bill", category = ExpenseCategory.ELECTRICITY, amount = 2450.0)
            val exp3 = Expense(title = "Broadband Internet", category = ExpenseCategory.INTERNET, amount = 999.0)
            listOf(exp1, exp2, exp3).forEach { db.expenseDao().insert(it) }
        }
    }
}
