package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(customer: Customer): Long
    @Update suspend fun update(customer: Customer)
    @Delete suspend fun delete(customer: Customer)
    @Query("DELETE FROM customers") suspend fun deleteAllCustomers()
    @Query("DELETE FROM customers WHERE name IN ('Amit Kumar', 'Pooja Sharma', 'Rahul Verma')") suspend fun deleteDummyCustomers()

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomersList(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE :query OR phone LIKE :query")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Query("SELECT COUNT(*) FROM customers")
    fun getTotalCustomerCount(): Flow<Int>
}
