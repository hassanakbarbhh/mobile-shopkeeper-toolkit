package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.db.entity.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSale(sale: Sale): Long
    @Update suspend fun update(sale: Sale)
    @Delete suspend fun delete(sale: Sale)
    @Query("DELETE FROM sales WHERE id = :saleId") suspend fun deleteSaleById(saleId: Long)
    @Query("DELETE FROM sale_items WHERE saleId = :saleId") suspend fun deleteSaleItems(saleId: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE sellerId = :sellerId ORDER BY saleDate DESC")
    fun getSalesBySeller(sellerId: Long): Flow<List<Sale>>

    @Query("SELECT SUM(finalAmount) FROM sales WHERE sellerId = :sellerId")
    fun getTotalSalesBySeller(sellerId: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales WHERE sellerId = :sellerId")
    fun getSalesCountBySeller(sellerId: Long): Flow<Int>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sale_items WHERE imei = :imei")
    suspend fun saleItemsByImei(imei: String): List<SaleItem>

    @Query("SELECT * FROM sales WHERE saleDate BETWEEN :startDate AND :endDate ORDER BY saleDate DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>>

    @Query("SELECT SUM(finalAmount) FROM sales WHERE saleDate BETWEEN :startDate AND :endDate")
    fun getTotalSalesAmount(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales WHERE saleDate BETWEEN :startDate AND :endDate")
    fun getTotalSalesCount(startDate: Long, endDate: Long): Flow<Int>

    @Query("SELECT * FROM sales WHERE customerName LIKE :query ORDER BY saleDate DESC")
    fun searchSales(query: String): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE paymentStatus != 'PAID' ORDER BY saleDate DESC")
    fun getDueSales(): Flow<List<Sale>>

    @Query("SELECT SUM(finalAmount) FROM sales WHERE paymentStatus = 'PENDING'")
    fun getTotalPendingAmount(): Flow<Double?>
}
