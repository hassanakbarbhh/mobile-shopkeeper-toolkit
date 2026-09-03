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
        PurchaseItem::class, Expense::class, Seller::class
    ],
    version = 2,
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
    abstract fun sellerDao(): SellerDao

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
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { seedInitialData(it) }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { ensureCleanDataAndDefaultStock(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                // Also trigger cleanup/seed check right away
                CoroutineScope(Dispatchers.IO).launch {
                    ensureCleanDataAndDefaultStock(instance)
                }
                instance
            }
        }

        suspend fun ensureCleanDataAndDefaultStock(db: AppDatabase) {
            try {
                // Delete old dummy customers so customer data starts clean
                db.customerDao().deleteDummyCustomers()

                // Delete dummy repairs and dummy expenses
                db.repairDao().deleteDummyRepairs()
                db.expenseDao().deleteDummyExpenses()

                // Check products in stock
                val existing = db.productDao().getAllProductsList()
                
                // Delete the old dummy accessory products
                val dummyNames = setOf(
                    "Apple 20W USB-C Power Adapter",
                    "boAt Airdopes 141 ANC",
                    "Tempered Glass (Universal 6.7\")",
                    "Mi 10000mAh Power Bank 3i"
                )
                existing.filter { it.name in dummyNames }.forEach {
                    db.productDao().delete(it)
                }

                // If stock is empty or missing phone models, seed all brands & models with no price
                val currentProducts = db.productDao().getAllProductsList()
                val hasOnlineModels = currentProducts.any { it.notes.contains("Specs:") || it.notes.contains("Online Model") || it.sellingPrice == 0.0 }
                
                if (currentProducts.isEmpty() || !hasOnlineModels) {
                    val defaultPhoneProducts = com.shopkeeper.mobileshop.data.catalog.OnlineCatalogRepository.allOnlineModels.map { 
                        it.toProductNoPrice() 
                    }
                    db.productDao().insertAll(defaultPhoneProducts)
                }
            } catch (e: Exception) {
                // Ignore background sync errors
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            // Seed all phone brand names & models with no price and 0 quantity
            val defaultPhoneProducts = com.shopkeeper.mobileshop.data.catalog.OnlineCatalogRepository.allOnlineModels.map {
                it.toProductNoPrice()
            }
            db.productDao().insertAll(defaultPhoneProducts)

            // Shop Owner Seller
            val s1 = Seller(
                name = "Hassan (Owner)",
                phone = "+92 300 1234567",
                role = "Shop Owner",
                commissionPercent = 0.0,
                isActive = true
            )
            val s2 = Seller(
                name = "Ali Khan",
                phone = "+92 321 7654321",
                role = "Sales Executive",
                commissionPercent = 2.0,
                isActive = true
            )
            listOf(s1, s2).forEach { db.sellerDao().insert(it) }
        }
    }
}
