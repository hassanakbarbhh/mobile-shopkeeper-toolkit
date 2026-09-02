package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long? = null,
    val customerId: Long? = null,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val paymentType: PaymentType,
    val reference: String = "",
    val notes: String = "",
    val paymentDate: Long = System.currentTimeMillis()
)

enum class PaymentType { RECEIVED, REFUND }
