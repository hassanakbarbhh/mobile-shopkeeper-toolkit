package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        androidx.room.Index(value = ["cloudId"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: ExpenseCategory,
    val amount: Double,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val cloudId: String = java.util.UUID.randomUUID().toString(),
    val shopId: String = "",
    val version: Long = 1L,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val lastModifiedBy: String = "",
    val lastModifiedDevice: String = ""
)

enum class ExpenseCategory {
    RENT, SALARY, ELECTRICITY, INTERNET, TRANSPORT, MAINTENANCE, MARKETING, TAX, OTHER
}
