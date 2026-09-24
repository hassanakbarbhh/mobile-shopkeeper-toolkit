package com.shopkeeper.mobileshop.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    indices = [
        androidx.room.Index(value = ["cloudId"])
    ]
)
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val company: String = "",
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
