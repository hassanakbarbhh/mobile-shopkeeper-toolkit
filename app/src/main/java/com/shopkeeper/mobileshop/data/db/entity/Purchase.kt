package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val supplierName: String,
    val totalCost: Double,
    val paidAmount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val notes: String = "",
    val purchaseDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_items")
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productName: String,
    val supplierName: String = "",
    val imei: String = "",
    val quantity: Int = 1,
    val unitCost: Double
)
