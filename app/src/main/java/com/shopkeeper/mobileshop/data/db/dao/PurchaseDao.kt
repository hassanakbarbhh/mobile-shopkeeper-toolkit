package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Purchase
import com.shopkeeper.mobileshop.data.db.entity.PurchaseItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Insert suspend fun insert(p: Purchase): Long
    @Update suspend fun update(p: Purchase)
    @Delete suspend fun delete(p: Purchase)
    @Insert suspend fun insertItems(items: List<PurchaseItem>)

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAll(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItemsForPurchase(purchaseId: Long): List<PurchaseItem>

    @Query("SELECT * FROM purchase_items WHERE imei = :imei")
    suspend fun itemsByImei(imei: String): List<PurchaseItem>

    @Query("SELECT SUM(paidAmount) FROM purchases WHERE purchaseDate BETWEEN :start AND :end")
    fun sumPaidInRange(start: Long, end: Long): Flow<Double?>
}
