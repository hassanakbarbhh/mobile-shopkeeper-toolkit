package com.shopkeeper.mobileshop.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class P0DataIntegrityFixTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: ShopRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShopRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // --- P0-A: Purchase flow integrity ---

    @Test
    fun testPurchaseIncreasesStockAndRecordsCostBasis() = runBlocking {
        val supplier = Supplier(name = "ABC Mobile", phone = "03001234567")
        val supplierId = repository.insertSupplier(supplier)

        val product = Product(
            name = "Samsung Galaxy A54",
            brand = "Samsung",
            model = "A54",
            imei = "",
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 0.0,
            sellingPrice = 120000.0,
            quantity = 0
        )
        val productId = repository.insertProduct(product)

        val purchase = Purchase(
            supplierId = supplierId,
            supplierName = "ABC Mobile",
            totalCost = 100000.0,
            paidAmount = 100000.0
        )
        val item = PurchaseItem(
            purchaseId = 0,
            productName = "Samsung Galaxy A54",
            imei = "356789123456789",
            quantity = 2,
            unitCost = 50000.0
        )

        val purchaseId = repository.insertPurchase(purchase, listOf(item))

        assertTrue(purchaseId > 0)
        val updated = repository.getProduct(productId)!!
        assertEquals("Stock must increase by purchased quantity", 2, updated.quantity)
        assertEquals("Cost basis must be recorded from unit cost", 50000.0, updated.purchasePrice, 0.001)
    }

    @Test
    fun testPurchaseOnCreditIncreasesSupplierPayable() = runBlocking {
        val supplier = Supplier(name = "Wholesale Traders", phone = "03009876543")
        val supplierId = repository.insertSupplier(supplier)

        val product = Product(
            name = "USB-C Cable",
            brand = "Generic",
            model = "1m",
            category = ProductCategory.CABLE,
            purchasePrice = 100.0,
            sellingPrice = 300.0,
            quantity = 10
        )
        repository.insertProduct(product)

        val purchase = Purchase(
            supplierId = supplierId,
            supplierName = "Wholesale Traders",
            totalCost = 5000.0,
            paidAmount = 2000.0
        )
        val item = PurchaseItem(
            purchaseId = 0,
            productName = "USB-C Cable",
            quantity = 20,
            unitCost = 250.0
        )

        repository.insertPurchase(purchase, listOf(item))

        val updatedSupplier = repository.allSuppliers.let { flow ->
            kotlinx.coroutines.flow.first(repository.allSuppliers).first { it.id == supplierId }
        }
        assertEquals("Unpaid remainder must land in supplier payable", 3000.0, updatedSupplier.balance, 0.001)

        val updatedProduct = repository.getProduct(product.id)!!
        assertEquals("Stock must increase", 30, updatedProduct.quantity)
    }

    @Test
    fun testPurchaseRollsBackCompletelyWhenProductMissing() = runBlocking {
        val supplier = Supplier(name = "Reliable Traders", phone = "03001112233")
        val supplierId = repository.insertSupplier(supplier)

        val purchase = Purchase(
            supplierId = supplierId,
            supplierName = "Reliable Traders",
            totalCost = 8000.0,
            paidAmount = 0.0
        )
        val item = PurchaseItem(
            purchaseId = 0,
            productName = "Nonexistent Product X",
            quantity = 1,
            unitCost = 8000.0
        )

        try {
            repository.insertPurchase(purchase, listOf(item))
            fail("Must reject a purchase whose items cannot be matched to products")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Product not found") == true)
        }

        // Purchase must not exist
        val allPurchases = kotlinx.coroutines.flow.first(repository.allPurchases)
        assertTrue("Failed purchase must not persist", allPurchases.isEmpty())

        // Supplier payable must be untouched
        val supplierAfter = kotlinx.coroutines.flow.first(repository.allSuppliers).first { it.id == supplierId }
        assertEquals("Payable must not change on rollback", 0.0, supplierAfter.balance, 0.001)
    }

    @Test
    fun testPurchaseRollsBackWhenSupplierMissing() = runBlocking {
        val product = Product(
            name = "Tempered Glass",
            brand = "Generic",
            model = "9D",
            category = ProductCategory.SCREEN_PROTECTOR,
            purchasePrice = 80.0,
            sellingPrice = 250.0,
            quantity = 5
        )
        repository.insertProduct(product)

        val purchase = Purchase(
            supplierId = 9999L, // nonexistent supplier, credit purchase
            supplierName = "Ghost Supplier",
            totalCost = 400.0,
            paidAmount = 0.0
        )
        val item = PurchaseItem(
            purchaseId = 0,
            productName = "Tempered Glass",
            quantity = 1,
            unitCost = 400.0
        )

        try {
            repository.insertPurchase(purchase, listOf(item))
            fail("Must reject a credit purchase with a missing supplier")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Supplier not found") == true)
        }

        val allPurchases = kotlinx.coroutines.flow.first(repository.allPurchases)
        assertTrue("Failed purchase must not persist", allPurchases.isEmpty())
        val productAfter = repository.getProduct(product.id)!!
        assertEquals("Stock must be unchanged after rollback", 5, productAfter.quantity)
    }

    @Test
    fun testFullyPaidPurchaseDoesNotTouchSupplierBalance() = runBlocking {
        val supplier = Supplier(name = "Cash Supplier", phone = "03005556667")
        val supplierId = repository.insertSupplier(supplier)

        val product = Product(
            name = "Power Bank 20000mAh",
            brand = "Generic",
            model = "PB20",
            category = ProductCategory.POWER_BANK,
            purchasePrice = 3000.0,
            sellingPrice = 4500.0,
            quantity = 2
        )
        repository.insertProduct(product)

        val purchase = Purchase(
            supplierId = supplierId,
            supplierName = "Cash Supplier",
            totalCost = 6000.0,
            paidAmount = 6000.0
        )
        val item = PurchaseItem(
            purchaseId = 0,
            productName = "Power Bank 20000mAh",
            quantity = 2,
            unitCost = 3000.0
        )

        repository.insertPurchase(purchase, listOf(item))

        val supplierAfter = kotlinx.coroutines.flow.first(repository.allSuppliers).first { it.id == supplierId }
        assertEquals("Fully paid purchase must not create payable", 0.0, supplierAfter.balance, 0.001)
    }

    // --- P0-B: recordPayment atomicity + status transitions ---

    private suspend fun makeSaleWithDue(total: Double, final: Double): Pair<Long, Product> {
        val prod = Product(
            name = "Infinix Hot 40",
            brand = "Infinix",
            model = "X6825",
            imei = "351111111111111",
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 22000.0,
            sellingPrice = 30000.0,
            quantity = 10
        )
        val pid = repository.insertProduct(prod)
        val sale = Sale(
            customerName = "Walk-in Customer",
            totalAmount = total,
            finalAmount = final,
            paymentMethod = PaymentMethod.CASH
        )
        val item = SaleItem(
            saleId = 0,
            productId = pid,
            productName = prod.name,
            quantity = 1,
            unitPrice = final,
            totalPrice = final
        )
        val saleId = repository.insertSale(sale, listOf(item))
        return saleId to prod
    }

    @Test
    fun testPartialPaymentSetsPartialStatus() = runBlocking {
        val (saleId, _) = makeSaleWithDue(30000.0, 30000.0)

        repository.recordPayment(
            Payment(saleId = saleId, amount = 10000.0, paymentMethod = PaymentMethod.CASH, paymentType = PaymentType.RECEIVED)
        )

        val sale = repository.getPendingSales().firstOrNull { it.id == saleId }
        assertNotNull("Partially paid sale must remain visible in dues", sale)
        assertEquals(PaymentStatus.PARTIAL, sale!!.paymentStatus)
    }

    @Test
    fun testFullPaymentMarksSalePaid() = runBlocking {
        val (saleId, _) = makeSaleWithDue(30000.0, 30000.0)

        repository.recordPayment(
            Payment(saleId = saleId, amount = 10000.0, paymentMethod = PaymentMethod.CASH, paymentType = PaymentType.RECEIVED)
        )
        repository.recordPayment(
            Payment(saleId = saleId, amount = 20000.0, paymentMethod = PaymentMethod.CASH, paymentType = PaymentType.RECEIVED)
        )

        val sale = repository.getPendingSales().firstOrNull { it.id == saleId }
        assertNull("Fully paid sale must leave the dues list", sale)
    }

    @Test
    fun testPaymentWithoutSaleDoesNotCrash() = runBlocking {
        // Expense-type or customer payments not tied to a sale must be accepted
        repository.recordPayment(
            Payment(customerId = 1L, amount = 500.0, paymentMethod = PaymentMethod.CASH, paymentType = PaymentType.RECEIVED)
        )
    }

    // --- P0-C: no demo seed data ---

    @Test
    fun testFreshDatabaseIsNotPollutedWithDemoRecords() = runBlocking {
        // Seed check must not inject fake sellers, IMEIs, or invoices
        AppDatabase.ensureCleanDataAndDefaultStock(db)

        val sellers = kotlinx.coroutines.flow.first(repository.allSellers)
        assertTrue("No demo sellers may be seeded", sellers.none { it.name.contains("Hassan") || it.name.contains("Ali Khan") })

        val imeiAssets = db.imeiAssetDao().let { dao ->
            // ImeiAssetDao has no list-all; verify via a known fake IMEI absence
            dao.getAsset("356789123456789")
        }
        assertNull("Demo IMEI asset must not exist", imeiAssets)
    }
}
