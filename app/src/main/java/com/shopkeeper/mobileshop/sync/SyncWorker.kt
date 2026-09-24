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

                    val docRef = firestore.collection("shops")
                        .document(op.shopId)
                        .collection(collectionName)
                        .document(op.entityId)

                    docRef.set(dataMap, SetOptions.merge()).await()

                    // Mark operation COMPLETED in Room Outbox
                    outboxDao.updateStatus(op.eventId, OutboxOperation.STATUS_COMPLETED, null)
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
