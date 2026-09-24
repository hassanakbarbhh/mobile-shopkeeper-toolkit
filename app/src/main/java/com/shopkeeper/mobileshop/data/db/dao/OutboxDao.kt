package com.shopkeeper.mobileshop.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shopkeeper.mobileshop.data.db.entity.OutboxOperation
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: OutboxOperation)

    @Update
    suspend fun update(operation: OutboxOperation)

    @Query("SELECT * FROM outbox_operations WHERE status = :status ORDER BY createdAt ASC")
    fun getOperationsByStatus(status: String = OutboxOperation.STATUS_PENDING): Flow<List<OutboxOperation>>

    @Query("SELECT * FROM outbox_operations WHERE status IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    suspend fun getPendingList(): List<OutboxOperation>

    @Query("SELECT COUNT(*) FROM outbox_operations WHERE status = 'PENDING' OR status = 'FAILED'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox_operations WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox_operations WHERE status = 'FAILED'")
    fun getFailedCount(): Flow<Int>

    @Query("SELECT * FROM outbox_operations ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentOperations(limit: Int = 50): Flow<List<OutboxOperation>>

    @Query("UPDATE outbox_operations SET status = :status, lastAttemptAt = :time, errorMessage = :error WHERE eventId = :id")
    suspend fun updateStatus(id: String, status: String, error: String? = null, time: Long = System.currentTimeMillis())

    @Query("UPDATE outbox_operations SET retryCount = retryCount + 1, lastAttemptAt = :time, errorMessage = :error, status = 'FAILED' WHERE eventId = :id")
    suspend fun incrementRetry(id: String, error: String?, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM outbox_operations WHERE status = 'COMPLETED' AND createdAt < :cutoffTime")
    suspend fun deleteOldCompleted(cutoffTime: Long)

    @Delete
    suspend fun delete(operation: OutboxOperation)
}
