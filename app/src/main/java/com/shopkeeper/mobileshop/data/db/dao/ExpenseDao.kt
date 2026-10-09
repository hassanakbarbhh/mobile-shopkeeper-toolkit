package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert suspend fun insert(e: Expense): Long
    @Update suspend fun update(e: Expense)
    @Delete suspend fun delete(e: Expense)
    @Query("SELECT COUNT(*) FROM expenses WHERE isDeleted = 1") suspend fun deleteDummyExpenses(): Int

    @Query("SELECT * FROM expenses WHERE cloudId = :cloudId LIMIT 1")
    suspend fun getByCloudId(cloudId: String): Expense?

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :start AND :end")
    fun sumInRange(start: Long, end: Long): Flow<Double?>
}
