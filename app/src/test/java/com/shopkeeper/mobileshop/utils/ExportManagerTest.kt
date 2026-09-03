package com.shopkeeper.mobileshop.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shopkeeper.mobileshop.data.db.entity.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileInputStream

@RunWith(RobolectricTestRunner::class)
class ExportManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun checkUtf8Bom(file: File) {
        assertTrue("File must exist", file.exists())
        assertTrue("File size must be at least 3 bytes for BOM", file.length() >= 3)
        FileInputStream(file).use { input ->
            val b1 = input.read()
            val b2 = input.read()
            val b3 = input.read()
            assertEquals("BOM byte 1 must be 0xEF", 0xEF, b1)
            assertEquals("BOM byte 2 must be 0xBB", 0xBB, b2)
            assertEquals("BOM byte 3 must be 0xBF", 0xBF, b3)
        }
    }

    @Test
    fun testExportProductsCsv() {
        val products = listOf(
            Product(
                id = 1,
                name = "Apple iPhone 15 Pro",
                brand = "Apple",
                model = "iPhone 15 Pro",
                category = ProductCategory.SMARTPHONE,
                purchasePrice = 0.0,
                sellingPrice = 0.0,
                quantity = 0,
                imei = "352012345678901"
            )
        )
        val file = ExportManager.exportProductsCsv(context, products)
        checkUtf8Bom(file)

        val content = file.readText()
        assertTrue("CSV should contain header", content.contains("ID,Product Name,Brand,Model,Category,Stock Quantity,Purchase Price,Selling Price"))
        assertTrue("CSV should contain product name", content.contains("Apple iPhone 15 Pro"))
        assertTrue("CSV should contain brand", content.contains("Apple"))
        assertTrue("CSV should contain IMEI", content.contains("352012345678901"))
    }

    @Test
    fun testExportCustomersCsv() {
        val customers = listOf(
            Customer(
                id = 1,
                name = "Ali Khan",
                phone = "03001234567",
                email = "ali@example.com",
                address = "Main Market Shop 5"
            )
        )
        val file = ExportManager.exportCustomersCsv(context, customers)
        checkUtf8Bom(file)

        val content = file.readText()
        assertTrue("CSV should contain Customer header", content.contains("Customer ID,Name,Phone Number"))
        assertTrue("CSV should contain customer name", content.contains("Ali Khan"))
        assertTrue("CSV should contain phone", content.contains("03001234567"))
    }

    @Test
    fun testExportSalesCsv() {
        val sales = listOf(
            Sale(
                id = 101,
                customerName = "Bilal Ahmed",
                totalAmount = 75000.0,
                discount = 500.0,
                finalAmount = 74500.0,
                paymentMethod = PaymentMethod.CASH,
                paymentStatus = PaymentStatus.PAID,
                sellerName = "Owner"
            )
        )
        val file = ExportManager.exportSalesCsv(context, sales)
        checkUtf8Bom(file)

        val content = file.readText()
        assertTrue("CSV should contain Sales header", content.contains("Invoice ID,Date,Customer Name,Seller/Staff"))
        assertTrue("CSV should contain Bilal Ahmed", content.contains("Bilal Ahmed"))
        assertTrue("CSV should contain amount", content.contains("74500.0"))
    }

    @Test
    fun testExportPurchasesCsv() {
        val purchases = listOf(
            Purchase(
                id = 1,
                supplierId = 1,
                supplierName = "Hafeez Center Wholesale",
                totalCost = 1400000.0,
                paidAmount = 1400000.0,
                paymentStatus = PaymentStatus.PAID,
                notes = "Wholesale bulk purchase"
            )
        )
        val file = ExportManager.exportPurchasesCsv(context, purchases)
        checkUtf8Bom(file)

        val content = file.readText()
        assertTrue("CSV should contain Purchase header", content.contains("Purchase ID,Date,Supplier Name,Total Cost,Paid Amount,Payment Status"))
        assertTrue("CSV should contain supplier name", content.contains("Hafeez Center Wholesale"))
        assertTrue("CSV should contain total cost", content.contains("1400000.0"))
    }

    @Test
    fun testExportRepairsCsv() {
        val repairs = listOf(
            Repair(
                id = 55,
                customerName = "Tariq Mahmood",
                customerPhone = "03219876543",
                deviceBrand = "Xiaomi",
                deviceModel = "Redmi Note 13",
                issueDescription = "Broken Screen Replacement",
                estimatedCost = 6500.0,
                actualCost = 6500.0,
                status = RepairStatus.COMPLETED
            )
        )
        val file = ExportManager.exportRepairsCsv(context, repairs)
        checkUtf8Bom(file)

        val content = file.readText()
        assertTrue("CSV should contain Repair header", content.contains("Ticket ID,Date Received,Customer Name,Phone Number,Device Brand,Device Model"))
        assertTrue("CSV should contain Tariq Mahmood", content.contains("Tariq Mahmood"))
        assertTrue("CSV should contain Xiaomi", content.contains("Xiaomi"))
        assertTrue("CSV should contain issue", content.contains("Broken Screen Replacement"))
    }
}
