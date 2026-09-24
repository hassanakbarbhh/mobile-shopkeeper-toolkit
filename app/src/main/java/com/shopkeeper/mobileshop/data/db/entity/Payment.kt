package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [
        androidx.room.Index(value = ["cloudId"])
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long? = null,
    val customerId: Long? = null,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val paymentType: PaymentType,
    val reference: String = "",
    val notes: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val cloudId: String = java.util.UUID.randomUUID().toString(),
    val shopId: String = "",
    val version: Long = 1L,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val lastModifiedBy: String = "",
    val lastModifiedDevice: String = ""
)

enum class PaymentType { RECEIVED, REFUND }
