package com.shopkeeper.mobileshop.data.repository

import androidx.room.withTransaction
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val db: AppDatabase) {

    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()
    val lowStockProducts: Flow<List<Product>> = db.productDao().getLowStockProducts()
    val totalInventoryValue: Flow<Double?> = db.productDao().getTotalInventoryValue()
    val totalProductCount: Flow<Int> = db.productDao().getTotalProductCount()
    fun searchProducts(q: String) = db.productDao().searchProducts("%$q%")
    suspend fun getProduct(id: Long) = db.productDao().getProductById(id)
    suspend fun getProductByImei(imei: String) = db.productDao().getByImei(imei)
    suspend fun getProductByBarcode(barcode: String) = db.productDao().getByBarcode(barcode) ?: db.productDao().getByImei(barcode)
    suspend fun getAllProductsList(): List<Product> = db.productDao().getAllProductsList()

    suspend fun insertProduct(p: Product): Long {
        if (p.imei.isNotBlank()) {
            val existing = db.productDao().getByImei(p.imei)
            if (existing != null && existing.id != p.id && !existing.isDeleted) {
                val updated = existing.copy(
                    name = p.name,
                    brand = p.brand,
                    model = p.model,
                    purchasePrice = p.purchasePrice,
                    sellingPrice = p.sellingPrice,
                    quantity = existing.quantity + p.quantity,
                    updatedAt = System.currentTimeMillis()
                )
                db.productDao().update(updated)
                return existing.id
            }
        }
        return db.productDao().insert(p)
    }

    suspend fun updateProduct(p: Product) = db.productDao().update(p)
    suspend fun deleteProduct(p: Product) = db.productDao().delete(p)

    val allCustomers: Flow<List<Customer>> = db.customerDao().getAllCustomers()
    fun searchCustomers(q: String) = db.customerDao().searchCustomers("%$q%")
    suspend fun getCustomer(id: Long) = db.customerDao().getCustomerById(id)
    suspend fun insertCustomer(c: Customer) = db.customerDao().insert(c)
    suspend fun updateCustomer(c: Customer) = db.customerDao().update(c)
    suspend fun deleteCustomer(c: Customer) = db.customerDao().delete(c)

    val allSales: Flow<List<Sale>> = db.saleDao().getAllSales()
    val dueSales: Flow<List<Sale>> = db.saleDao().getDueSales()
    val totalPendingAmount: Flow<Double?> = db.saleDao().getTotalPendingAmount()
    suspend fun getPendingSales() = db.saleDao().getPendingSales()

    suspend fun insertSale(sale: Sale, items: List<SaleItem>): Long {
        return db.withTransaction {
            // 1. Verify stock availability for all items before applying any database changes
            for (item in items) {
                val prod = db.productDao().getProductById(item.productId)
                if (prod != null && prod.quantity < item.quantity) {
                    throw IllegalStateException("Insufficient stock for '${prod.name}'. In stock: ${prod.quantity}, requested: ${item.quantity}")
                }
            }

            // 2. Insert parent sale
            val id = db.saleDao().insertSale(sale)
            val linked = items.map { it.copy(saleId = id) }
            db.saleDao().insertSaleItems(linked)

            // 3. Atomically deduct stock and verify rows updated
            for (item in linked) {
                val updatedRows = db.productDao().reduceStock(item.productId, item.quantity)
                if (updatedRows == 0) {
                    val prod = db.productDao().getProductById(item.productId)
                    throw IllegalStateException("Failed to deduct stock for '${prod?.name ?: item.productName}'. Insufficient quantity.")
                }
            }

            // 4. IMEI lifecycle: sold serial devices become SOLD assets with audit events
            recordSoldImeiAssets(sale.copy(id = id), linked)

            id
        }
    }

    /**
     * For every sale line carrying a non-blank IMEI, marks the tracked asset
     * as SOLD and appends a lifecycle event. Runs inside the caller's
     * transaction; never creates assets for blank/placeholder IMEIs.
     */
    private suspend fun recordSoldImeiAssets(sale: Sale, items: List<SaleItem>) {
        val now = System.currentTimeMillis()
        for (item in items) {
            val imei = item.imei.trim()
            if (imei.isEmpty()) continue
            val existing = db.imeiAssetDao().getAssetSync(imei)
            if (existing != null) {
                db.imeiAssetDao().updateAsset(
                    existing.copy(
                        currentStatus = ImeiAsset.STATUS_SOLD,
                        customerId = sale.customerId ?: existing.customerId,
                        customerName = sale.customerName.ifBlank { existing.customerName },
                        saleInvoiceId = sale.id,
                        sellingPrice = if (item.totalPrice > 0.0) item.totalPrice else existing.sellingPrice,
                        updatedAt = now
                    )
                )
            } else {
                // Asset was never registered by a purchase; register it now so
                // the IMEI remains traceable through sale history.
                val product = db.productDao().getProductById(item.productId)
                db.imeiAssetDao().insertAsset(
                    ImeiAsset(
                        imei = imei,
                        productId = item.productId,
                        productName = item.productName,
                        brand = product?.brand ?: "",
                        model = product?.model ?: "",
                        purchasePrice = product?.purchasePrice ?: 0.0,
                        sellingPrice = item.unitPrice,
                        currentStatus = ImeiAsset.STATUS_SOLD,
                        customerId = sale.customerId ?: 0L,
                        customerName = sale.customerName,
                        saleInvoiceId = sale.id,
                        updatedAt = now
                    )
                )
            }
            db.imeiAssetDao().insertLifecycleEvent(
                ImeiLifecycleEvent(
                    imei = imei,
                    eventType = ImeiLifecycleEvent.EVENT_SOLD,
                    timestamp = now,
                    title = "Sold to Customer",
                    details = "Sold for Rs ${item.totalPrice}. Invoice ${sale.invoiceNumber}",
                    referenceId = sale.invoiceNumber
                )
            )
        }
    }

    suspend fun getSaleItems(saleId: Long) = db.saleDao().getSaleItems(saleId)

    /**
     * Deletes a sale with full reversal inside one transaction: restocks,
     * restores affected IMEI assets to RETURNED status with a lifecycle event,
     * and removes sale items. Payments tied to the sale remain recorded so
     * financial history stays auditable.
     */
    suspend fun deleteSale(sale: Sale, restock: Boolean = true) {
        db.withTransaction {
            val items = db.saleDao().getSaleItems(sale.id)
            if (restock) {
                items.forEach { db.productDao().increaseStock(it.productId, it.quantity) }
            }
            val now = System.currentTimeMillis()
            for (item in items) {
                val imei = item.imei.trim()
                if (imei.isEmpty()) continue
                val asset = db.imeiAssetDao().getAssetSync(imei)
                if (asset != null && asset.saleInvoiceId == sale.id && asset.currentStatus == ImeiAsset.STATUS_SOLD) {
                    db.imeiAssetDao().updateAsset(
                        asset.copy(
                            currentStatus = ImeiAsset.STATUS_RETURNED,
                            customerId = 0L,
                            customerName = "",
                            saleInvoiceId = 0L,
                            updatedAt = now
                        )
                    )
                    db.imeiAssetDao().insertLifecycleEvent(
                        ImeiLifecycleEvent(
                            imei = imei,
                            eventType = ImeiLifecycleEvent.EVENT_RETURNED,
                            timestamp = now,
                            title = "Sale Deleted - Device Returned",
                            details = "Sale ${sale.invoiceNumber} was deleted; device status set to RETURNED.",
                            referenceId = sale.invoiceNumber
                        )
                    )
                }
            }
            db.saleDao().deleteSaleItems(sale.id)
            db.saleDao().delete(sale)
        }
    }

    val allRepairs: Flow<List<Repair>> = db.repairDao().getAllRepairs()
    val activeRepairs: Flow<List<Repair>> = db.repairDao().getActiveRepairs()
    val activeRepairCount: Flow<Int> = db.repairDao().getActiveRepairCount()
    fun getRepairsByStatus(status: RepairStatus) = db.repairDao().getRepairsByStatus(status.name)
    suspend fun insertRepair(r: Repair) = db.repairDao().insert(r)
    suspend fun updateRepair(r: Repair) = db.repairDao().update(r)
    suspend fun deleteRepair(r: Repair) = db.repairDao().delete(r)

    val allSuppliers: Flow<List<Supplier>> = db.supplierDao().getAll()
    suspend fun insertSupplier(s: Supplier) = db.supplierDao().insert(s)
    suspend fun updateSupplier(s: Supplier) = db.supplierDao().update(s)
    suspend fun deleteSupplier(s: Supplier) = db.supplierDao().delete(s)

    val allPurchases: Flow<List<Purchase>> = db.purchaseDao().getAll()
    suspend fun getPurchaseItems(purchaseId: Long) = db.purchaseDao().getItemsForPurchase(purchaseId)
    suspend fun updatePurchase(p: Purchase) = db.purchaseDao().update(p)

    /**
     * Records a purchase atomically:
     *  - inserts the purchase and its items,
     *  - increases stock for every matched product (matched by productId when provided,
     *    otherwise by non-blank IMEI, else by name match),
     *  - records the purchase cost basis into matched products,
     *  - increases the supplier's payable balance by the unpaid remainder,
     *  - registers/refreshes ImeiAsset records for serial lines with lifecycle events.
     *
     * If any step fails, the whole transaction rolls back so no purchase record,
     * payable, stock change, or IMEI asset is left half-applied.
     */
    suspend fun insertPurchase(p: Purchase, items: List<PurchaseItem>, productIds: Map<PurchaseItem, Long> = emptyMap()): Long {
        return db.withTransaction {
            val id = db.purchaseDao().insert(p)
            val linked = items.map { it.copy(purchaseId = id) }
            db.purchaseDao().insertItems(linked)

            val outstanding = p.totalCost - p.paidAmount
            if (outstanding > 0.0) {
                val supplier = db.supplierDao().getById(p.supplierId)
                    ?: throw IllegalStateException("Supplier not found for purchase: id=${p.supplierId}")
                db.supplierDao().adjustBalance(p.supplierId, outstanding)
            }

            val now = System.currentTimeMillis()
            for (item in linked) {
                val product = resolveProductForPurchase(item, productIds[item])
                    ?: throw IllegalStateException(
                        "Product not found for purchase item '${item.productName}'. Create the product before recording the purchase."
                    )
                val updated = product.copy(
                    quantity = product.quantity + item.quantity,
                    purchasePrice = if (item.unitCost > 0.0) item.unitCost else product.purchasePrice,
                    updatedAt = now
                )
                db.productDao().update(updated)

                // IMEI lifecycle: serial lines create/refresh tracked assets
                val imei = item.imei.trim()
                if (imei.isNotEmpty()) {
                    val existing = db.imeiAssetDao().getAssetSync(imei)
                    if (existing == null) {
                        db.imeiAssetDao().insertAsset(
                            ImeiAsset(
                                imei = imei,
                                productId = product.id,
                                productName = product.name,
                                brand = product.brand,
                                model = product.model,
                                purchaseId = id,
                                supplierName = p.supplierName,
                                purchasePrice = item.unitCost,
                                sellingPrice = product.sellingPrice,
                                currentStatus = ImeiAsset.STATUS_IN_STOCK,
                                updatedAt = now
                            )
                        )
                    } else if (existing.currentStatus == ImeiAsset.STATUS_SOLD || existing.isDeleted) {
                        // Re-purchase of a previously sold/removed device re-enters stock
                        db.imeiAssetDao().updateAsset(
                            existing.copy(
                                currentStatus = ImeiAsset.STATUS_IN_STOCK,
                                productId = product.id,
                                purchaseId = id,
                                supplierName = p.supplierName,
                                purchasePrice = item.unitCost,
                                isDeleted = false,
                                deletedAt = null,
                                customerId = 0L,
                                customerName = "",
                                saleInvoiceId = 0L,
                                updatedAt = now
                            )
                        )
                    }
                    db.imeiAssetDao().insertLifecycleEvent(
                        ImeiLifecycleEvent(
                            imei = imei,
                            eventType = ImeiLifecycleEvent.EVENT_IN_STOCK,
                            timestamp = now,
                            title = "Purchased from Supplier",
                            details = "Purchased from ${p.supplierName} at Rs ${item.unitCost}. Order ${p.orderNumber}",
                            referenceId = p.orderNumber
                        )
                    )
                }
            }
            id
        }
    }

    /**
     * Deletes a purchase with full reversal inside one transaction: reduces
     * stock (never below zero), tombstones IMEI assets registered by this
     * purchase (only while still IN_STOCK — sold devices are never touched),
     * and decreases the supplier payable by this purchase's outstanding amount
     * without letting the payable go negative.
     */
    suspend fun deletePurchaseWithReversal(p: Purchase) {
        db.withTransaction {
            val items = db.purchaseDao().getItemsForPurchase(p.id)
            val now = System.currentTimeMillis()
            for (item in items) {
                val imei = item.imei.trim()
                if (imei.isNotEmpty()) {
                    val asset = db.imeiAssetDao().getAssetSync(imei)
                    if (asset != null && asset.purchaseId == p.id && asset.currentStatus == ImeiAsset.STATUS_IN_STOCK) {
                        db.imeiAssetDao().insertLifecycleEvent(
                            ImeiLifecycleEvent(
                                imei = imei,
                                eventType = ImeiLifecycleEvent.EVENT_RETURNED,
                                timestamp = now,
                                title = "Purchase Deleted - Asset Unlinked",
                                details = "Purchase ${p.orderNumber} was deleted; asset removed from tracked stock.",
                                referenceId = p.orderNumber
                            )
                        )
                        db.imeiAssetDao().updateAsset(asset.copy(isDeleted = true, deletedAt = now, updatedAt = now))
                    }
                }
            }
            for (item in items) {
                val product = resolveProductForPurchase(item, null)
                if (product != null && product.quantity >= item.quantity) {
                    db.productDao().reduceStock(product.id, item.quantity)
                }
                // If quantity already partly sold, stock is deliberately not forced negative
            }
            val outstanding = p.totalCost - p.paidAmount
            if (outstanding > 0.0) {
                val supplier = db.supplierDao().getById(p.supplierId)
                if (supplier != null) {
                    val reduction = minOf(outstanding, supplier.balance.coerceAtLeast(0.0))
                    if (reduction > 0.0) {
                        db.supplierDao().adjustBalance(p.supplierId, -reduction)
                    }
                }
            }
            db.purchaseDao().deleteItemsForPurchase(p.id)
            db.purchaseDao().delete(p)
        }
    }

    private suspend fun resolveProductForPurchase(item: PurchaseItem, explicitId: Long?): Product? {
        if (explicitId != null && explicitId > 0L) {
            return db.productDao().getProductById(explicitId)
        }
        if (item.imei.isNotBlank()) {
            val byImei = db.productDao().getByImei(item.imei)
            if (byImei != null) return byImei
        }
        return db.productDao().getByName(item.productName)
    }

    suspend fun deletePurchase(p: Purchase) = db.purchaseDao().delete(p)

    val allExpenses: Flow<List<Expense>> = db.expenseDao().getAll()
    suspend fun insertExpense(e: Expense) = db.expenseDao().insert(e)
    suspend fun deleteExpense(e: Expense) = db.expenseDao().delete(e)
    fun sumExpenses(start: Long, end: Long) = db.expenseDao().sumInRange(start, end)

    // Seller & Staff Management for Owner
    val allSellers: Flow<List<Seller>> = db.sellerDao().getAllSellers()
    val activeSellers: Flow<List<Seller>> = db.sellerDao().getActiveSellers()
    val totalSellerCount: Flow<Int> = db.sellerDao().getSellerCount()
    suspend fun insertSeller(s: Seller) = db.sellerDao().insert(s)
    suspend fun updateSeller(s: Seller) = db.sellerDao().update(s)
    suspend fun deleteSeller(s: Seller) = db.sellerDao().delete(s)
    fun getSalesBySeller(sellerId: Long) = db.saleDao().getSalesBySeller(sellerId)
    fun getTotalSalesBySeller(sellerId: Long) = db.saleDao().getTotalSalesBySeller(sellerId)
    fun getSalesCountBySeller(sellerId: Long) = db.saleDao().getSalesCountBySeller(sellerId)

    /**
     * Records a payment atomically. The payment row insert and the dependent
     * sale payment-status recompute happen inside one Room transaction so a
     * failure cannot leave a paid sale flagged pending (or vice versa), and a
     * crash cannot record a payment whose status update was lost.
     */
    suspend fun recordPayment(payment: Payment) {
        db.withTransaction {
            db.paymentDao().insert(payment)
            payment.saleId?.let { saleId ->
                val sale = db.saleDao().getSaleById(saleId) ?: return@withTransaction
                val totalReceived = db.paymentDao().receivedForSale(saleId) ?: 0.0
                val newStatus = when {
                    totalReceived >= sale.finalAmount -> PaymentStatus.PAID
                    totalReceived > 0.0 -> PaymentStatus.PARTIAL
                    else -> PaymentStatus.PENDING
                }
                db.saleDao().update(sale.copy(paymentStatus = newStatus))
            }
        }
    }

    suspend fun getSalePayments(saleId: Long) = db.paymentDao().getPaymentsForSale(saleId)
}
