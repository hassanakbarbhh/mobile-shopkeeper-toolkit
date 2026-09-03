package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(product: Product): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(products: List<Product>)
    @Update suspend fun update(product: Product)
    @Delete suspend fun delete(product: Product)
    @Query("DELETE FROM products WHERE id = :id") suspend fun deleteById(id: Long)
    @Query("DELETE FROM products") suspend fun deleteAllProducts()

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsList(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE imei = :imei LIMIT 1")
    suspend fun getByImei(imei: String): Product?

    @Query("SELECT * FROM products WHERE name LIKE :query OR brand LIKE :query OR model LIKE :query OR imei LIKE :query")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE quantity <= :threshold ORDER BY quantity ASC")
    fun getLowStockProducts(threshold: Int = 5): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT SUM(quantity * purchasePrice) FROM products")
    fun getTotalInventoryValue(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductCount(): Flow<Int>

    @Query("UPDATE products SET quantity = quantity - :qty WHERE id = :productId AND quantity >= :qty")
    suspend fun reduceStock(productId: Long, qty: Int): Int

    @Query("UPDATE products SET quantity = quantity + :qty WHERE id = :productId")
    suspend fun increaseStock(productId: Long, qty: Int)
}
