package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shopkeeper.mobileshop.data.db.entity.CashClosing
import kotlinx.coroutines.flow.Flow

@Dao
interface CashClosingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(closing: CashClosing): Long

    @Query("SELECT * FROM cash_closing_table ORDER BY closingDate DESC")
    fun getAllClosings(): Flow<List<CashClosing>>

    @Query("SELECT * FROM cash_closing_table ORDER BY closingDate DESC LIMIT 1")
    suspend fun getLastClosing(): CashClosing?
}
