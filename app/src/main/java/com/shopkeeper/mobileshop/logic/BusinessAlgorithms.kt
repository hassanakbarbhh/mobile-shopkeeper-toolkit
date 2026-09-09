package com.shopkeeper.mobileshop.logic

import java.util.Date
import java.util.concurrent.TimeUnit

object NetProfitEngine {
    fun calculate(revenue: Double, cogs: Double, expenses: Double): Double {
        return revenue - cogs - expenses
    }
}

object ImeiUniquenessGuard {
    fun isUnique(imei: String, existingList: List<String>): Boolean {
        return !existingList.contains(imei)
    }
}

object MarginGuard {
    fun calculateMargin(costPrice: Double, sellingPrice: Double): Double {
        if (costPrice == 0.0) return 100.0
        return ((sellingPrice - costPrice) / costPrice) * 100
    }
    
    fun isMarginSafe(margin: Double, minimumThreshold: Double = 10.0): Boolean {
        return margin >= minimumThreshold
    }
}

object UdhaarAging {
    fun calculateDaysPending(dueDate: Date): Long {
        val diff = Date().time - dueDate.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }
    
    fun getRiskCategory(daysPending: Long): String {
        return when {
            daysPending <= 0 -> "Safe"
            daysPending <= 30 -> "Low Risk"
            daysPending <= 90 -> "Medium Risk"
            else -> "High Risk"
        }
    }
}

object ReorderPoint {
    fun shouldReorder(currentStock: Int, minimumStock: Int): Boolean {
        return currentStock <= minimumStock
    }
    
    fun calculateReorderAmount(maximumStock: Int, currentStock: Int): Int {
        return maxOf(0, maximumStock - currentStock)
    }
}
