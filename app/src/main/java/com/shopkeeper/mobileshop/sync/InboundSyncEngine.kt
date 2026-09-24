package com.shopkeeper.mobileshop.sync

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.google.firebase.firestore.DocumentSnapshot
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
            val outboxDao = db.outboxDao()

            // 1. Inbound Products
            totalApplied += syncProducts(db, shopRef, cursor, outboxDao)

            // 2. Inbound Customers
            totalApplied += syncCustomers(db, shopRef, cursor, outboxDao)

            // 3. Inbound IMEI Assets
            totalApplied += syncImeiAssets(db, shopRef, cursor, outboxDao)

            // 4. Inbound Repairs
            totalApplied += syncRepairs(db, shopRef, cursor, outboxDao)

            // 5. Inbound Sales (with transaction-safe inventory & IMEI updates)
            totalApplied += syncSales(db, shopRef, cursor, outboxDao)

            // 6. Inbound Expenses
            totalApplied += syncExpenses(db, shopRef, cursor, outboxDao)

            // 7. Inbound Cash Closings
            totalApplied += syncCashClosings(db, shopRef, cursor, outboxDao)

            // 8. Inbound Payments
            totalApplied += syncPayments(db, shopRef, cursor, outboxDao)

            // 9. Inbound Suppliers
            totalApplied += syncSuppliers(db, shopRef, cursor, outboxDao)

            // Persist metrics & sync high-water mark
            if (totalApplied > 0) {
                SyncPreferences.incrementDownloadedCount(context, totalApplied)
            }
            SyncPreferences.setLastSyncCursor(context, thisSyncStart)
            SyncPreferences.setLastSyncTimestamp(context, thisSyncStart)
            Log.d(TAG, "Inbound sync completed successfully. $totalApplied entities synchronized from cloud.")
        } catch (e: Exception) {
            Log.e(TAG, "Inbound sync encountered error", e)
            SyncPreferences.setLastError(context, e.localizedMessage)
        }
        return totalApplied
    }

    /**
     * Determines whether an incoming remote document should overwrite the local state.
     * Prevents clobbering unsynchronized offline local mutations unless remote is strictly newer.
     */
    private fun shouldApplyRemote(
        hasPendingLocal: Boolean,
        localVersion: Long,
        remoteVersion: Long,
        localUpdatedAt: Long,
        remoteUpdatedAt: Long
    ): Boolean {
        return if (hasPendingLocal) {
            // Local device has an uncommitted mutation in Outbox.
            // Only overwrite if remote is strictly newer (both version and timestamp).
            remoteVersion > localVersion && remoteUpdatedAt > localUpdatedAt
        } else {
            // No local pending edits: standard optimistic versioning & timestamp comparison
            remoteVersion >= localVersion || remoteUpdatedAt >= localUpdatedAt
        }
    }

    private suspend fun syncProducts(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("products")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val cloudId = doc.id
                val imei = doc.getString("imei") ?: ""
                val hasPending = outboxDao.hasPendingMutation("Product", cloudId)
                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                val isDeleted = doc.getBoolean("isDeleted") == true || (doc.getLong("deletedAt") ?: 0L) > 0L
                val deletedAt = doc.getLong("deletedAt")

                val existing = db.productDao().getByCloudId(cloudId)
                    ?: if (imei.isNotBlank()) db.productDao().getByImei(imei) else null

                if (existing != null) {
                    if (!shouldApplyRemote(hasPending, existing.version, remoteVersion, existing.updatedAt, remoteUpdatedAt)) {
                        continue
                    }
                    if (isDeleted) {
                        db.productDao().update(existing.copy(
                            isDeleted = true,
                            deletedAt = deletedAt ?: System.currentTimeMillis(),
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt
                        ))
                    } else {
                        val catStr = doc.getString("category") ?: existing.category.name
                        val category = runCatching { ProductCategory.valueOf(catStr) }.getOrDefault(existing.category)
                        val updated = existing.copy(
                            cloudId = cloudId,
                            name = doc.getString("name") ?: existing.name,
                            brand = doc.getString("brand") ?: existing.brand,
                            model = doc.getString("model") ?: existing.model,
                            imei = imei,
                            category = category,
                            purchasePrice = doc.getDouble("purchasePrice") ?: existing.purchasePrice,
                            sellingPrice = doc.getDouble("sellingPrice") ?: existing.sellingPrice,
                            quantity = doc.getLong("quantity")?.toInt() ?: existing.quantity,
                            ram = doc.getString("ram") ?: existing.ram,
                            storage = doc.getString("storage") ?: existing.storage,
                            color = doc.getString("color") ?: existing.color,
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt,
                            isDeleted = false,
                            deletedAt = null,
                            lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                            lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                        )
                        db.productDao().update(updated)
                    }
                    applied++
                } else if (!isDeleted) {
                    val name = doc.getString("name") ?: continue
                    val catStr = doc.getString("category") ?: ProductCategory.SMARTPHONE.name
                    val category = runCatching { ProductCategory.valueOf(catStr) }.getOrDefault(ProductCategory.SMARTPHONE)
                    val newProduct = Product(
                        cloudId = cloudId,
                        name = name,
                        brand = doc.getString("brand") ?: "",
                        model = doc.getString("model") ?: "",
                        imei = imei,
                        category = category,
                        purchasePrice = doc.getDouble("purchasePrice") ?: 0.0,
                        sellingPrice = doc.getDouble("sellingPrice") ?: 0.0,
                        quantity = doc.getLong("quantity")?.toInt() ?: 1,
                        ram = doc.getString("ram") ?: "",
                        storage = doc.getString("storage") ?: "",
                        color = doc.getString("color") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = remoteUpdatedAt,
                        version = remoteVersion,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    db.productDao().insert(newProduct)
                    applied++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound product ${doc.id}", e)
            }
        }
        return applied
    }

    private suspend fun syncCustomers(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("customers")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val cloudId = doc.id
                val phone = doc.getString("phone") ?: ""
                val hasPending = outboxDao.hasPendingMutation("Customer", cloudId)
                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                val isDeleted = doc.getBoolean("isDeleted") == true || (doc.getLong("deletedAt") ?: 0L) > 0L
                val deletedAt = doc.getLong("deletedAt")

                // Map cross-device identity by cloudId, fallback by phone to avoid duplicates
                val existing = db.customerDao().getByCloudId(cloudId)
                    ?: if (phone.isNotBlank()) db.customerDao().getByPhone(phone) else null

                if (existing != null) {
                    if (!shouldApplyRemote(hasPending, existing.version, remoteVersion, existing.updatedAt, remoteUpdatedAt)) {
                        continue
                    }
                    if (isDeleted) {
                        db.customerDao().update(existing.copy(
                            isDeleted = true,
                            deletedAt = deletedAt ?: System.currentTimeMillis(),
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt
                        ))
                    } else {
                        val updated = existing.copy(
                            cloudId = cloudId,
                            name = doc.getString("name") ?: existing.name,
                            phone = phone.ifBlank { existing.phone },
                            email = doc.getString("email") ?: existing.email,
                            address = doc.getString("address") ?: existing.address,
                            notes = doc.getString("notes") ?: existing.notes,
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt,
                            isDeleted = false,
                            deletedAt = null,
                            lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                            lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                        )
                        db.customerDao().update(updated)
                    }
                    applied++
                } else if (!isDeleted) {
                    val name = doc.getString("name") ?: continue
                    val newCustomer = Customer(
                        cloudId = cloudId,
                        name = name,
                        phone = phone,
                        email = doc.getString("email") ?: "",
                        address = doc.getString("address") ?: "",
                        notes = doc.getString("notes") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = remoteUpdatedAt,
                        version = remoteVersion,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    db.customerDao().insert(newCustomer)
                    applied++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound customer ${doc.id}", e)
            }
        }
        return applied
    }

    private suspend fun syncImeiAssets(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("imei_assets")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val imei = doc.getString("imei") ?: doc.id
                val hasPending = outboxDao.hasPendingMutation("ImeiAsset", imei)
                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                val isDeleted = doc.getBoolean("isDeleted") == true || (doc.getLong("deletedAt") ?: 0L) > 0L
                val deletedAt = doc.getLong("deletedAt")

                val existing = db.imeiAssetDao().getAssetSync(imei)
                if (existing != null) {
                    if (!shouldApplyRemote(hasPending, existing.version, remoteVersion, existing.updatedAt, remoteUpdatedAt)) {
                        continue
                    }
                    if (isDeleted) {
                        db.imeiAssetDao().updateAsset(existing.copy(
                            isDeleted = true,
                            deletedAt = deletedAt ?: System.currentTimeMillis(),
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt
                        ))
                    } else {
                        val updated = existing.copy(
                            cloudId = imei,
                            brand = doc.getString("brand") ?: existing.brand,
                            model = doc.getString("model") ?: existing.model,
                            storage = doc.getString("storage") ?: existing.storage,
                            color = doc.getString("color") ?: existing.color,
                            ptaStatus = doc.getString("ptaStatus") ?: existing.ptaStatus,
                            purchasePrice = doc.getDouble("purchasePrice") ?: existing.purchasePrice,
                            sellingPrice = doc.getDouble("sellingPrice") ?: existing.sellingPrice,
                            warrantyExpiryDate = doc.getLong("warrantyExpiryDate") ?: existing.warrantyExpiryDate,
                            supplierName = doc.getString("supplierName") ?: existing.supplierName,
                            currentBranch = doc.getString("currentBranch") ?: existing.currentBranch,
                            currentStatus = doc.getString("currentStatus") ?: existing.currentStatus,
                            customerName = doc.getString("customerName") ?: existing.customerName,
                            customerPhone = doc.getString("customerPhone") ?: existing.customerPhone,
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt,
                            isDeleted = false,
                            deletedAt = null,
                            lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                            lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                        )
                        db.imeiAssetDao().updateAsset(updated)
                    }
                    applied++
                } else if (!isDeleted) {
                    val asset = ImeiAsset(
                        imei = imei,
                        cloudId = imei,
                        brand = doc.getString("brand") ?: "",
                        model = doc.getString("model") ?: "",
                        storage = doc.getString("storage") ?: "128GB",
                        color = doc.getString("color") ?: "Black",
                        ptaStatus = doc.getString("ptaStatus") ?: "Approved",
                        purchasePrice = doc.getDouble("purchasePrice") ?: 0.0,
                        sellingPrice = doc.getDouble("sellingPrice") ?: 0.0,
                        warrantyExpiryDate = doc.getLong("warrantyExpiryDate") ?: 0L,
                        supplierName = doc.getString("supplierName") ?: "",
                        currentBranch = doc.getString("currentBranch") ?: "Main Branch",
                        currentStatus = doc.getString("currentStatus") ?: ImeiAsset.STATUS_IN_STOCK,
                        customerName = doc.getString("customerName") ?: "",
                        customerPhone = doc.getString("customerPhone") ?: "",
                        updatedAt = remoteUpdatedAt,
                        version = remoteVersion,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    db.imeiAssetDao().insertAsset(asset)
                    applied++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound IMEI asset ${doc.id}", e)
            }
        }
        return applied
    }

    private suspend fun syncRepairs(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("repairs")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val cloudId = doc.id
                val hasPending = outboxDao.hasPendingMutation("Repair", cloudId)
                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                val isDeleted = doc.getBoolean("isDeleted") == true || (doc.getLong("deletedAt") ?: 0L) > 0L
                val deletedAt = doc.getLong("deletedAt")

                val existing = db.repairDao().getRepairByCloudId(cloudId)
                    ?: doc.getString("entityId")?.toLongOrNull()?.let { db.repairDao().getRepairById(it) }

                if (existing != null) {
                    if (!shouldApplyRemote(hasPending, existing.version, remoteVersion, existing.updatedAt, remoteUpdatedAt)) {
                        continue
                    }
                    if (isDeleted) {
                        db.repairDao().update(existing.copy(
                            isDeleted = true,
                            deletedAt = deletedAt ?: System.currentTimeMillis(),
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt
                        ))
                    } else {
                        val statusStr = doc.getString("status") ?: existing.status.name
                        val status = runCatching { RepairStatus.valueOf(statusStr) }.getOrDefault(existing.status)
                        val updated = existing.copy(
                            cloudId = cloudId,
                            status = status,
                            actualCost = doc.getDouble("actualCost") ?: existing.actualCost,
                            notes = doc.getString("notes") ?: existing.notes,
                            estimatedCost = doc.getDouble("estimatedCost") ?: existing.estimatedCost,
                            deliveryDate = doc.getLong("deliveryDate") ?: existing.deliveryDate,
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt,
                            isDeleted = false,
                            deletedAt = null,
                            lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                            lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                        )
                        db.repairDao().update(updated)
                    }
                    applied++
                } else if (!isDeleted) {
                    val statusStr = doc.getString("status") ?: RepairStatus.RECEIVED.name
                    val status = runCatching { RepairStatus.valueOf(statusStr) }.getOrDefault(RepairStatus.RECEIVED)
                    val newRepair = Repair(
                        cloudId = cloudId,
                        customerName = doc.getString("customerName") ?: "Walk-in",
                        customerPhone = doc.getString("customerPhone") ?: "",
                        deviceBrand = doc.getString("deviceBrand") ?: "",
                        deviceModel = doc.getString("deviceModel") ?: "",
                        imei = doc.getString("imei") ?: "",
                        issueDescription = doc.getString("issueDescription") ?: "",
                        estimatedCost = doc.getDouble("estimatedCost") ?: 0.0,
                        actualCost = doc.getDouble("actualCost") ?: 0.0,
                        status = status,
                        receivedDate = doc.getLong("receivedDate") ?: System.currentTimeMillis(),
                        deliveryDate = doc.getLong("deliveryDate"),
                        notes = doc.getString("notes") ?: "",
                        version = remoteVersion,
                        updatedAt = remoteUpdatedAt,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    db.repairDao().insert(newRepair)
                    applied++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound repair ${doc.id}", e)
            }
        }
        return applied
    }

    /**
     * Inbound Sales Sync:
     * Atomically registers the sale and adjusts inventory/IMEI status across devices.
     */
    private suspend fun syncSales(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("sales")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val cloudId = doc.id
                val existing = db.saleDao().getSaleByCloudId(cloudId)
                if (existing != null) {
                    // Sales are immutable audit events; already processed locally
                    continue
                }

                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                val finalAmount = doc.getDouble("finalAmount") ?: totalAmount
                val discount = doc.getDouble("discount") ?: 0.0
                val taxAmount = doc.getDouble("taxAmount") ?: 0.0
                val customerName = doc.getString("customerName") ?: "Walk-in Customer"
                val sellerName = doc.getString("sellerName") ?: "Staff"
                val paymentMethodStr = doc.getString("paymentMethod") ?: PaymentMethod.CASH.name
                val paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethodStr) }.getOrDefault(PaymentMethod.CASH)
                val paymentStatusStr = doc.getString("paymentStatus") ?: PaymentStatus.PAID.name
                val paymentStatus = runCatching { PaymentStatus.valueOf(paymentStatusStr) }.getOrDefault(PaymentStatus.PAID)
                val saleDate = doc.getLong("saleDate") ?: System.currentTimeMillis()

                // Execute atomic local transaction
                db.withTransaction {
                    val sale = Sale(
                        cloudId = cloudId,
                        customerName = customerName,
                        totalAmount = totalAmount,
                        discount = discount,
                        taxAmount = taxAmount,
                        finalAmount = finalAmount,
                        paymentMethod = paymentMethod,
                        paymentStatus = paymentStatus,
                        sellerName = sellerName,
                        notes = doc.getString("notes") ?: "",
                        saleDate = saleDate,
                        version = remoteVersion,
                        updatedAt = remoteUpdatedAt,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    val insertedSaleId = db.saleDao().insertSale(sale)

                    // Process sale items and decrement local inventory
                    val itemsRaw = doc.get("items") as? List<Map<String, Any>>
                    if (!itemsRaw.isNullOrEmpty()) {
                        val saleItems = mutableListOf<SaleItem>()
                        for (rawItem in itemsRaw) {
                            val productName = rawItem["productName"]?.toString() ?: ""
                            val quantity = (rawItem["quantity"] as? Number)?.toInt() ?: 1
                            val unitPrice = (rawItem["unitPrice"] as? Number)?.toDouble() ?: 0.0
                            val totalPrice = (rawItem["totalPrice"] as? Number)?.toDouble() ?: (unitPrice * quantity)
                            val imei = rawItem["imei"]?.toString() ?: ""
                            val productId = (rawItem["productId"] as? Number)?.toLong() ?: 0L

                            saleItems.add(
                                SaleItem(
                                    saleId = insertedSaleId,
                                    productId = productId,
                                    productName = productName,
                                    quantity = quantity,
                                    unitPrice = unitPrice,
                                    totalPrice = totalPrice,
                                    imei = imei
                                )
                            )

                            // Atomic inventory decrement
                            if (productId > 0) {
                                db.productDao().reduceStock(productId, quantity)
                            }
                            // Atomic IMEI status update
                            if (imei.isNotBlank()) {
                                val asset = db.imeiAssetDao().getAssetSync(imei)
                                if (asset != null && asset.currentStatus != ImeiAsset.STATUS_SOLD) {
                                    db.imeiAssetDao().updateAsset(
                                        asset.copy(
                                            currentStatus = ImeiAsset.STATUS_SOLD,
                                            customerName = customerName,
                                            saleInvoiceId = insertedSaleId,
                                            updatedAt = remoteUpdatedAt
                                        )
                                    )
                                }
                            }
                        }
                        db.saleDao().insertSaleItems(saleItems)
                    }
                }
                applied++
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound sale ${doc.id}", e)
            }
        }
        return applied
    }

    private suspend fun syncExpenses(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("expenses")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val cloudId = doc.id
                val hasPending = outboxDao.hasPendingMutation("Expense", cloudId)
                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                val isDeleted = doc.getBoolean("isDeleted") == true || (doc.getLong("deletedAt") ?: 0L) > 0L
                val deletedAt = doc.getLong("deletedAt")

                val existing = db.expenseDao().getByCloudId(cloudId)
                if (existing != null) {
                    if (!shouldApplyRemote(hasPending, existing.version, remoteVersion, existing.updatedAt, remoteUpdatedAt)) {
                        continue
                    }
                    if (isDeleted) {
                        db.expenseDao().update(existing.copy(
                            isDeleted = true,
                            deletedAt = deletedAt ?: System.currentTimeMillis(),
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt
                        ))
                    } else {
                        val catStr = doc.getString("category") ?: existing.category.name
                        val category = runCatching { ExpenseCategory.valueOf(catStr) }.getOrDefault(existing.category)
                        val updated = existing.copy(
                            cloudId = cloudId,
                            title = doc.getString("title") ?: existing.title,
                            category = category,
                            amount = doc.getDouble("amount") ?: existing.amount,
                            notes = doc.getString("notes") ?: existing.notes,
                            date = doc.getLong("date") ?: existing.date,
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt,
                            isDeleted = false,
                            deletedAt = null,
                            lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                            lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                        )
                        db.expenseDao().update(updated)
                    }
                    applied++
                } else if (!isDeleted) {
                    val title = doc.getString("title") ?: continue
                    val catStr = doc.getString("category") ?: ExpenseCategory.OTHER.name
                    val category = runCatching { ExpenseCategory.valueOf(catStr) }.getOrDefault(ExpenseCategory.OTHER)
                    val newExpense = Expense(
                        cloudId = cloudId,
                        title = title,
                        category = category,
                        amount = doc.getDouble("amount") ?: 0.0,
                        notes = doc.getString("notes") ?: "",
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        version = remoteVersion,
                        updatedAt = remoteUpdatedAt,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    db.expenseDao().insert(newExpense)
                    applied++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound expense ${doc.id}", e)
            }
        }
        return applied
    }

    private suspend fun syncCashClosings(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("cash_closing")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val cloudId = doc.id
                val existing = db.cashClosingDao().getByCloudId(cloudId)
                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()

                if (existing != null) {
                    if (remoteVersion > existing.version) {
                        val updated = existing.copy(
                            cloudId = cloudId,
                            openingCash = doc.getDouble("openingCash") ?: existing.openingCash,
                            cashIn = doc.getDouble("cashIn") ?: existing.cashIn,
                            cashOut = doc.getDouble("cashOut") ?: existing.cashOut,
                            countedCash = doc.getDouble("countedCash") ?: existing.countedCash,
                            variance = doc.getDouble("variance") ?: existing.variance,
                            signedBy = doc.getString("signedBy") ?: existing.signedBy,
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt
                        )
                        db.cashClosingDao().update(updated)
                        applied++
                    }
                } else {
                    val closing = CashClosing(
                        cloudId = cloudId,
                        closingDate = doc.getLong("closingDate") ?: System.currentTimeMillis(),
                        openingCash = doc.getDouble("openingCash") ?: 0.0,
                        cashIn = doc.getDouble("cashIn") ?: 0.0,
                        cashOut = doc.getDouble("cashOut") ?: 0.0,
                        countedCash = doc.getDouble("countedCash") ?: 0.0,
                        variance = doc.getDouble("variance") ?: 0.0,
                        signedBy = doc.getString("signedBy") ?: "Staff",
                        version = remoteVersion,
                        updatedAt = remoteUpdatedAt,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    db.cashClosingDao().insert(closing)
                    applied++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound cash closing ${doc.id}", e)
            }
        }
        return applied
    }

    private suspend fun syncPayments(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("payments")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val cloudId = doc.id
                val existing = db.paymentDao().getByCloudId(cloudId)
                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()

                if (existing != null) {
                    if (remoteVersion > existing.version) {
                        val methodStr = doc.getString("paymentMethod") ?: existing.paymentMethod.name
                        val method = runCatching { PaymentMethod.valueOf(methodStr) }.getOrDefault(existing.paymentMethod)
                        val typeStr = doc.getString("paymentType") ?: existing.paymentType.name
                        val type = runCatching { PaymentType.valueOf(typeStr) }.getOrDefault(existing.paymentType)
                        val updated = existing.copy(
                            amount = doc.getDouble("amount") ?: existing.amount,
                            paymentMethod = method,
                            paymentType = type,
                            reference = doc.getString("reference") ?: existing.reference,
                            notes = doc.getString("notes") ?: existing.notes,
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt
                        )
                        db.paymentDao().update(updated)
                        applied++
                    }
                } else {
                    val methodStr = doc.getString("paymentMethod") ?: PaymentMethod.CASH.name
                    val method = runCatching { PaymentMethod.valueOf(methodStr) }.getOrDefault(PaymentMethod.CASH)
                    val typeStr = doc.getString("paymentType") ?: PaymentType.RECEIVED.name
                    val type = runCatching { PaymentType.valueOf(typeStr) }.getOrDefault(PaymentType.RECEIVED)
                    val payment = Payment(
                        cloudId = cloudId,
                        saleId = doc.getLong("saleId"),
                        customerId = doc.getLong("customerId"),
                        amount = doc.getDouble("amount") ?: 0.0,
                        paymentMethod = method,
                        paymentType = type,
                        reference = doc.getString("reference") ?: "",
                        notes = doc.getString("notes") ?: "",
                        paymentDate = doc.getLong("paymentDate") ?: System.currentTimeMillis(),
                        version = remoteVersion,
                        updatedAt = remoteUpdatedAt,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    db.paymentDao().insert(payment)
                    applied++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound payment ${doc.id}", e)
            }
        }
        return applied
    }

    private suspend fun syncSuppliers(
        db: AppDatabase,
        shopRef: com.google.firebase.firestore.DocumentReference,
        cursor: Long,
        outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao
    ): Int {
        var applied = 0
        val docs = runCatching {
            shopRef.collection("suppliers")
                .whereGreaterThan("updatedAt", cursor)
                .get()
                .await()
        }.getOrNull() ?: return 0

        for (doc in docs.documents) {
            try {
                val cloudId = doc.id
                val existing = db.supplierDao().getByCloudId(cloudId)
                val remoteVersion = doc.getLong("version") ?: 1L
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()

                if (existing != null) {
                    if (remoteVersion > existing.version) {
                        val updated = existing.copy(
                            name = doc.getString("name") ?: existing.name,
                            phone = doc.getString("phone") ?: existing.phone,
                            company = doc.getString("company") ?: existing.company,
                            address = doc.getString("address") ?: existing.address,
                            notes = doc.getString("notes") ?: existing.notes,
                            version = remoteVersion,
                            updatedAt = remoteUpdatedAt
                        )
                        db.supplierDao().update(updated)
                        applied++
                    }
                } else {
                    val name = doc.getString("name") ?: continue
                    val supplier = Supplier(
                        cloudId = cloudId,
                        name = name,
                        phone = doc.getString("phone") ?: "",
                        company = doc.getString("company") ?: "",
                        address = doc.getString("address") ?: "",
                        notes = doc.getString("notes") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        version = remoteVersion,
                        updatedAt = remoteUpdatedAt,
                        lastModifiedBy = doc.getString("lastModifiedBy") ?: "",
                        lastModifiedDevice = doc.getString("lastModifiedDevice") ?: ""
                    )
                    db.supplierDao().insert(supplier)
                    applied++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying inbound supplier ${doc.id}", e)
            }
        }
        return applied
    }
}
