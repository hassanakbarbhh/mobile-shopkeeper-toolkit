import re

with open("app/src/test/java/com/shopkeeper/mobileshop/domain/BusinessEnginesTest.kt", "r") as f:
    content = f.read()

# Add import
if "import com.shopkeeper.mobileshop.data.db.entity.RepairStatus" not in content:
    content = content.replace("package com.shopkeeper.mobileshop.domain", "package com.shopkeeper.mobileshop.domain\n\nimport com.shopkeeper.mobileshop.data.db.entity.RepairStatus")

# Fix router test
old_router_test = """    @Test
    fun testRepairStatusStateRouter() {
        val router = RepairStatusStateRouter()
        assertEquals("DIAGNOSING", router.getNextStatus("INTAKE"))
        assertEquals("REPAIRED", router.getNextStatus("WAITING_FOR_PARTS"))
    }"""

new_router_test = """    @Test
    fun testRepairStatusStateRouter() {
        val router = RepairStatusStateRouter()
        assertEquals(RepairStatus.DIAGNOSING, router.getNextStatus(RepairStatus.RECEIVED))
        assertEquals(RepairStatus.IN_REPAIR, router.getNextStatus(RepairStatus.WAITING_PARTS))
        assertEquals(RepairStatus.DELIVERED, router.getNextStatus(RepairStatus.DELIVERED))
    }"""
content = content.replace(old_router_test, new_router_test)

new_tests = """
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
"""

if "testStockoutForecaster" not in content:
    content = content.replace("}\n", new_tests + "\n}\n")

with open("app/src/test/java/com/shopkeeper/mobileshop/domain/BusinessEnginesTest.kt", "w") as f:
    f.write(content)
