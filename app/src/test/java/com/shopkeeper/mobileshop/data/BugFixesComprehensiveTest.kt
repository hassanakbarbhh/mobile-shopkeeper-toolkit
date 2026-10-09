package com.shopkeeper.mobileshop.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.utils.AppPreferences
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BugFixesComprehensiveTest {

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

    // BUG-001: Empty IMEI uniqueness fix
    @Test
    fun testMultipleProductsWithEmptyImeiCoexistWithoutDeletion() = runBlocking {
        val cable = Product(
            name = "USB-C Fast Charging Cable",
            brand = "Generic",
            model = "1m Braided",
            imei = "",
            category = ProductCategory.CABLE,
            purchasePrice = 150.0,
            sellingPrice = 350.0,
            quantity = 25
        )
        val case = Product(
            name = "Silicone Matte Phone Case",
            brand = "Generic",
            model = "Universal 6.7",
            imei = "",
            category = ProductCategory.CASE,
            purchasePrice = 200.0,
            sellingPrice = 500.0,
            quantity = 15
        )
        val glass = Product(
            name = "Tempered Glass Protector",
            brand = "Generic",
            model = "9D Curved",
            imei = "",
            category = ProductCategory.SCREEN_PROTECTOR,
            purchasePrice = 80.0,
            sellingPrice = 250.0,
            quantity = 50
        )

        val id1 = repository.insertProduct(cable)
        val id2 = repository.insertProduct(case)
        val id3 = repository.insertProduct(glass)

        val all = repository.getAllProductsList()
        assertEquals("All three empty-IMEI products must coexist", 3, all.size)
        assertTrue(all.any { it.name == "USB-C Fast Charging Cable" })
        assertTrue(all.any { it.name == "Silicone Matte Phone Case" })
        assertTrue(all.any { it.name == "Tempered Glass Protector" })
        assertNotEquals(id1, id2)
        assertNotEquals(id2, id3)
    }

    // BUG-002: Non-destructive data safety test
    @Test
    fun testLegitimateExpensesAndCustomersSurviveDatabaseCleanup() = runBlocking {
        // Insert legitimate recurring store expenses
        val rentExpense = Expense(
            title = "Shop Rent - October 2026",
            category = ExpenseCategory.RENT,
            amount = 45000.0,
            date = System.currentTimeMillis()
        )
        val electricExpense = Expense(
            title = "Electricity Bill October",
            category = ExpenseCategory.ELECTRICITY,
            amount = 12500.0,
            date = System.currentTimeMillis()
        )
        val internetExpense = Expense(
            title = "Broadband Internet September",
            category = ExpenseCategory.INTERNET,
            amount = 3500.0,
            date = System.currentTimeMillis()
        )

        db.expenseDao().insert(rentExpense)
        db.expenseDao().insert(electricExpense)
        db.expenseDao().insert(internetExpense)

        // Insert legitimate customers
        val c1 = Customer(name = "Amit Kumar", phone = "+923001112233")
        val c2 = Customer(name = "Pooja Sharma", phone = "+923004445566")
        db.customerDao().insert(c1)
        db.customerDao().insert(c2)

        // Trigger startup/initialization routine
        AppDatabase.ensureCleanDataAndDefaultStock(db)

        // Verify expenses are NOT deleted
        val rent = db.expenseDao().getByCloudId(rentExpense.cloudId)
        val electric = db.expenseDao().getByCloudId(electricExpense.cloudId)
        val internet = db.expenseDao().getByCloudId(internetExpense.cloudId)

        assertNotNull("Shop Rent must survive", rent)
        assertNotNull("Electricity Bill must survive", electric)
        assertNotNull("Broadband Internet must survive", internet)

        // Verify customers are NOT deleted
        val amit = db.customerDao().getByPhone("+923001112233")
        val pooja = db.customerDao().getByPhone("+923004445566")
        assertNotNull("Amit Kumar must survive", amit)
        assertNotNull("Pooja Sharma must survive", pooja)
    }

    // BUG-003: Sale transaction atomicity and stock deduction
    @Test
    fun testSaleTransactionDeductsStockAtomically() = runBlocking {
        val prod = Product(
            name = "OnePlus 12",
            brand = "OnePlus",
            model = "CPH2581",
            imei = "869402061234567",
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 180000.0,
            sellingPrice = 210000.0,
            quantity = 5
        )
        val prodId = repository.insertProduct(prod)

        val sale = Sale(
            customerName = "Ali Raza",
            totalAmount = 210000.0,
            finalAmount = 210000.0,
            paymentMethod = PaymentMethod.CASH
        )
        val item = SaleItem(
            saleId = 0,
            productId = prodId,
            productName = prod.name,
            quantity = 2,
            unitPrice = 210000.0,
            totalPrice = 420000.0
        )

        val saleId = repository.insertSale(sale, listOf(item))
        assertTrue(saleId > 0)

        val updatedProd = repository.getProduct(prodId)
        assertNotNull(updatedProd)
        assertEquals("Stock must be deducted from 5 to 3", 3, updatedProd!!.quantity)

        val saleItems = repository.getSaleItems(saleId)
        assertEquals(1, saleItems.size)
        assertEquals(saleId, saleItems.first().saleId)
    }

    // BUG-003: Sale transaction rolls back when stock is insufficient
    @Test
    fun testSaleTransactionRollsBackOnInsufficientStock() = runBlocking {
        val prod = Product(
            name = "Google Pixel 9 Pro",
            brand = "Google",
            model = "G1NZG",
            imei = "354921098765432",
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 220000.0,
            sellingPrice = 250000.0,
            quantity = 1
        )
        val prodId = repository.insertProduct(prod)

        val sale = Sale(
            customerName = "Bilal Khan",
            totalAmount = 500000.0,
            finalAmount = 500000.0,
            paymentMethod = PaymentMethod.CASH
        )
        val item = SaleItem(
            saleId = 0,
            productId = prodId,
            productName = prod.name,
            quantity = 3, // Requesting 3 when only 1 is available
            unitPrice = 250000.0,
            totalPrice = 750000.0
        )

        try {
            repository.insertSale(sale, listOf(item))
            fail("Must throw IllegalStateException on insufficient stock")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Insufficient stock") == true)
        }

        // Verify stock remains untouched
        val prodAfter = repository.getProduct(prodId)
        assertEquals("Stock must remain unchanged after rollback", 1, prodAfter!!.quantity)
    }

    // BUG-007: App Lock preference logic
    @Test
    fun testAppLockPreferencePersistence() {
        assertFalse(AppPreferences.isLockEnabled(context))
        AppPreferences.setLockEnabled(context, true)
        assertTrue(AppPreferences.isLockEnabled(context))
        AppPreferences.setLockEnabled(context, false)
        assertFalse(AppPreferences.isLockEnabled(context))
    }
}
