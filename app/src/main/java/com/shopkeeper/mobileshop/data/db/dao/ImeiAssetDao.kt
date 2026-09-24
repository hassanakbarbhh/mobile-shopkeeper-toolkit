package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shopkeeper.mobileshop.data.db.entity.ImeiAsset
import com.shopkeeper.mobileshop.data.db.entity.ImeiLifecycleEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ImeiAssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: ImeiAsset)

    @Update
    suspend fun updateAsset(asset: ImeiAsset)

    @Query("SELECT * FROM imei_assets WHERE imei = :imei LIMIT 1")
    fun getAssetByImei(imei: String): Flow<ImeiAsset?>

    @Query("SELECT * FROM imei_assets WHERE imei = :imei LIMIT 1")
    suspend fun getAssetSync(imei: String): ImeiAsset?

    @Query("SELECT * FROM imei_assets WHERE productId = :productId")
    fun getAssetsByProduct(productId: Long): Flow<List<ImeiAsset>>

    @Query("SELECT * FROM imei_assets WHERE currentStatus = :status")
    fun getAssetsByStatus(status: String): Flow<List<ImeiAsset>>

    @Query("SELECT * FROM imei_assets ORDER BY updatedAt DESC")
    fun getAllAssets(): Flow<List<ImeiAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifecycleEvent(event: ImeiLifecycleEvent)

    @Query("SELECT * FROM imei_lifecycle_events WHERE imei = :imei ORDER BY timestamp ASC")
    fun getEventsForImei(imei: String): Flow<List<ImeiLifecycleEvent>>

    @Query("SELECT * FROM imei_lifecycle_events WHERE imei = :imei ORDER BY timestamp ASC")
    suspend fun getEventsForImeiSync(imei: String): List<ImeiLifecycleEvent>
}
