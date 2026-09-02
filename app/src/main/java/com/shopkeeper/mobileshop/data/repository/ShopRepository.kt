package com.shopkeeper.mobileshop.data.repository

import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val db: AppDatabase) {

    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()
    val lowStockProducts: Flow<List<Product>> = db.productDao().getLowStockProducts()
    val totalInventoryValue: Flow<Double?> = db.productDao().getTotalInventoryValue()
    val totalProductCount: Flow<Int> = db.productDao().getTotalProductCount()
    fun searchProducts(q: String) = db.productDao().searchProducts("%$q%")
    suspend fun insertProduct(p: Product) = db.productDao().insert(p)
    suspend fun updateProduct(p: Product) = db.productDao().update(p)
    suspend fun deleteProduct(p: Product) = db.productDao().delete(p)

    val allCustomers: Flow<List<Customer>> = db.customerDao().getAllCustomers()
    fun searchCustomers(q: String) = db.customerDao().searchCustomers("%$q%")
    suspend fun insertCustomer(c: Customer) = db.customerDao().insert(c)

    val allSales: Flow<List<Sale>> = db.saleDao().getAllSales()
    val dueSales: Flow<List<Sale>> = db.saleDao().getDueSales()
    val totalPendingAmount: Flow<Double?> = db.saleDao().getTotalPendingAmount()
    suspend fun insertSale(sale: Sale, items: List<SaleItem>): Long {
        val id = db.saleDao().insertSale(sale)
        val linked = items.map { it.copy(saleId = id) }
        db.saleDao().insertSaleItems(linked)
        linked.forEach { db.productDao().reduceStock(it.productId, it.quantity) }
        return id
    }
    suspend fun getSaleItems(saleId: Long) = db.saleDao().getSaleItems(saleId)

    val allRepairs: Flow<List<Repair>> = db.repairDao().getAllRepairs()
    val activeRepairs: Flow<List<Repair>> = db.repairDao().getActiveRepairs()
    val activeRepairCount: Flow<Int> = db.repairDao().getActiveRepairCount()
    fun getRepairsByStatus(status: RepairStatus) = db.repairDao().getRepairsByStatus(status.name)
    suspend fun insertRepair(r: Repair) = db.repairDao().insert(r)
    suspend fun updateRepair(r: Repair) = db.repairDao().update(r)

    val allSuppliers: Flow<List<Supplier>> = db.supplierDao().getAll()
    suspend fun insertSupplier(s: Supplier) = db.supplierDao().insert(s)

    val allPurchases: Flow<List<Purchase>> = db.purchaseDao().getAll()
    suspend fun insertPurchase(p: Purchase, items: List<PurchaseItem>): Long {
        val id = db.purchaseDao().insert(p)
        db.purchaseDao().insertItems(items.map { it.copy(purchaseId = id) })
        return id
    }

    val allExpenses: Flow<List<Expense>> = db.expenseDao().getAll()
    suspend fun insertExpense(e: Expense) = db.expenseDao().insert(e)
    fun sumExpenses(start: Long, end: Long) = db.expenseDao().sumInRange(start, end)

    suspend fun recordPayment(payment: Payment) {
        db.paymentDao().insert(payment)
        payment.saleId?.let { saleId ->
            val sale = db.saleDao().getSaleById(saleId) ?: return@let
            val totalReceived = db.paymentDao().receivedForSale(saleId) ?: 0.0
            val newStatus = when {
                totalReceived >= sale.finalAmount -> PaymentStatus.PAID
                totalReceived > 0.0 -> PaymentStatus.PARTIAL
                else -> PaymentStatus.PENDING
            }
            db.saleDao().update(sale.copy(paymentStatus = newStatus))
        }
    }

    suspend fun getSalePayments(saleId: Long) = db.paymentDao().getPaymentsForSale(saleId)
}
