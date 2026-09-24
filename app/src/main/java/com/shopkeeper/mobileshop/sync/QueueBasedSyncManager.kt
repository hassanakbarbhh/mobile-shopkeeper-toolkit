package com.shopkeeper.mobileshop.sync

import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.OutboxOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

object QueueBasedSyncManager {
    private const val TAG = "QueueSync"
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        SyncEngine.init(context)
    }

    fun enqueue(path: String, data: Map<String, Any>) {
        val context = appContext
        if (context != null) {
            val entityType = path.split("/").getOrNull(0) ?: "record"
            val entityId = path.split("/").getOrNull(1) ?: System.currentTimeMillis().toString()
            val payload = JSONObject(data).toString()

            val op = OutboxOperation(
                operationType = "SYNC_$entityType".uppercase(),
                entityType = entityType,
                entityId = entityId,
                payloadJson = payload
            )
            SyncEngine.enqueueOperation(context, op)
        } else {
            // Fallback direct write
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = FirebaseDatabase.getInstance().reference
                    db.child(path).setValue(data)
                } catch (e: Exception) {
                    Log.e(TAG, "Direct sync error", e)
                }
            }
        }
    }
}
