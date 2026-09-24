package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "imei_lifecycle_events",
    indices = [
        Index(value = ["imei"]),
        Index(value = ["timestamp"])
    ]
)
data class ImeiLifecycleEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val imei: String,
    val eventType: String, // PURCHASED, IN_STOCK, TRANSFERRED, SOLD, CUSTOMER, WARRANTY, REPAIR
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val details: String,
    val referenceId: String = "", // e.g. INV-002891, R-1042, PO-4481
    val performedBy: String = "Hassan (Owner)"
) {
    companion object {
        const val EVENT_PURCHASED = "PURCHASED"
        const val EVENT_IN_STOCK = "IN_STOCK"
        const val EVENT_TRANSFERRED = "TRANSFERRED"
        const val EVENT_SOLD = "SOLD"
        const val EVENT_CUSTOMER = "CUSTOMER"
        const val EVENT_WARRANTY = "WARRANTY"
        const val EVENT_REPAIR = "REPAIR"
        const val EVENT_RETURNED = "RETURNED"
    }
}
