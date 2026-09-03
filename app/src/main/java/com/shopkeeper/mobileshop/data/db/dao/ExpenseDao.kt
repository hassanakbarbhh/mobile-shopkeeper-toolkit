package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert suspend fun insert(e: Expense): Long
    @Delete suspend fun delete(e: Expense)
    @Query("DELETE FROM expenses WHERE title LIKE 'Shop Rent%' OR title LIKE 'Electricity Bill%' OR title LIKE 'Broadband Internet%'") suspend fun deleteDummyExpenses()

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :start AND :end")
    fun sumInRange(start: Long, end: Long): Flow<Double?>
}
