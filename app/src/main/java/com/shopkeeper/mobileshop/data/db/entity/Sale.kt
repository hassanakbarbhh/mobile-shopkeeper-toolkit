package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

import androidx.room.Index

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["saleDate"]),
        Index(value = ["paymentStatus"]),
        Index(value = ["cloudId"])
    ]
)
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
    val saleDate: Long = System.currentTimeMillis(),
    val cloudId: String = java.util.UUID.randomUUID().toString(),
    val shopId: String = "",
    val version: Long = 1L,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val lastModifiedBy: String = "",
    val lastModifiedDevice: String = ""
) {
    @Ignore var items: List<SaleItem> = emptyList()
    val invoiceNumber: String get() = id.toString()
}

enum class PaymentMethod { CASH, CARD, UPI, BANK_TRANSFER, CREDIT, OTHER }
enum class PaymentStatus { PAID, PENDING, PARTIAL }
