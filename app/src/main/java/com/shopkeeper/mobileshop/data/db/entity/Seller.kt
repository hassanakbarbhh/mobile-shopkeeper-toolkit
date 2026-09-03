package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sellers")
data class Seller(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val role: String = "Sales Executive",
    val commissionPercent: Double = 0.0,
    val isActive: Boolean = true,
    val pin: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
