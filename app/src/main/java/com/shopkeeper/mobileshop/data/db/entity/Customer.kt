package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        androidx.room.Index(value = ["cloudId"]),
        androidx.room.Index(value = ["phone"])
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val cloudId: String = java.util.UUID.randomUUID().toString(),
    val shopId: String = "",
    val version: Long = 1L,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val lastModifiedBy: String = "",
    val lastModifiedDevice: String = ""
)
