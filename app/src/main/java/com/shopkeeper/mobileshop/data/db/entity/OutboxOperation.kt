package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "outbox_operations",
    indices = [
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class OutboxOperation(
    @PrimaryKey
    val eventId: String = UUID.randomUUID().toString(),
    val operationType: String, // CREATE_SALE, UPDATE_STOCK, ADD_PAYMENT, REPAIR_UPDATE, PURCHASE_RECEIVE, etc.
    val entityType: String,    // Sale, Product, Payment, Repair, Purchase, CashClosing, Customer
    val entityId: String,
    val payloadJson: String,
    val status: String = STATUS_PENDING, // PENDING, UPLOADING, COMPLETED, FAILED
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long = 0L,
    val errorMessage: String? = null,
    val deviceId: String = "DEVICE_DEFAULT",
    val shopId: String = "SHOP_DEFAULT",
    val userId: String = "USER_DEFAULT"
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_UPLOADING = "UPLOADING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"

        // Operations
        const val OP_CREATE_SALE = "CREATE_SALE"
        const val OP_UPDATE_STOCK = "UPDATE_STOCK"
        const val OP_ADD_PAYMENT = "ADD_PAYMENT"
        const val OP_REPAIR_UPDATE = "REPAIR_UPDATE"
        const val OP_PURCHASE_RECEIVE = "PURCHASE_RECEIVE"
        const val OP_CUSTOMER_CREATE = "CUSTOMER_CREATE"
        const val OP_EXPENSE_CREATE = "EXPENSE_CREATE"
        const val OP_IMEI_LIFECYCLE = "IMEI_LIFECYCLE"
        const val OP_CASH_CLOSING = "CASH_CLOSING"
    }
}
