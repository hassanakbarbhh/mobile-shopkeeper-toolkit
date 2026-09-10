package com.shopkeeper.mobileshop.domain

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
    fun getNextStatus(currentStatus: String): String {
        return when (currentStatus) {
            "INTAKE" -> "DIAGNOSING"
            "DIAGNOSING" -> "WAITING_FOR_PARTS"
            "WAITING_FOR_PARTS" -> "REPAIRED"
            "REPAIRED" -> "DELIVERED"
            else -> "DELIVERED"
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
