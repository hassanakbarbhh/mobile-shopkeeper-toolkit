package com.shopkeeper.mobileshop.domain

import com.shopkeeper.mobileshop.data.db.entity.RepairStatus
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
        assertTrue(guard.isImeiUnique("", listOf("12345")))
    }

    @Test
    fun testMarginGuard() {
        val guard = MarginGuard()
        assertTrue(guard.isMarginAcceptable(120.0, 100.0, 15.0))
        assertFalse(guard.isMarginAcceptable(110.0, 100.0, 15.0))
    }

    @Test
    fun testUdhaarAgingAnalyzer() {
        val analyzer = UdhaarAgingAnalyzer()
        val now = System.currentTimeMillis()
        assertEquals("0-30 Days Overdue", analyzer.getAgingCategory(now - (10L * 24 * 60 * 60 * 1000), now))
        assertEquals("31-60 Days Overdue", analyzer.getAgingCategory(now - (40L * 24 * 60 * 60 * 1000), now))
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
        assertEquals(RepairStatus.DIAGNOSING, router.getNextStatus(RepairStatus.RECEIVED))
        assertEquals(RepairStatus.IN_REPAIR, router.getNextStatus(RepairStatus.WAITING_PARTS))
        assertEquals(RepairStatus.DELIVERED, router.getNextStatus(RepairStatus.DELIVERED))
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
        assertEquals(89, scorer.calculateScore(2, 1))
        assertEquals(100, scorer.calculateScore(10, 0))
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
        assertTrue(guard.requiresOwnerApproval(200.0, 1000.0, 15.0))
        assertFalse(guard.requiresOwnerApproval(100.0, 1000.0, 15.0))
    }

    @Test
    fun testCashClosingReconciler() {
        val reconciler = CashClosingReconciler()
        assertEquals(0.0, reconciler.reconcile(1000.0, 500.0, 100.0, 1400.0), 0.001)
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
        assertTrue(tracker.isWarrantyValid(now - (60L * 24 * 60 * 60 * 1000), 3, now))
        assertFalse(tracker.isWarrantyValid(now - (60L * 24 * 60 * 60 * 1000), 1, now))
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
        assertTrue(merger.isDuplicate("0300 123 4567", "03001234567"))
        assertFalse(merger.isDuplicate("03001234567", "03011234567"))
    }

    @Test
    fun testStockoutForecaster() {
        val forecaster = StockoutForecaster()
        assertEquals(5, forecaster.forecastDaysLeft(10, 2.0))
        assertEquals(Int.MAX_VALUE, forecaster.forecastDaysLeft(10, 0.0))
    }

    @Test
    fun testSupplierScoreEngine() {
        val engine = SupplierScoreEngine()
        assertEquals(100.0, engine.calculateScore(0.0, 1.0), 0.001)
        assertEquals(90.0, engine.calculateScore(0.1, 1.0), 0.001)
    }

    @Test
    fun testOfflineConflictResolver() {
        val resolver = OfflineConflictResolver()
        val conflicts = resolver.detectConflicts(1000L, 2000L, "IMEI1", "IMEI1")
        assertEquals(1, conflicts.size)
    }

    @Test
    fun testDeadStockDetector() {
        val detector = DeadStockDetector()
        val now = System.currentTimeMillis()
        val deadInfo = detector.analyzeStock(now - (70L * 24 * 60 * 60 * 1000), 5, 100.0, now)
        assertTrue(deadInfo.isDead)
        assertEquals(500.0, deadInfo.lockedCapital, 0.001)
        
        val aliveInfo = detector.analyzeStock(now - (10L * 24 * 60 * 60 * 1000), 5, 100.0, now)
        assertFalse(aliveInfo.isDead)
        assertEquals(0.0, aliveInfo.lockedCapital, 0.001)
    }
}
