package com.shopkeeper.mobileshop.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import kotlinx.coroutines.flow.first
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
        val productId = repository.insertProduct(product)

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

        val updatedSupplier = repository.allSuppliers.first().first { it.id == supplierId }
        assertEquals("Unpaid remainder must land in supplier payable", 3000.0, updatedSupplier.balance, 0.001)

        val updatedProduct = repository.getProduct(productId)!!
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

        val allPurchases = repository.allPurchases.first()
        assertTrue("Failed purchase must not persist", allPurchases.isEmpty())

        val supplierAfter = repository.allSuppliers.first().first { it.id == supplierId }
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
            supplierId = 9999L,
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

        val allPurchases = repository.allPurchases.first()
        assertTrue("Failed purchase must not persist", allPurchases.isEmpty())
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

        val supplierAfter = repository.allSuppliers.first().first { it.id == supplierId }
        assertEquals("Fully paid purchase must not create payable", 0.0, supplierAfter.balance, 0.001)
    }

    // --- P0-B: recordPayment atomicity + status transitions ---

    private suspend fun makeSaleWithDue(total: Double, final: Double): Long {
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
        return repository.insertSale(sale, listOf(item))
    }

    @Test
    fun testPartialPaymentSetsPartialStatus() = runBlocking {
        val saleId = makeSaleWithDue(30000.0, 30000.0)

        repository.recordPayment(
            Payment(saleId = saleId, amount = 10000.0, paymentMethod = PaymentMethod.CASH, paymentType = PaymentType.RECEIVED)
        )

        val sale = repository.getPendingSales().firstOrNull { it.id == saleId }
        assertNotNull("Partially paid sale must remain visible in dues", sale)
        assertEquals(PaymentStatus.PARTIAL, sale!!.paymentStatus)
    }

    @Test
    fun testFullPaymentMarksSalePaid() = runBlocking {
        val saleId = makeSaleWithDue(30000.0, 30000.0)

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
        repository.recordPayment(
            Payment(customerId = 1L, amount = 500.0, paymentMethod = PaymentMethod.CASH, paymentType = PaymentType.RECEIVED)
        )
    }

    // --- P0-C: no demo seed data ---

    @Test
    fun testFreshDatabaseIsNotPollutedWithDemoRecords() = runBlocking {
        AppDatabase.ensureCleanDataAndDefaultStock(db)

        val sellers = repository.allSellers.first()
        assertTrue("No demo sellers may be seeded", sellers.none { it.name.contains("Hassan") || it.name.contains("Ali Khan") })

        val demoAsset = db.imeiAssetDao().getAssetSync("356789123456789")
        assertNull("Demo IMEI asset must not exist", demoAsset)
    }

    // --- P1: IMEI lifecycle integration ---

    @Test
    fun testPurchaseRegistersImeiAssetInStockWithLifecycleEvent() = runBlocking {
        val supplierId = repository.insertSupplier(Supplier(name = "IMEI Source", phone = "03001110000"))
        val product = Product(
            name = "Samsung Galaxy A14",
            brand = "Samsung",
            model = "A14",
            imei = "",
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 0.0,
            sellingPrice = 60000.0,
            quantity = 0
        )
        repository.insertProduct(product)

        val purchase = Purchase(supplierId = supplierId, supplierName = "IMEI Source", totalCost = 45000.0, paidAmount = 45000.0)
        val item = PurchaseItem(purchaseId = 0, productName = "Samsung Galaxy A14", imei = "351234567890123", quantity = 1, unitCost = 45000.0)
        val purchaseId = repository.insertPurchase(purchase, listOf(item))

        val asset = db.imeiAssetDao().getAssetSync("351234567890123")
        assertNotNull("Purchase with IMEI must register a tracked asset", asset)
        assertEquals(ImeiAsset.STATUS_IN_STOCK, asset!!.currentStatus)
        assertEquals(purchaseId, asset.purchaseId)
        assertEquals(45000.0, asset.purchasePrice, 0.001)

        val events = db.imeiAssetDao().getEventsForImeiSync("351234567890123")
        assertTrue("Purchase must leave a lifecycle event", events.isNotEmpty())
        assertEquals(ImeiLifecycleEvent.EVENT_IN_STOCK, events.first().eventType)
    }

    @Test
    fun testBlankImeiPurchaseNeverCreatesAsset() = runBlocking {
        val supplierId = repository.insertSupplier(Supplier(name = "Accessory Source", phone = "03002220000"))
        val product = Product(
            name = "Charger 33W",
            brand = "Generic",
            model = "C33",
            imei = "",
            category = ProductCategory.CHARGER,
            purchasePrice = 0.0,
            sellingPrice = 2500.0,
            quantity = 0
        )
        repository.insertProduct(product)

        val purchase = Purchase(supplierId = supplierId, supplierName = "Accessory Source", totalCost = 10000.0, paidAmount = 10000.0)
        val item = PurchaseItem(purchaseId = 0, productName = "Charger 33W", imei = "", quantity = 5, unitCost = 2000.0)
        repository.insertPurchase(purchase, listOf(item))

        val assets = db.imeiAssetDao().getAllAssets().first()
        assertTrue("Blank IMEI must never create a tracked asset", assets.isEmpty())
    }

    @Test
    fun testSaleWithImeiMarksAssetSoldAndRecordsEvent() = runBlocking {
        val supplierId = repository.insertSupplier(Supplier(name = "Flow Supplier", phone = "03003330000"))
        val product = Product(
            name = "Xiaomi Redmi 13C",
            brand = "Xiaomi",
            model = "23124RN87I",
            imei = "",
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 0.0,
            sellingPrice = 48000.0,
            quantity = 0
        )
        val pid = repository.insertProduct(product)

        val purchase = Purchase(supplierId = supplierId, supplierName = "Flow Supplier", totalCost = 38000.0, paidAmount = 38000.0)
        val pItem = PurchaseItem(purchaseId = 0, productName = "Xiaomi Redmi 13C", imei = "861234567890124", quantity = 1, unitCost = 38000.0)
        repository.insertPurchase(purchase, listOf(pItem))

        val sale = Sale(customerName = "Kamran Ali", totalAmount = 48000.0, finalAmount = 48000.0, paymentMethod = PaymentMethod.CASH)
        val sItem = SaleItem(
            saleId = 0,
            productId = pid,
            productName = "Xiaomi Redmi 13C",
            quantity = 1,
            unitPrice = 48000.0,
            totalPrice = 48000.0,
            imei = "861234567890124"
        )
        val saleId = repository.insertSale(sale, listOf(sItem))

        val asset = db.imeiAssetDao().getAssetSync("861234567890124")!!
        assertEquals("Sold device must be marked SOLD", ImeiAsset.STATUS_SOLD, asset.currentStatus)
        assertEquals(saleId, asset.saleInvoiceId)
        assertEquals("Kamran Ali", asset.customerName)

        val events = db.imeiAssetDao().getEventsForImeiSync("861234567890124")
        assertEquals(ImeiLifecycleEvent.EVENT_SOLD, events.last().eventType)
    }

    @Test
    fun testSaleRollbackLeavesNoSoldAsset() = runBlocking {
        val product = Product(
            name = "Oppo A18",
            brand = "Oppo",
            model = "CPH2591",
            imei = "",
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 24000.0,
            sellingPrice = 29000.0,
            quantity = 1
        )
        val pid = repository.insertProduct(product)

        val sale = Sale(customerName = "Test", totalAmount = 999999.0, finalAmount = 999999.0, paymentMethod = PaymentMethod.CASH)
        val sItem = SaleItem(
            saleId = 0, productId = pid, productName = "Oppo A18", quantity = 5,
            unitPrice = 29000.0, totalPrice = 145000.0, imei = "869999999999991"
        )
        try {
            repository.insertSale(sale, listOf(sItem))
            fail("Sale must fail on insufficient stock")
        } catch (e: IllegalStateException) {
            // expected
        }
        val asset = db.imeiAssetDao().getAssetSync("869999999999991")
        assertNull("Rolled-back sale must not create an IMEI asset", asset)
    }

    // --- P2: delete reversal ---

    @Test
    fun testDeletePurchaseWithReversalReducesStockAndPayable() = runBlocking {
        val supplierId = repository.insertSupplier(Supplier(name = "Reversal Supplier", phone = "03004440000"))
        val product = Product(
            name = "Reversal Test Phone",
            brand = "Test",
            model = "RT1",
            imei = "",
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 10000.0,
            sellingPrice = 15000.0,
            quantity = 0
        )
        val productId = repository.insertProduct(product)

        val purchase = Purchase(supplierId = supplierId, supplierName = "Reversal Supplier", totalCost = 10000.0, paidAmount = 4000.0)
        val item = PurchaseItem(purchaseId = 0, productName = "Reversal Test Phone", quantity = 3, unitCost = 3333.333333)
        val purchaseId = repository.insertPurchase(purchase, listOf(item))

        val supplierMid = repository.allSuppliers.first().first { it.id == supplierId }
        assertEquals(6000.0, supplierMid.balance, 0.01)

        val purchaseRecord = repository.allPurchases.first().first { p -> p.id == purchaseId }
        repository.deletePurchaseWithReversal(purchaseRecord)

        val productAfter = repository.getProduct(productId)!!
        assertEquals("Stock must be reduced back", 0, productAfter.quantity)
        val supplierAfter = repository.allSuppliers.first().first { it.id == supplierId }
        assertEquals("Payable must be reduced on purchase deletion", 0.0, supplierAfter.balance, 0.01)
    }
}
