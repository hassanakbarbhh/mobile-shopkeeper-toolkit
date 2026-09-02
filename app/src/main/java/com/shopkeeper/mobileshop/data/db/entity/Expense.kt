package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: ExpenseCategory,
    val amount: Double,
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)

enum class ExpenseCategory {
    RENT, SALARY, ELECTRICITY, INTERNET, TRANSPORT, MAINTENANCE, MARKETING, TAX, OTHER
}
