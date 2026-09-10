package com.shopkeeper.mobileshop.domain

import com.shopkeeper.mobileshop.data.db.entity.RepairStatus

class NetProfitEngine {
    fun calculateNetProfit(sellingPrice: Double, purchasePrice: Double,
                          discount: Double, tax: Double): Double =
        (sellingPrice - discount + tax) - purchasePrice
}

class ImeiUniquenessGuard {
    fun isImeiUnique(newImei: String, existingImeis: List<String>): Boolean {
        if (newImei.isBlank()) return true
        return !existingImeis.contains(newImei)
    }
}

class MarginGuard {
    fun isMarginAcceptable(sellingPrice: Double, purchasePrice: Double,
                          minimumMarginPercent: Double = 3.0): Boolean {
        if (purchasePrice <= 0) return true
        return ((sellingPrice - purchasePrice) / purchasePrice) * 100 >= minimumMarginPercent
    }
}

class UdhaarAgingAnalyzer {
    fun getAgingCategory(dueDateMillis: Long,
                        currentTimeMillis: Long = System.currentTimeMillis()): String {
        val diffDays = (currentTimeMillis - dueDateMillis) / (1000L * 60 * 60 * 24)
        return when {
            diffDays < 0 -> "Not Due"
            diffDays in 0..30 -> "0-30 Days Overdue"
            diffDays in 31..60 -> "31-60 Days Overdue"
            diffDays in 61..90 -> "61-90 Days Overdue"
            else -> "90+ Days Overdue (Critical)"
        }
    }
}

class LowStockAlertEngine {
    fun requiresReorder(currentQuantity: Int, minimumThreshold: Int): Boolean =
        currentQuantity <= minimumThreshold
}

class RepairStatusStateRouter {
    fun getNextStatus(current: RepairStatus): RepairStatus = when (current) {
        RepairStatus.RECEIVED      -> RepairStatus.DIAGNOSING
        RepairStatus.DIAGNOSING    -> RepairStatus.WAITING_PARTS
        RepairStatus.WAITING_PARTS -> RepairStatus.IN_REPAIR
        RepairStatus.IN_REPAIR     -> RepairStatus.COMPLETED
        RepairStatus.COMPLETED     -> RepairStatus.DELIVERED
        RepairStatus.DELIVERED     -> RepairStatus.DELIVERED
        RepairStatus.CANCELLED     -> RepairStatus.CANCELLED
    }
}

class SalesCommissionCalculator {
    fun calculateCommission(profit: Double, commissionRate: Double): Double =
        if (profit <= 0) 0.0 else profit * (commissionRate / 100)
}

class CustomerTrustScorer {
    fun calculateScore(totalPurchases: Int, overduePayments: Int): Int {
        val baseScore = 100
        val penalty = overduePayments * 15
        val bonus = totalPurchases * 2
        return (baseScore - penalty + bonus).coerceIn(0, 100)
    }
}

class InventoryValuationEngine {
    fun calculateTotalValue(products: List<Pair<Double, Int>>): Double =
        products.sumOf { it.first * it.second }
}

class DiscountApprovalGuard {
    fun requiresOwnerApproval(discountAmount: Double, totalPrice: Double,
                             maxAllowedPercent: Double): Boolean {
        if (totalPrice <= 0) return false
        return (discountAmount / totalPrice) * 100 > maxAllowedPercent
    }
}

class CashClosingReconciler {
    fun reconcile(openingBalance: Double, cashSales: Double,
                 cashExpenses: Double, actualClosingCash: Double): Double {
        val expectedCash = openingBalance + cashSales - cashExpenses
        return actualClosingCash - expectedCash
    }
}

class BulkImportSanitizer {
    fun sanitizeProductName(rawName: String): String =
        rawName.trim().replace(Regex("\\s+"), " ")
    fun isValidPrice(price: Double): Boolean = price >= 0
}

class WarrantyExpirationTracker {
    fun isWarrantyValid(saleDateMillis: Long, warrantyMonths: Int,
                       currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        if (warrantyMonths <= 0) return false
        val expiryTime = saleDateMillis + (warrantyMonths * 30L * 24L * 60L * 60L * 1000L)
        return currentTimeMillis <= expiryTime
    }
}

class FastMovingItemDetector {
    fun isFastMoving(salesLast30Days: Int, threshold: Int): Boolean =
        salesLast30Days >= threshold
}

class DuplicateCustomerMerger {
    fun isDuplicate(phone1: String, phone2: String): Boolean {
        val clean1 = phone1.replace(Regex("[^0-9]"), "")
        val clean2 = phone2.replace(Regex("[^0-9]"), "")
        return clean1 == clean2 && clean1.isNotEmpty()
    }
}

// === 4 NEW ENGINES ===

class StockoutForecaster {
    fun forecastDaysLeft(currentQty: Int, avgDailySales: Double): Int {
        if (avgDailySales <= 0.0) return Int.MAX_VALUE
        return (currentQty / avgDailySales).toInt()
    }
}

class SupplierScoreEngine {
    fun calculateScore(priceIncreasePercent: Double, fulfillmentRate: Double): Double {
        val pricePenalty = (priceIncreasePercent * 100).coerceAtMost(50.0)
        val fulfillmentBonus = fulfillmentRate * 50
        return (50.0 - pricePenalty + fulfillmentBonus).coerceIn(0.0, 100.0)
    }
}

data class Conflict(
    val imei: String,
    val localUpdatedAt: Long,
    val remoteUpdatedAt: Long
)

class OfflineConflictResolver {
    fun detectConflicts(localUpdatedAt: Long, remoteUpdatedAt: Long,
                       localImei: String, remoteImei: String): List<Conflict> {
        if (localImei != remoteImei || localImei.isBlank()) return emptyList()
        if (localUpdatedAt != remoteUpdatedAt) {
            return listOf(Conflict(localImei, localUpdatedAt, remoteUpdatedAt))
        }
        return emptyList()
    }
}

data class DeadStockInfo(val isDead: Boolean, val lockedCapital: Double, val daysSinceLastSale: Int)

class DeadStockDetector {
    fun analyzeStock(lastSaleDateMillis: Long, quantity: Int, costPrice: Double,
                    currentTimeMillis: Long = System.currentTimeMillis()): DeadStockInfo {
        val daysSince = ((currentTimeMillis - lastSaleDateMillis) / (1000L * 60 * 60 * 24)).toInt()
        val isDead = daysSince > 60 && quantity > 0
        return DeadStockInfo(
            isDead = isDead,
            lockedCapital = if (isDead) quantity * costPrice else 0.0,
            daysSinceLastSale = daysSince
        )
    }
}
