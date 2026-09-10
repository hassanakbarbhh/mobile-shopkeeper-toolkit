package com.shopkeeper.mobileshop.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessEnginesTest {

    @Test
    fun testNetProfitEngine() {
        val engine = NetProfitEngine()
        val profit = engine.calculateNetProfit(1000.0, 800.0, 50.0, 10.0)
        assertEquals(160.0, profit, 0.001)
    }

    @Test
    fun testImeiUniquenessGuard() {
        val guard = ImeiUniquenessGuard()
        assertTrue(guard.isImeiUnique("12345", listOf("54321", "99999")))
        assertFalse(guard.isImeiUnique("12345", listOf("12345", "99999")))
        assertTrue(guard.isImeiUnique("", listOf("12345"))) // Accessory
    }

    @Test
    fun testMarginGuard() {
        val guard = MarginGuard()
        assertTrue(guard.isMarginAcceptable(120.0, 100.0, 15.0)) // 20% margin
        assertFalse(guard.isMarginAcceptable(110.0, 100.0, 15.0)) // 10% margin
    }

    @Test
    fun testUdhaarAgingAnalyzer() {
        val analyzer = UdhaarAgingAnalyzer()
        val now = System.currentTimeMillis()
        val tenDaysAgo = now - (10L * 24 * 60 * 60 * 1000)
        val fortyDaysAgo = now - (40L * 24 * 60 * 60 * 1000)
        
        assertEquals("0-30 Days Overdue", analyzer.getAgingCategory(tenDaysAgo, now))
        assertEquals("31-60 Days Overdue", analyzer.getAgingCategory(fortyDaysAgo, now))
    }

    @Test
    fun testLowStockAlertEngine() {
        val engine = LowStockAlertEngine()
        assertTrue(engine.requiresReorder(2, 5))
        assertFalse(engine.requiresReorder(10, 5))
    }

    @Test
    fun testRepairStatusStateRouter() {
        val router = RepairStatusStateRouter()
        assertEquals("DIAGNOSING", router.getNextStatus("INTAKE"))
        assertEquals("REPAIRED", router.getNextStatus("WAITING_FOR_PARTS"))
    }

    @Test
    fun testSalesCommissionCalculator() {
        val calc = SalesCommissionCalculator()
        assertEquals(10.0, calc.calculateCommission(200.0, 5.0), 0.001)
        assertEquals(0.0, calc.calculateCommission(-50.0, 5.0), 0.001)
    }

    @Test
    fun testCustomerTrustScorer() {
        val scorer = CustomerTrustScorer()
        assertEquals(100, scorer.calculateScore(0, 0))
        assertEquals(89, scorer.calculateScore(2, 1)) // 100 - 15 + 4
        assertEquals(100, scorer.calculateScore(10, 0)) // bounded by 100
    }

    @Test
    fun testInventoryValuationEngine() {
        val engine = InventoryValuationEngine()
        val stock = listOf(Pair(100.0, 5), Pair(50.0, 10))
        assertEquals(1000.0, engine.calculateTotalValue(stock), 0.001)
    }

    @Test
    fun testDiscountApprovalGuard() {
        val guard = DiscountApprovalGuard()
        assertTrue(guard.requiresOwnerApproval(200.0, 1000.0, 15.0)) // 20% > 15%
        assertFalse(guard.requiresOwnerApproval(100.0, 1000.0, 15.0)) // 10% < 15%
    }

    @Test
    fun testCashClosingReconciler() {
        val reconciler = CashClosingReconciler()
        // Expected: 1000 + 500 - 100 = 1400. Actual = 1400. Variance = 0
        assertEquals(0.0, reconciler.reconcile(1000.0, 500.0, 100.0, 1400.0), 0.001)
        // Actual 1350, Variance = -50
        assertEquals(-50.0, reconciler.reconcile(1000.0, 500.0, 100.0, 1350.0), 0.001)
    }

    @Test
    fun testBulkImportSanitizer() {
        val sanitizer = BulkImportSanitizer()
        assertEquals("iPhone 15 Pro", sanitizer.sanitizeProductName("   iPhone    15 Pro  "))
        assertTrue(sanitizer.isValidPrice(10.0))
        assertFalse(sanitizer.isValidPrice(-5.0))
    }

    @Test
    fun testWarrantyExpirationTracker() {
        val tracker = WarrantyExpirationTracker()
        val now = System.currentTimeMillis()
        val twoMonthsAgo = now - (60L * 24 * 60 * 60 * 1000)
        
        assertTrue(tracker.isWarrantyValid(twoMonthsAgo, 3, now))
        assertFalse(tracker.isWarrantyValid(twoMonthsAgo, 1, now))
    }

    @Test
    fun testFastMovingItemDetector() {
        val detector = FastMovingItemDetector()
        assertTrue(detector.isFastMoving(50, 30))
        assertFalse(detector.isFastMoving(10, 30))
    }

    @Test
    fun testDuplicateCustomerMerger() {
        val merger = DuplicateCustomerMerger()
        // Our basic cleaner strips non-digits, so 923001234567 != 03001234567 directly unless we strip country codes too.
        // Let's just test the basic digit extraction
        assertTrue(merger.isDuplicate("0300 123 4567", "03001234567"))
        assertFalse(merger.isDuplicate("03001234567", "03011234567"))
    }
}
