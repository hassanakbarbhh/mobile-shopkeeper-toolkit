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
        PurchaseItem::class, Expense::class, Seller::class, CashClosing::class,
        OutboxOperation::class, ImeiAsset::class, ImeiLifecycleEvent::class
    ],
    version = 8,
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
    abstract fun cashClosingDao(): CashClosingDao
    abstract fun outboxDao(): OutboxDao
    abstract fun imeiAssetDao(): ImeiAssetDao

    companion object {
        const val DATABASE_NAME = "mobile_shop_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun closeDatabase() {
            try {
                if (INSTANCE?.isOpen == true) {
                    INSTANCE?.close()
                }
            } catch (e: Exception) {
                // Ignore
            } finally {
                INSTANCE = null
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(DatabaseMigrations.MIGRATION_7_8)
                    .fallbackToDestructiveMigrationOnDowngrade()
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

            // Seed flagship IMEI 356789123456789 lifecycle as seen in product design poster
            val sampleImei = "356789123456789"
            val handset = ImeiAsset(
                imei = sampleImei,
                serialNumber = "R58M30XYZ89",
                productId = 1L,
                productName = "Samsung Galaxy S24",
                brand = "Samsung",
                model = "Galaxy S24",
                storage = "256GB",
                color = "Phantom Black",
                supplierName = "ABC Mobile",
                purchasePrice = 160000.0,
                sellingPrice = 185000.0,
                currentStatus = ImeiAsset.STATUS_SOLD,
                currentBranch = "Main Branch",
                ptaStatus = "Approved",
                warrantyExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000),
                customerId = 1L,
                customerName = "Muhammad Ali",
                customerPhone = "0300-1234567",
                saleInvoiceId = 2891L,
                repairHistoryCount = 1
            )
            db.imeiAssetDao().insertAsset(handset)

            val now = System.currentTimeMillis()
            val dayMs = 24L * 3600 * 1000
            val events = listOf(
                ImeiLifecycleEvent(
                    imei = sampleImei,
                    eventType = ImeiLifecycleEvent.EVENT_PURCHASED,
                    timestamp = now - (40 * dayMs),
                    title = "Purchased from Supplier",
                    details = "Purchased from ABC Mobile Wholesale at Rs 160,000",
                    referenceId = "PO-4481"
                ),
                ImeiLifecycleEvent(
                    imei = sampleImei,
                    eventType = ImeiLifecycleEvent.EVENT_IN_STOCK,
                    timestamp = now - (38 * dayMs),
                    title = "Received In Stock",
                    details = "Verified PTA Status: Approved, Battery Health: 100%",
                    referenceId = "STK-902"
                ),
                ImeiLifecycleEvent(
                    imei = sampleImei,
                    eventType = ImeiLifecycleEvent.EVENT_TRANSFERRED,
                    timestamp = now - (34 * dayMs),
                    title = "Transferred to Display",
                    details = "Transferred to Counter 1 - Main Branch",
                    referenceId = "TR-102"
                ),
                ImeiLifecycleEvent(
                    imei = sampleImei,
                    eventType = ImeiLifecycleEvent.EVENT_SOLD,
                    timestamp = now - (30 * dayMs),
                    title = "Sold to Customer",
                    details = "Sold to Muhammad Ali for Rs 185,000. Invoice #INV-002891",
                    referenceId = "INV-002891"
                ),
                ImeiLifecycleEvent(
                    imei = sampleImei,
                    eventType = ImeiLifecycleEvent.EVENT_WARRANTY,
                    timestamp = now - (30 * dayMs),
                    title = "Official Warranty Activated",
                    details = "1 Year Brand Warranty active till August 2025",
                    referenceId = "WAR-S24"
                ),
                ImeiLifecycleEvent(
                    imei = sampleImei,
                    eventType = ImeiLifecycleEvent.EVENT_REPAIR,
                    timestamp = now - (10 * dayMs),
                    title = "Repair Inspection: Screen Glass",
                    details = "Replaced outer protective glass. Ticket #R-1042 completed",
                    referenceId = "R-1042"
                )
            )
            events.forEach { db.imeiAssetDao().insertLifecycleEvent(it) }
        }
    }
}
