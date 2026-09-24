package com.shopkeeper.mobileshop.sync

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import kotlinx.coroutines.tasks.await

object InboundSyncEngine {
    private const val TAG = "InboundSyncEngine"

    suspend fun syncDown(context: Context, db: AppDatabase, shopId: String): Int {
        var totalApplied = 0
        try {
            val firestore = FirebaseFirestore.getInstance()
            val shopRef = firestore.collection("shops").document(shopId)
            val cursor = SyncPreferences.getLastSyncCursor(context)
            val thisSyncStart = System.currentTimeMillis()

            // 1. Inbound Products
            val productDocs = runCatching {
                shopRef.collection("products")
                    .whereGreaterThan("updatedAt", cursor)
                    .get()
                    .await()
            }.getOrNull()

            if (productDocs != null && !productDocs.isEmpty) {
                for (doc in productDocs.documents) {
                    try {
                        val name = doc.getString("name") ?: continue
                        val brand = doc.getString("brand") ?: ""
                        val model = doc.getString("model") ?: ""
                        val imei = doc.getString("imei") ?: ""
                        val catStr = doc.getString("category") ?: ProductCategory.SMARTPHONE.name
                        val category = runCatching { ProductCategory.valueOf(catStr) }.getOrDefault(ProductCategory.SMARTPHONE)
                        val purchasePrice = doc.getDouble("purchasePrice") ?: 0.0
                        val sellingPrice = doc.getDouble("sellingPrice") ?: 0.0
                        val quantity = doc.getLong("quantity")?.toInt() ?: 1
                        val ram = doc.getString("ram") ?: ""
                        val storage = doc.getString("storage") ?: ""
                        val color = doc.getString("color") ?: ""

                        val existing = if (imei.isNotBlank()) db.productDao().getByImei(imei) else null
                        if (existing != null) {
                            val updated = existing.copy(
                                name = name,
                                brand = brand,
                                model = model,
                                category = category,
                                purchasePrice = purchasePrice,
                                sellingPrice = sellingPrice,
                                quantity = quantity,
                                ram = ram,
                                storage = storage,
                                color = color,
                                updatedAt = System.currentTimeMillis()
                            )
                            db.productDao().update(updated)
                        } else {
                            val newProduct = Product(
                                name = name,
                                brand = brand,
                                model = model,
                                imei = imei,
                                category = category,
                                purchasePrice = purchasePrice,
                                sellingPrice = sellingPrice,
                                quantity = quantity,
                                ram = ram,
                                storage = storage,
                                color = color,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            db.productDao().insert(newProduct)
                        }
                        totalApplied++
                    } catch (e: Exception) {
                        Log.e(TAG, "Error applying inbound product ${doc.id}", e)
                    }
                }
            }

            // 2. Inbound Customers
            val customerDocs = runCatching {
                shopRef.collection("customers")
                    .whereGreaterThan("updatedAt", cursor)
                    .get()
                    .await()
            }.getOrNull()

            if (customerDocs != null && !customerDocs.isEmpty) {
                for (doc in customerDocs.documents) {
                    try {
                        val name = doc.getString("name") ?: continue
                        val phone = doc.getString("phone") ?: ""
                        val email = doc.getString("email") ?: ""
                        val address = doc.getString("address") ?: ""
                        val notes = doc.getString("notes") ?: ""

                        val newCustomer = Customer(
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            notes = notes,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                        db.customerDao().insert(newCustomer)
                        totalApplied++
                    } catch (e: Exception) {
                        Log.e(TAG, "Error applying inbound customer ${doc.id}", e)
                    }
                }
            }

            // 3. Inbound IMEI Assets
            val imeiDocs = runCatching {
                shopRef.collection("imei_assets")
                    .whereGreaterThan("updatedAt", cursor)
                    .get()
                    .await()
            }.getOrNull()

            if (imeiDocs != null && !imeiDocs.isEmpty) {
                for (doc in imeiDocs.documents) {
                    try {
                        val imei = doc.getString("imei") ?: doc.id
                        val brand = doc.getString("brand") ?: ""
                        val model = doc.getString("model") ?: ""
                        val storage = doc.getString("storage") ?: "128GB"
                        val color = doc.getString("color") ?: ""
                        val ptaStatus = doc.getString("ptaStatus") ?: "Approved"
                        val purchasePrice = doc.getDouble("purchasePrice") ?: 0.0
                        val sellingPrice = doc.getDouble("sellingPrice") ?: 0.0
                        val warrantyExpiryDate = doc.getLong("warrantyExpiryDate") ?: 0L
                        val supplierName = doc.getString("supplierName") ?: ""
                        val currentBranch = doc.getString("currentBranch") ?: "Main Branch"
                        val currentStatus = doc.getString("currentStatus") ?: "IN_STOCK"
                        val customerName = doc.getString("customerName") ?: ""
                        val customerPhone = doc.getString("customerPhone") ?: ""

                        val asset = ImeiAsset(
                            imei = imei,
                            brand = brand,
                            model = model,
                            storage = storage,
                            color = color,
                            ptaStatus = ptaStatus,
                            purchasePrice = purchasePrice,
                            sellingPrice = sellingPrice,
                            warrantyExpiryDate = warrantyExpiryDate,
                            supplierName = supplierName,
                            currentBranch = currentBranch,
                            currentStatus = currentStatus,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            updatedAt = System.currentTimeMillis()
                        )
                        db.imeiAssetDao().insertAsset(asset)
                        totalApplied++
                    } catch (e: Exception) {
                        Log.e(TAG, "Error applying inbound IMEI asset ${doc.id}", e)
                    }
                }
            }

            // 4. Inbound Repairs (Technician updates from another device)
            val repairDocs = runCatching {
                shopRef.collection("repairs")
                    .whereGreaterThan("updatedAt", cursor)
                    .get()
                    .await()
            }.getOrNull()

            if (repairDocs != null && !repairDocs.isEmpty) {
                for (doc in repairDocs.documents) {
                    try {
                        val idStr = doc.getString("entityId") ?: doc.id
                        val localId = idStr.toLongOrNull() ?: 0L
                        val statusStr = doc.getString("status") ?: RepairStatus.RECEIVED.name
                        val status = runCatching { RepairStatus.valueOf(statusStr) }.getOrDefault(RepairStatus.RECEIVED)
                        val actualCost = doc.getDouble("actualCost") ?: 0.0
                        val notes = doc.getString("notes") ?: ""

                        if (localId > 0) {
                            val existing = db.repairDao().getRepairById(localId)
                            if (existing != null) {
                                val updated = existing.copy(
                                    status = status,
                                    actualCost = actualCost,
                                    notes = notes
                                )
                                db.repairDao().update(updated)
                                totalApplied++
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error applying inbound repair ${doc.id}", e)
                    }
                }
            }

            // Save Sync Preferences
            if (totalApplied > 0) {
                SyncPreferences.incrementDownloadedCount(context, totalApplied)
            }
            SyncPreferences.setLastSyncCursor(context, thisSyncStart)
            SyncPreferences.setLastSyncTimestamp(context, thisSyncStart)
            Log.d(TAG, "Inbound sync completed. $totalApplied entities updated from cloud.")
        } catch (e: Exception) {
            Log.e(TAG, "Inbound sync encountered error", e)
            SyncPreferences.setLastError(context, e.localizedMessage)
        }
        return totalApplied
    }
}
