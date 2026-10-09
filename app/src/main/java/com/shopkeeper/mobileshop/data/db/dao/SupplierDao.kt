package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.shopkeeper.mobileshop.data.db.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert suspend fun insert(s: Supplier): Long
    @Update suspend fun update(s: Supplier)
    @Query("DELETE FROM suppliers WHERE id = :id") suspend fun deleteById(id: Long)
    @Query("DELETE FROM suppliers") suspend fun deleteAll()

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAll(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Supplier?

    @Query("UPDATE suppliers SET balance = balance + :delta, updatedAt = :updatedAt WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Double, updatedAt: Long = System.currentTimeMillis()): Int
}
