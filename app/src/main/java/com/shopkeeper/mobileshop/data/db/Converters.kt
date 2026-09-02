package com.shopkeeper.mobileshop.data.db

import androidx.room.TypeConverter
import com.shopkeeper.mobileshop.data.db.entity.*

class Converters {
    @TypeConverter fun fromProductCategory(v: ProductCategory) = v.name
    @TypeConverter fun toProductCategory(v: String) = runCatching { ProductCategory.valueOf(v) }.getOrDefault(ProductCategory.OTHER)
    @TypeConverter fun fromProductCondition(v: ProductCondition) = v.name
    @TypeConverter fun toProductCondition(v: String) = runCatching { ProductCondition.valueOf(v) }.getOrDefault(ProductCondition.NEW)
    @TypeConverter fun fromPaymentMethod(v: PaymentMethod) = v.name
    @TypeConverter fun toPaymentMethod(v: String) = runCatching { PaymentMethod.valueOf(v) }.getOrDefault(PaymentMethod.CASH)
    @TypeConverter fun fromPaymentStatus(v: PaymentStatus) = v.name
    @TypeConverter fun toPaymentStatus(v: String) = runCatching { PaymentStatus.valueOf(v) }.getOrDefault(PaymentStatus.PAID)
    @TypeConverter fun fromRepairStatus(v: RepairStatus) = v.name
    @TypeConverter fun toRepairStatus(v: String) = runCatching { RepairStatus.valueOf(v) }.getOrDefault(RepairStatus.RECEIVED)
    @TypeConverter fun fromPaymentType(v: PaymentType) = v.name
    @TypeConverter fun toPaymentType(v: String) = runCatching { PaymentType.valueOf(v) }.getOrDefault(PaymentType.RECEIVED)
    @TypeConverter fun fromExpenseCategory(v: ExpenseCategory) = v.name
    @TypeConverter fun toExpenseCategory(v: String) = runCatching { ExpenseCategory.valueOf(v) }.getOrDefault(ExpenseCategory.OTHER)
}
