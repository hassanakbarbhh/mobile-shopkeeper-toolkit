package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Purchase
import com.shopkeeper.mobileshop.data.db.entity.PurchaseItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Insert suspend fun insert(p: Purchase): Long
    @Insert suspend fun insertItems(items: List<PurchaseItem>)

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAll(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchase_items WHERE imei = :imei")
    suspend fun itemsByImei(imei: String): List<PurchaseItem>
}
