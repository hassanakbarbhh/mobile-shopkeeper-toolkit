package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert suspend fun insert(s: Supplier): Long
    @Update suspend fun update(s: Supplier)
    @Delete suspend fun delete(s: Supplier)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAll(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: Long): Supplier?
}
