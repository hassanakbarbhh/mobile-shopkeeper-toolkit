package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String,
    val totalAmount: Double,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val finalAmount: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val sellerId: Long? = null,
    val sellerName: String = "Owner",
    val notes: String = "",
    val saleDate: Long = System.currentTimeMillis()
) {
    @Ignore var items: List<SaleItem> = emptyList()
}

enum class PaymentMethod { CASH, CARD, UPI, BANK_TRANSFER, CREDIT, OTHER }
enum class PaymentStatus { PAID, PENDING, PARTIAL }
