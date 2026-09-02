package com.shopkeeper.mobileshop.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.money(): String =
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(this)

fun Long.dateText(): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))

fun Long.dateTimeText(): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(this))
