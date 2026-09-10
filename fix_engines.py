import re

with open("app/src/main/java/com/shopkeeper/mobileshop/domain/BusinessEngines.kt", "r") as f:
    content = f.read()

# Add import
if "import com.shopkeeper.mobileshop.data.db.entity.RepairStatus" not in content:
    content = content.replace("package com.shopkeeper.mobileshop.domain", "package com.shopkeeper.mobileshop.domain\n\nimport com.shopkeeper.mobileshop.data.db.entity.RepairStatus")

# Replace RepairStatusStateRouter
old_router = """// 6. RepairStatusStateRouter
class RepairStatusStateRouter {
    fun getNextStatus(currentStatus: String): String {
        return when (currentStatus) {
            "INTAKE" -> "DIAGNOSING"
            "DIAGNOSING" -> "WAITING_FOR_PARTS"
            "WAITING_FOR_PARTS" -> "REPAIRED"
            "REPAIRED" -> "DELIVERED"
            else -> "DELIVERED"
        }
    }
}"""

new_router = """// 6. RepairStatusStateRouter
class RepairStatusStateRouter {
    fun getNextStatus(currentStatus: RepairStatus): RepairStatus {
        return when (currentStatus) {
            RepairStatus.RECEIVED -> RepairStatus.DIAGNOSING
            RepairStatus.DIAGNOSING -> RepairStatus.WAITING_PARTS
            RepairStatus.WAITING_PARTS -> RepairStatus.IN_REPAIR
            RepairStatus.IN_REPAIR -> RepairStatus.COMPLETED
            RepairStatus.COMPLETED -> RepairStatus.DELIVERED
            RepairStatus.DELIVERED -> RepairStatus.DELIVERED
            RepairStatus.CANCELLED -> RepairStatus.CANCELLED
        }
    }
}"""

content = content.replace(old_router, new_router)

# Add the 4 new engines at the end if not exist
new_engines = """

// 16. StockoutForecaster
class StockoutForecaster {
    fun forecastDaysLeft(currentQty: Int, avgDailySales: Double): Int {
        if (avgDailySales <= 0.0) return Int.MAX_VALUE
        return (currentQty / avgDailySales).toInt()
    }
}

// 17. SupplierScore
class SupplierScoreEngine {
    fun calculateScore(priceTrend: Double, fulfillmentRate: Double): Double {
        val priceScore = if (priceTrend <= 0) 50.0 else (50.0 - (priceTrend * 100)).coerceAtLeast(0.0)
        val fulfillmentScore = fulfillmentRate * 50.0
        return priceScore + fulfillmentScore
    }
}

// 18. OfflineConflictResolver
data class ConflictRecord(val id: String, val reason: String)
class OfflineConflictResolver {
    fun detectConflicts(
        localUpdatedAt: Long, 
        remoteUpdatedAt: Long, 
        localImei: String, 
        remoteImei: String
    ): List<ConflictRecord> {
        val conflicts = mutableListOf<ConflictRecord>()
        if (localImei == remoteImei && localUpdatedAt < remoteUpdatedAt) {
             conflicts.add(ConflictRecord(localImei, "Remote has newer update for same IMEI"))
        }
        return conflicts
    }
}

// 19. DeadStockDetector
data class DeadStockInfo(val isDead: Boolean, val lockedCapital: Double)
class DeadStockDetector {
    fun analyzeStock(lastSaleDateMillis: Long?, qty: Int, cost: Double, currentTimeMillis: Long = System.currentTimeMillis()): DeadStockInfo {
        if (lastSaleDateMillis == null) {
            return DeadStockInfo(false, 0.0) 
        }
        val daysSinceLastSale = (currentTimeMillis - lastSaleDateMillis) / (1000 * 60 * 60 * 24)
        val isDead = daysSinceLastSale >= 60 && qty > 0
        val lockedCapital = if (isDead) (qty * cost) else 0.0
        return DeadStockInfo(isDead, lockedCapital)
    }
}
"""

if "StockoutForecaster" not in content:
    content += new_engines

with open("app/src/main/java/com/shopkeeper/mobileshop/domain/BusinessEngines.kt", "w") as f:
    f.write(content)
