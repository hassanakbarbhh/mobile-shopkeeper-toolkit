package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Seller
import kotlinx.coroutines.flow.Flow

@Dao
interface SellerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seller: Seller): Long

    @Update
    suspend fun update(seller: Seller)

    @Delete
    suspend fun delete(seller: Seller)

    @Query("SELECT * FROM sellers ORDER BY name ASC")
    fun getAllSellers(): Flow<List<Seller>>

    @Query("SELECT * FROM sellers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveSellers(): Flow<List<Seller>>

    @Query("SELECT * FROM sellers WHERE id = :id")
    suspend fun getSellerById(id: Long): Seller?

    @Query("SELECT COUNT(*) FROM sellers")
    fun getSellerCount(): Flow<Int>
}
