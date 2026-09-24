package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "cash_closing_table",
    indices = [
        androidx.room.Index(value = ["cloudId"])
    ]
)
data class CashClosing(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val closingDate: Long,
    val openingCash: Double,
    val cashIn: Double,
    val cashOut: Double,
    val countedCash: Double,
    val variance: Double,
    val signedBy: String,
    val cloudId: String = java.util.UUID.randomUUID().toString(),
    val shopId: String = "",
    val version: Long = 1L,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val lastModifiedBy: String = "",
    val lastModifiedDevice: String = ""
)
