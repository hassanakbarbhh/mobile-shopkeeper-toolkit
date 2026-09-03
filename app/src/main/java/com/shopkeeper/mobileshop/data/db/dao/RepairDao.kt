package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.*
import com.shopkeeper.mobileshop.data.db.entity.Repair
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(repair: Repair): Long
    @Update suspend fun update(repair: Repair)
    @Delete suspend fun delete(repair: Repair)
    @Query("DELETE FROM repairs") suspend fun deleteAllRepairs()
    @Query("DELETE FROM repairs WHERE customerName IN ('Pooja Sharma', 'Vikas Patel')") suspend fun deleteDummyRepairs()

    @Query("SELECT * FROM repairs ORDER BY receivedDate DESC")
    fun getAllRepairs(): Flow<List<Repair>>

    @Query("SELECT * FROM repairs WHERE id = :id")
    suspend fun getRepairById(id: Long): Repair?

    @Query("SELECT * FROM repairs WHERE imei = :imei ORDER BY receivedDate DESC")
    suspend fun byImei(imei: String): List<Repair>

    @Query("SELECT * FROM repairs WHERE status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY receivedDate DESC")
    fun getActiveRepairs(): Flow<List<Repair>>

    @Query("SELECT * FROM repairs WHERE status = :status ORDER BY receivedDate DESC")
    fun getRepairsByStatus(status: String): Flow<List<Repair>>

    @Query("SELECT COUNT(*) FROM repairs WHERE status NOT IN ('DELIVERED', 'CANCELLED')")
    fun getActiveRepairCount(): Flow<Int>

    @Query("SELECT SUM(actualCost) FROM repairs WHERE status IN ('COMPLETED', 'DELIVERED') AND receivedDate BETWEEN :startDate AND :endDate")
    fun getRepairRevenue(startDate: Long, endDate: Long): Flow<Double?>
}
