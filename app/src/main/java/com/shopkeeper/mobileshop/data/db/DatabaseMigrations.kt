package com.shopkeeper.mobileshop.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Products table
            db.execSQL("ALTER TABLE products ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE products ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE products ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE products ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE products ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE products ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE products ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_products_cloudId ON products(cloudId)")
            db.execSQL("UPDATE products SET cloudId = hex(randomblob(16)) WHERE cloudId = '' OR cloudId IS NULL")

            // 2. Customers table
            db.execSQL("ALTER TABLE customers ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE customers ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE customers ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE customers ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE customers ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE customers ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE customers ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE customers ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_customers_cloudId ON customers(cloudId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_customers_phone ON customers(phone)")
            db.execSQL("UPDATE customers SET cloudId = hex(randomblob(16)) WHERE cloudId = '' OR cloudId IS NULL")
            db.execSQL("UPDATE customers SET updatedAt = createdAt WHERE updatedAt = 0")

            // 3. Sales table
            db.execSQL("ALTER TABLE sales ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE sales ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE sales ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE sales ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE sales ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE sales ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE sales ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE sales ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_sales_cloudId ON sales(cloudId)")
            db.execSQL("UPDATE sales SET cloudId = hex(randomblob(16)) WHERE cloudId = '' OR cloudId IS NULL")
            db.execSQL("UPDATE sales SET updatedAt = saleDate WHERE updatedAt = 0")

            // 4. Repairs table
            db.execSQL("ALTER TABLE repairs ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE repairs ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE repairs ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE repairs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE repairs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE repairs ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE repairs ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE repairs ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_repairs_cloudId ON repairs(cloudId)")
            db.execSQL("UPDATE repairs SET cloudId = hex(randomblob(16)) WHERE cloudId = '' OR cloudId IS NULL")
            db.execSQL("UPDATE repairs SET updatedAt = receivedDate WHERE updatedAt = 0")

            // 5. Expenses table
            db.execSQL("ALTER TABLE expenses ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE expenses ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE expenses ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE expenses ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE expenses ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE expenses ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE expenses ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE expenses ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_cloudId ON expenses(cloudId)")
            db.execSQL("UPDATE expenses SET cloudId = hex(randomblob(16)) WHERE cloudId = '' OR cloudId IS NULL")
            db.execSQL("UPDATE expenses SET updatedAt = date WHERE updatedAt = 0")

            // 6. Cash closing table
            db.execSQL("ALTER TABLE cash_closing_table ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE cash_closing_table ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE cash_closing_table ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE cash_closing_table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE cash_closing_table ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE cash_closing_table ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE cash_closing_table ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE cash_closing_table ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cash_closing_table_cloudId ON cash_closing_table(cloudId)")
            db.execSQL("UPDATE cash_closing_table SET cloudId = hex(randomblob(16)) WHERE cloudId = '' OR cloudId IS NULL")
            db.execSQL("UPDATE cash_closing_table SET updatedAt = closingDate WHERE updatedAt = 0")

            // 7. IMEI Assets table
            db.execSQL("ALTER TABLE imei_assets ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE imei_assets ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE imei_assets ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE imei_assets ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE imei_assets ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE imei_assets ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE imei_assets ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_imei_assets_cloudId ON imei_assets(cloudId)")
            db.execSQL("UPDATE imei_assets SET cloudId = imei WHERE cloudId = '' OR cloudId IS NULL")

            // 8. Suppliers table
            db.execSQL("ALTER TABLE suppliers ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE suppliers ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE suppliers ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE suppliers ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE suppliers ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE suppliers ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE suppliers ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE suppliers ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_suppliers_cloudId ON suppliers(cloudId)")
            db.execSQL("UPDATE suppliers SET cloudId = hex(randomblob(16)) WHERE cloudId = '' OR cloudId IS NULL")
            db.execSQL("UPDATE suppliers SET updatedAt = createdAt WHERE updatedAt = 0")

            // 9. Payments table
            db.execSQL("ALTER TABLE payments ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE payments ADD COLUMN shopId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE payments ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE payments ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE payments ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE payments ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE payments ADD COLUMN lastModifiedBy TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE payments ADD COLUMN lastModifiedDevice TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_cloudId ON payments(cloudId)")
            db.execSQL("UPDATE payments SET cloudId = hex(randomblob(16)) WHERE cloudId = '' OR cloudId IS NULL")
            db.execSQL("UPDATE payments SET updatedAt = paymentDate WHERE updatedAt = 0")
        }
    }
}
