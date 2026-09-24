package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "imei_assets",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["currentStatus"]),
        Index(value = ["customerPhone"])
    ]
)
data class ImeiAsset(
    @PrimaryKey
    val imei: String,
    val serialNumber: String = "",
    val productId: Long = 0L,
    val productName: String = "",
    val brand: String = "",
    val model: String = "",
    val storage: String = "",
    val color: String = "",
    val purchaseId: Long = 0L,
    val supplierName: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStatus: String = STATUS_IN_STOCK, // IN_STOCK, SOLD, REPAIR, TRANSFERRED, RETURNED
    val currentBranch: String = "Main Branch",
    val ptaStatus: String = "Approved",          // Approved, Non-PTA, CPID, Patch
    val warrantyExpiryDate: Long = 0L,
    val customerId: Long = 0L,
    val customerName: String = "",
    val customerPhone: String = "",
    val saleInvoiceId: Long = 0L,
    val repairHistoryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_IN_STOCK = "IN_STOCK"
        const val STATUS_SOLD = "SOLD"
        const val STATUS_REPAIR = "REPAIR"
        const val STATUS_TRANSFERRED = "TRANSFERRED"
        const val STATUS_RETURNED = "RETURNED"
    }
}
