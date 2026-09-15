package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_closing_table")
data class CashClosing(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val closingDate: Long,
    val openingCash: Double,
    val cashIn: Double,
    val cashOut: Double,
    val countedCash: Double,
    val variance: Double,
    val signedBy: String
)
