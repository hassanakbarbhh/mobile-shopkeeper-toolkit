package com.shopkeeper.mobileshop.domain

import com.shopkeeper.mobileshop.data.db.entity.RepairStatus

// 1. NetProfitEngine
class NetProfitEngine {
    fun calculateNetProfit(sellingPrice: Double, purchasePrice: Double, discount: Double, tax: Double): Double {
        return (sellingPrice - discount + tax) - purchasePrice
    }
}

// 2. ImeiUniquenessGuard
class ImeiUniquenessGuard {
    fun isImeiUnique(newImei: String, existingImeis: List<String>): Boolean {
        if (newImei.isBlank()) return true // Accessories don't have IMEI
        return !existingImeis.contains(newImei)
    }
}

// 3. MarginGuard
class MarginGuard {
    fun isMarginAcceptable(sellingPrice: Double, purchasePrice: Double, minimumMarginPercent: Double): Boolean {
        if (purchasePrice <= 0) return true
        val margin = ((sellingPrice - purchasePrice) / purchasePrice) * 100
        return margin >= minimumMarginPercent
    }
}

// 4. UdhaarAgingAnalyzer
class UdhaarAgingAnalyzer {
    fun getAgingCategory(dueDateMillis: Long, currentTimeMillis: Long = System.currentTimeMillis()): String {
        val diffDays = (currentTimeMillis - dueDateMillis) / (1000 * 60 * 60 * 24)
        return when {
            diffDays < 0 -> "Not Due"
            diffDays in 0..30 -> "0-30 Days Overdue"
            diffDays in 31..60 -> "31-60 Days Overdue"
            diffDays in 61..90 -> "61-90 Days Overdue"
            else -> "90+ Days Overdue (Critical)"
        }
    }
}

// 5. LowStockAlertEngine
class LowStockAlertEngine {
    fun requiresReorder(currentQuantity: Int, minimumThreshold: Int): Boolean {
        return currentQuantity <= minimumThreshold
    }
}

// 6. RepairStatusStateRouter
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
}

// 7. SalesCommissionCalculator
class SalesCommissionCalculator {
    fun calculateCommission(profit: Double, commissionRate: Double): Double {
        if (profit <= 0) return 0.0
        return profit * (commissionRate / 100)
    }
}

// 8. CustomerTrustScorer
class CustomerTrustScorer {
    fun calculateScore(totalPurchases: Int, overduePayments: Int): Int {
        val baseScore = 100
        val penalty = overduePayments * 15
        val bonus = totalPurchases * 2
        return (baseScore - penalty + bonus).coerceIn(0, 100)
    }
}

// 9. InventoryValuationEngine
class InventoryValuationEngine {
    fun calculateTotalValue(products: List<Pair<Double, Int>>): Double { // Pair<PurchasePrice, Quantity>
        return products.sumOf { it.first * it.second }
    }
}

// 10. DiscountApprovalGuard
class DiscountApprovalGuard {
    fun requiresOwnerApproval(discountAmount: Double, totalPrice: Double, maxAllowedPercent: Double): Boolean {
        if (totalPrice <= 0) return false
        val discountPercent = (discountAmount / totalPrice) * 100
        return discountPercent > maxAllowedPercent
    }
}

// 11. CashClosingReconciler
class CashClosingReconciler {
    fun reconcile(openingBalance: Double, cashSales: Double, cashExpenses: Double, actualClosingCash: Double): Double {
        val expectedCash = openingBalance + cashSales - cashExpenses
        return actualClosingCash - expectedCash // Returns variance (0 means perfect)
    }
}

// 12. BulkImportSanitizer
class BulkImportSanitizer {
    fun sanitizeProductName(rawName: String): String {
        return rawName.trim().replace(Regex("\\s+"), " ")
    }
    fun isValidPrice(price: Double): Boolean = price >= 0
}

// 13. WarrantyExpirationTracker
class WarrantyExpirationTracker {
    fun isWarrantyValid(saleDateMillis: Long, warrantyMonths: Int, currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        if (warrantyMonths <= 0) return false
        val expiryTime = saleDateMillis + (warrantyMonths * 30L * 24L * 60L * 60L * 1000L) // Approx 30 days/month
        return currentTimeMillis <= expiryTime
    }
}

// 14. FastMovingItemDetector
class FastMovingItemDetector {
    fun isFastMoving(salesLast30Days: Int, threshold: Int): Boolean {
        return salesLast30Days >= threshold
    }
}

// 15. DuplicateCustomerMerger
class DuplicateCustomerMerger {
    fun isDuplicate(phone1: String, phone2: String): Boolean {
        val clean1 = phone1.replace(Regex("[^0-9]"), "")
        val clean2 = phone2.replace(Regex("[^0-9]"), "")
        return clean1 == clean2 && clean1.isNotEmpty()
    }
}


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
