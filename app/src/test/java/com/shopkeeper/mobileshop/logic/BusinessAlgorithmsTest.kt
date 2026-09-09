package com.shopkeeper.mobileshop.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class BusinessAlgorithmsTest {

    @Test
    fun testNetProfitEngine() {
        assertEquals(5000.0, NetProfitEngine.calculate(15000.0, 8000.0, 2000.0), 0.01)
    }

    @Test
    fun testImeiUniquenessGuard() {
        assertTrue(ImeiUniquenessGuard.isUnique("123", listOf("456", "789")))
        assertFalse(ImeiUniquenessGuard.isUnique("456", listOf("456", "789")))
    }

    @Test
    fun testMarginGuard() {
        assertEquals(50.0, MarginGuard.calculateMargin(100.0, 150.0), 0.01)
        assertTrue(MarginGuard.isMarginSafe(15.0))
        assertFalse(MarginGuard.isMarginSafe(5.0))
    }

    @Test
    fun testUdhaarAging() {
        val today = Date()
        assertEquals(0, UdhaarAging.calculateDaysPending(today))
        assertEquals("Safe", UdhaarAging.getRiskCategory(-5))
        assertEquals("Low Risk", UdhaarAging.getRiskCategory(15))
        assertEquals("Medium Risk", UdhaarAging.getRiskCategory(60))
        assertEquals("High Risk", UdhaarAging.getRiskCategory(100))
    }

    @Test
    fun testReorderPoint() {
        assertTrue(ReorderPoint.shouldReorder(5, 10))
        assertFalse(ReorderPoint.shouldReorder(15, 10))
        assertEquals(15, ReorderPoint.calculateReorderAmount(20, 5))
    }
}
