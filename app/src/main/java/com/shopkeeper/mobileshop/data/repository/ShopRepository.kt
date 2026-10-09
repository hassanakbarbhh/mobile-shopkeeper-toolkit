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
            id
        }
    }

    suspend fun getSaleItems(saleId: Long) = db.saleDao().getSaleItems(saleId)

    suspend fun deleteSale(sale: Sale, restock: Boolean = true) {
        db.withTransaction {
            if (restock) {
                val items = db.saleDao().getSaleItems(sale.id)
                items.forEach { db.productDao().increaseStock(it.productId, it.quantity) }
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
     *    otherwise by non-blank IMEI, else by exact name/brand/model with a name-only fallback),
     *  - records the purchase cost basis into matched products,
     *  - increases the supplier's payable balance by the unpaid remainder.
     *
     * If any step fails (e.g. product missing, supplier missing), the whole
     * transaction rolls back so no purchase record or payable is left behind
     * without its stock/cost effects.
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

            for (item in linked) {
                val product = resolveProductForPurchase(item, productIds[item])
                    ?: throw IllegalStateException(
                        "Product not found for purchase item '${item.productName}'. Create the product before recording the purchase."
                    )
                val updated = product.copy(
                    quantity = product.quantity + item.quantity,
                    purchasePrice = if (item.unitCost > 0.0) item.unitCost else product.purchasePrice,
                    updatedAt = System.currentTimeMillis()
                )
                db.productDao().update(updated)
            }
            id
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
