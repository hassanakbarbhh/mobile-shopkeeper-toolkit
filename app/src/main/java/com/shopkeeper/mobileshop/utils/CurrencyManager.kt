package com.shopkeeper.mobileshop.utils

import android.content.Context
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val label: String
)

object CurrencyManager {
    private const val PREFS = "shop_currency_prefs"
    private const val KEY_CURRENCY_CODE = "currency_code"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"

    val CURRENCIES = listOf(
        CurrencyItem("PKR", "Rs", "PKR - Pakistani Rupee (Rs)"),
        CurrencyItem("USD", "$", "USD - US Dollar ($)"),
        CurrencyItem("EUR", "€", "EUR - Euro (€)"),
        CurrencyItem("GBP", "£", "GBP - British Pound (£)"),
        CurrencyItem("AED", "AED", "AED - UAE Dirham (AED)"),
        CurrencyItem("SAR", "SAR", "SAR - Saudi Riyal (SAR)"),
        CurrencyItem("INR", "₹", "INR - Indian Rupee (₹)"),
        CurrencyItem("BDT", "Tk", "BDT - Bangladeshi Taka (Tk)"),
        CurrencyItem("CUSTOM", "", "Custom Currency Symbol...")
    )

    @Volatile
    private var cachedSymbol: String = "Rs"

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        cachedSymbol = prefs.getString(KEY_CURRENCY_SYMBOL, "Rs") ?: "Rs"
    }

    fun getSymbol(context: Context? = null): String {
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            cachedSymbol = prefs.getString(KEY_CURRENCY_SYMBOL, "Rs") ?: "Rs"
        }
        return cachedSymbol
    }

    fun getCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY_CODE, "PKR") ?: "PKR"
    }

    fun setCurrency(context: Context, code: String, symbol: String) {
        cachedSymbol = symbol.trim()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_CURRENCY_CODE, code)
            .putString(KEY_CURRENCY_SYMBOL, cachedSymbol)
            .apply()
    }

    fun format(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        val formatter = if (amount % 1.0 == 0.0) {
            DecimalFormat("#,##0", symbols)
        } else {
            DecimalFormat("#,##0.00", symbols)
        }
        val formattedNumber = formatter.format(amount)
        return "$cachedSymbol $formattedNumber"
    }
}
