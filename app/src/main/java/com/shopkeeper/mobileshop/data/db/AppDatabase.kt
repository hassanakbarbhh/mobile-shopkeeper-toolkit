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
    version = 11,
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
                    .addMigrations(
                        DatabaseMigrations.MIGRATION_7_8,
                        DatabaseMigrations.MIGRATION_8_9,
                        DatabaseMigrations.MIGRATION_9_10,
                        DatabaseMigrations.MIGRATION_10_11
                    )
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // No demo seed data: a fresh install must start empty so
                            // real shop ledgers are never polluted with fake records.
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
                instance
            }
        }

        /**
         * Seeds the default phone catalog only when the products table is completely
         * empty (fresh install). Never deletes or overwrites existing business data.
         * Kept public because the regression suite exercises it directly (BUG-002).
         */
        suspend fun ensureCleanDataAndDefaultStock(db: AppDatabase) {
            try {
                val currentProducts = db.productDao().getAllProductsList()
                if (currentProducts.isEmpty()) {
                    val defaultPhoneProducts = com.shopkeeper.mobileshop.data.catalog.OnlineCatalogRepository.allOnlineModels.map {
                        it.toProductNoPrice()
                    }
                    db.productDao().insertAll(defaultPhoneProducts)
                }
            } catch (e: Exception) {
                // Ignore background initialization errors
            }
        }
    }
}
