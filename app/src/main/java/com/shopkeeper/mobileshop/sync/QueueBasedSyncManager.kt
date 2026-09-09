package com.shopkeeper.mobileshop.sync

import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

object QueueBasedSyncManager {
    private const val TAG = "QueueSync"
    
    // Simple queue holding Map of data to sync
    private val syncQueue: Queue<Pair<String, Map<String, Any>>> = LinkedList()
    
    fun enqueue(path: String, data: Map<String, Any>) {
        syncQueue.offer(Pair(path, data))
        processQueue()
    }
    
    private fun processQueue() {
        if (syncQueue.isEmpty()) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseDatabase.getInstance().reference
                while (syncQueue.isNotEmpty()) {
                    val (path, data) = syncQueue.peek() ?: break
                    db.child(path).setValue(data)
                        .addOnSuccessListener {
                            syncQueue.poll() // Remove from queue on success
                            Log.d(TAG, "Successfully synced: $path")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Sync failed for $path", e)
                            // We leave it in the queue for retry later or fallback logic.
                        }
                    // Simple delay or wait in a real robust system. 
                    // For now, if one fails, we break the loop to retry later.
                    break 
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync process error", e)
            }
        }
    }
}
