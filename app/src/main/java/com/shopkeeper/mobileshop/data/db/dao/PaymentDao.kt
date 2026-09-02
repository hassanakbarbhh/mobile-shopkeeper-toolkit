package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(payment: Payment): Long

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE saleId = :saleId")
    suspend fun getPaymentsForSale(saleId: Long): List<Payment>

    @Query("SELECT SUM(amount) FROM payments WHERE saleId = :saleId AND paymentType = 'RECEIVED'")
    suspend fun receivedForSale(saleId: Long): Double?
}
