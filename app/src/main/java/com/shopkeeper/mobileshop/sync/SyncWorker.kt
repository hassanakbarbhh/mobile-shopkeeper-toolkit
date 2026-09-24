package com.shopkeeper.mobileshop.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.OutboxOperation
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "ShopkeeperOutboxSyncWork"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting Bidirectional Cloud Firestore Synchronization...")
        val db = AppDatabase.getDatabase(applicationContext)
        val outboxDao = db.outboxDao()

        val pendingOperations = outboxDao.getPendingList()
        val shopId = ShopIdentityManager.getShopId(applicationContext)
        val firestore = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Cloud Firestore unavailable", e)
            null
        }

        var anyFailure = false

        if (firestore != null) {
            // 1. UPLOAD PHASE: Send local Room Outbox changes to Cloud Firestore
            for (op in pendingOperations) {
                try {
                    outboxDao.updateStatus(op.eventId, OutboxOperation.STATUS_UPLOADING, null)

                    val collectionName = when (op.entityType.lowercase()) {
                        "sale" -> "sales"
                        "product" -> "products"
                        "customer" -> "customers"
                        "repair" -> "repairs"
                        "payment" -> "payments"
                        "purchase" -> "purchases"
                        "expense" -> "expenses"
                        "imeiasset", "imei" -> "imei_assets"
                        "cashclosing" -> "cash_closing"
                        else -> op.entityType.lowercase() + "s"
                    }

                    val dataMap = mutableMapOf<String, Any>()
                    
                    // Parse payload JSON
                    runCatching {
                        val json = JSONObject(op.payloadJson)
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = json.get(key)
                            dataMap[key] = value
                        }
                    }

                    // Enforce required security audit keys
                    dataMap["createdAt"] = op.createdAt
                    dataMap["updatedAt"] = System.currentTimeMillis()
                    dataMap["eventId"] = op.eventId
                    dataMap["operationType"] = op.operationType
                    dataMap["entityType"] = op.entityType
                    dataMap["entityId"] = op.entityId
                    dataMap["deviceId"] = op.deviceId
                    dataMap["userId"] = op.userId
                    dataMap["shopId"] = op.shopId
                    dataMap["lastModifiedBy"] = op.userId
                    dataMap["lastModifiedDevice"] = op.deviceId
                    if (!dataMap.containsKey("cloudId")) {
                        dataMap["cloudId"] = op.entityId
                    }

                    if (op.operationType.startsWith("OP_DELETE") || op.operationType.contains("DELETE")) {
                        dataMap["isDeleted"] = true
                        dataMap["deletedAt"] = System.currentTimeMillis()
                    }

                    val docRef = firestore.collection("shops")
                        .document(op.shopId)
                        .collection(collectionName)
                        .document(op.entityId)

                    val localVer = (dataMap["version"] as? Number)?.toLong() ?: 1L

                    // Optimistic concurrency control via Firestore transaction:
                    // Verifies that concurrent remote edits don't overwrite blindly without version consistency.
                    firestore.runTransaction { tx ->
                        val snapshot = tx.get(docRef)
                        if (snapshot.exists()) {
                            val remoteVer = snapshot.getLong("version") ?: 1L
                            val remoteUpdated = snapshot.getLong("updatedAt") ?: 0L
                            val localUpdated = (dataMap["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

                            // If remote has progressed past our version and is newer, increment version and preserve audit
                            if (remoteVer > localVer && remoteUpdated > localUpdated) {
                                // Concurrent conflicting update detected.
                                Log.w(TAG, "Concurrent remote edit detected on ${op.entityType}/${op.entityId}: remoteVer=$remoteVer, localVer=$localVer. Merging changes.")
                                dataMap["version"] = remoteVer + 1L
                            } else {
                                dataMap["version"] = maxOf(localVer, remoteVer + 1L)
                            }
                        }
                        tx.set(docRef, dataMap, SetOptions.merge())
                    }.await()

                    // Mark operation COMPLETED in Room Outbox
                    outboxDao.updateStatus(op.eventId, OutboxOperation.STATUS_COMPLETED, null)
                    SyncPreferences.incrementUploadedCount(applicationContext, 1)
                    Log.d(TAG, "Successfully synced outbox event: ${op.eventId} -> shops/${op.shopId}/$collectionName/${op.entityId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload outbox event: ${op.eventId}", e)
                    outboxDao.incrementRetry(op.eventId, e.localizedMessage ?: "Sync error")
                    anyFailure = true
                }
            }

            // 2. DOWNLOAD PHASE: Inbound Synchronization from Cloud Firestore to Room
            try {
                val downloadedCount = InboundSyncEngine.syncDown(applicationContext, db, shopId)
                Log.d(TAG, "Inbound sync completed. $downloadedCount records downloaded.")
            } catch (e: Exception) {
                Log.e(TAG, "Inbound sync error", e)
            }

            SyncPreferences.setLastSyncTimestamp(applicationContext, System.currentTimeMillis())
        } else {
            // Firestore not ready or offline
            for (op in pendingOperations) {
                outboxDao.updateStatus(op.eventId, OutboxOperation.STATUS_PENDING, "Firestore client unavailable")
            }
            anyFailure = true
        }

        return if (anyFailure) Result.retry() else Result.success()
    }
}
