package com.shopkeeper.mobileshop.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.OutboxOperation
import kotlinx.coroutines.tasks.await

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "ShopkeeperOutboxSyncWork"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting Outbox Synchronization Work...")
        val db = AppDatabase.getDatabase(applicationContext)
        val outboxDao = db.outboxDao()

        val pendingOperations = outboxDao.getPendingList()
        if (pendingOperations.isEmpty()) {
            Log.d(TAG, "Outbox is clear. Zero pending operations.")
            return Result.success()
        }

        var anyFailure = false
        val firebaseRef = try {
            FirebaseDatabase.getInstance().reference
        } catch (e: Exception) {
            Log.e(TAG, "Firebase unavailable", e)
            null
        }

        for (op in pendingOperations) {
            try {
                outboxDao.updateStatus(op.eventId, OutboxOperation.STATUS_UPLOADING, null)

                if (firebaseRef != null) {
                    val path = "organizations/${op.shopId}/shops/${op.shopId}/${op.entityType.lowercase()}/${op.entityId}"
                    
                    // Construct sync payload
                    val syncData = mapOf(
                        "eventId" to op.eventId,
                        "operation" to op.operationType,
                        "entityType" to op.entityType,
                        "entityId" to op.entityId,
                        "payload" to op.payloadJson,
                        "timestamp" to op.createdAt,
                        "deviceId" to op.deviceId,
                        "userId" to op.userId
                    )

                    firebaseRef.child(path).setValue(syncData).await()
                    
                    // Mark Completed
                    outboxDao.updateStatus(op.eventId, OutboxOperation.STATUS_COMPLETED, null)
                    Log.d(TAG, "Successfully synced outbox event: ${op.eventId} (${op.operationType})")
                } else {
                    // Offline - keep as pending
                    outboxDao.updateStatus(op.eventId, OutboxOperation.STATUS_PENDING, "Firebase client offline")
                    anyFailure = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync outbox event: ${op.eventId}", e)
                outboxDao.incrementRetry(op.eventId, e.localizedMessage ?: "Sync error")
                anyFailure = true
            }
        }

        return if (anyFailure) Result.retry() else Result.success()
    }
}
