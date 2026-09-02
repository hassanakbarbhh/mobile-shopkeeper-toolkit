package com.shopkeeper.mobileshop.data.db

import androidx.room.TypeConverter
import com.shopkeeper.mobileshop.data.db.entity.*

class Converters {
    @TypeConverter fun fromProductCategory(v: ProductCategory) = v.name
    @TypeConverter fun toProductCategory(v: String) = ProductCategory.valueOf(v)
    @TypeConverter fun fromProductCondition(v: ProductCondition) = v.name
    @TypeConverter fun toProductCondition(v: String) = ProductCondition.valueOf(v)
    @TypeConverter fun fromPaymentMethod(v: PaymentMethod) = v.name
    @TypeConverter fun toPaymentMethod(v: String) = PaymentMethod.valueOf(v)
    @TypeConverter fun fromPaymentStatus(v: PaymentStatus) = v.name
    @TypeConverter fun toPaymentStatus(v: String) = PaymentStatus.valueOf(v)
    @TypeConverter fun fromRepairStatus(v: RepairStatus) = v.name
    @TypeConverter fun toRepairStatus(v: String) = RepairStatus.valueOf(v)
    @TypeConverter fun fromPaymentType(v: PaymentType) = v.name
    @TypeConverter fun toPaymentType(v: String) = PaymentType.valueOf(v)
    @TypeConverter fun fromExpenseCategory(v: ExpenseCategory) = v.name
    @TypeConverter fun toExpenseCategory(v: String) = ExpenseCategory.valueOf(v)
}
