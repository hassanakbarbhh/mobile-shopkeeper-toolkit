package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "repairs",
    indices = [
        androidx.room.Index(value = ["status"]),
        androidx.room.Index(value = ["cloudId"])
    ]
)
data class Repair(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String,
    val deviceBrand: String,
    val deviceModel: String,
    val imei: String = "",
    val issueDescription: String,
    val estimatedCost: Double = 0.0,
    val actualCost: Double = 0.0,
    val status: RepairStatus = RepairStatus.RECEIVED,
    val receivedDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long? = null,
    val notes: String = "",
    val cloudId: String = java.util.UUID.randomUUID().toString(),
    val shopId: String = "",
    val version: Long = 1L,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val lastModifiedBy: String = "",
    val lastModifiedDevice: String = ""
)

enum class RepairStatus {
    RECEIVED, DIAGNOSING, WAITING_PARTS, IN_REPAIR, COMPLETED, DELIVERED, CANCELLED
}
