package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.data.db.entity.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private fun escapeCsv(value: Any?): String {
        if (value == null) return ""
        val str = value.toString()
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    // ==========================================
    // 1. PRODUCTS / STOCK INVENTORY LEDGER
    // ==========================================

    fun exportProductsCsv(context: Context, products: List<Product>): File {
        val fileName = "Inventory_Stock_Ledger_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        
        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM for Microsoft Excel compatibility
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("ID,Product Name,Brand,Model,Category,Stock Quantity,Purchase Price,Selling Price,Condition,RAM,Storage,Color,IMEI,Warranty (Months),Notes\n")
                
                products.forEach { p ->
                    writer.append(escapeCsv(p.id)).append(",")
                        .append(escapeCsv(p.name)).append(",")
                        .append(escapeCsv(p.brand)).append(",")
                        .append(escapeCsv(p.model)).append(",")
                        .append(escapeCsv(p.category.name)).append(",")
                        .append(escapeCsv(p.quantity)).append(",")
                        .append(escapeCsv(p.purchasePrice)).append(",")
                        .append(escapeCsv(p.sellingPrice)).append(",")
                        .append(escapeCsv(p.condition.name)).append(",")
                        .append(escapeCsv(p.ram)).append(",")
                        .append(escapeCsv(p.storage)).append(",")
                        .append(escapeCsv(p.color)).append(",")
                        .append(escapeCsv(p.imei)).append(",")
                        .append(escapeCsv(p.warrantyMonths)).append(",")
                        .append(escapeCsv(p.notes)).append("\n")
                }
                writer.flush()
            }
        }
        return file
    }

    fun exportProductsPdf(context: Context, products: List<Product>): File {
        val fileName = "Inventory_Stock_Report_${dateFormat.format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)
        val doc = PdfDocument()

        val totalStock = products.sumOf { it.quantity }
        val totalStockValue = products.sumOf { it.quantity * it.purchasePrice }
        val inStockCount = products.count { it.quantity > 0 }
        val defaultModelCount = products.count { it.quantity == 0 && it.sellingPrice == 0.0 }

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawHeader(c: android.graphics.Canvas, p: Paint) {
            // Dark green top bar
            p.color = Color.parseColor("#14532D")
            c.drawRect(0f, 0f, 595f, 75f, p)

            p.color = Color.WHITE
            p.textSize = 18f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText(ShopProfile.name(context), 30f, 32f, p)

            p.textSize = 10f
            p.typeface = Typeface.DEFAULT
            c.drawText("Phone: ${ShopProfile.phone(context)} | ${ShopProfile.address(context)}", 30f, 50f, p)
            c.drawText("Generated: ${displayDateFormat.format(Date())}", 30f, 65f, p)

            p.textAlign = Paint.Align.RIGHT
            p.textSize = 14f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("INVENTORY LEDGER", 565f, 40f, p)
            p.textSize = 9f
            p.typeface = Typeface.DEFAULT
            c.drawText("Items: ${products.size} • In Stock: $inStockCount • Default Models: $defaultModelCount", 565f, 58f, p)
            p.textAlign = Paint.Align.LEFT
        }

        fun drawTableHeader(c: android.graphics.Canvas, p: Paint, y: Float) {
            p.color = Color.parseColor("#E8F5E9")
            c.drawRect(30f, y - 14f, 565f, y + 6f, p)

            p.color = Color.parseColor("#166534")
            p.textSize = 9.5f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("ID", 34f, y, p)
            c.drawText("PRODUCT / PHONE MODEL", 65f, y, p)
            c.drawText("BRAND", 280f, y, p)
            c.drawText("SPEC", 360f, y, p)
            c.drawText("STOCK", 450f, y, p)
            p.textAlign = Paint.Align.RIGHT
            c.drawText("PRICE", 560f, y, p)
            p.textAlign = Paint.Align.LEFT
        }

        drawHeader(canvas, paint)

        // Summary Bar on Page 1
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, 85f, 565f, 115f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Total Items: ${products.size}", 40f, 103f, paint)
        canvas.drawText("Total Units in Stock: $totalStock", 180f, 103f, paint)
        canvas.drawText("Inventory Valuation: ${totalStockValue.money()}", 360f, 103f, paint)

        var yPos = 145f
        drawTableHeader(canvas, paint, yPos)
        yPos += 18f

        products.forEachIndexed { idx, item ->
            if (yPos > 790f) {
                // Page footer
                paint.color = Color.GRAY
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
                paint.textAlign = Paint.Align.LEFT

                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                canvas = page.canvas
                drawHeader(canvas, paint)
                yPos = 95f
                drawTableHeader(canvas, paint, yPos)
                yPos += 18f
            }

            // Alternating row background
            if (idx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, yPos - 12f, 565f, yPos + 4f, paint)
            }

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("#${item.id}", 34f, yPos, paint)

            val nameStr = if (item.name.length > 34) item.name.substring(0, 31) + "..." else item.name
            canvas.drawText(nameStr, 65f, yPos, paint)
            canvas.drawText(item.brand, 280f, yPos, paint)

            val specStr = listOf(item.ram, item.storage).filter { it.isNotBlank() }.joinToString("/")
            canvas.drawText(if (specStr.isNotBlank()) specStr else "-", 360f, yPos, paint)

            // Stock badge text
            if (item.quantity > 0) {
                paint.color = Color.parseColor("#166534")
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("${item.quantity} pcs", 450f, yPos, paint)
            } else {
                paint.color = Color.parseColor("#64748B")
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("0 (Catalog)", 450f, yPos, paint)
            }

            // Price
            paint.textAlign = Paint.Align.RIGHT
            if (item.sellingPrice > 0) {
                paint.color = Color.parseColor("#0F172A")
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(item.sellingPrice.money(), 560f, yPos, paint)
            } else {
                paint.color = Color.parseColor("#94A3B8")
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("No Price Set", 560f, yPos, paint)
            }
            paint.textAlign = Paint.Align.LEFT

            yPos += 16f
        }

        // Final footer
        paint.color = Color.GRAY
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
        paint.textAlign = Paint.Align.LEFT

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ==========================================
    // 2. CUSTOMERS LEDGER & DIRECTORY
    // ==========================================

    fun exportCustomersCsv(context: Context, customers: List<Customer>): File {
        val fileName = "Customers_Ledger_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        
        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("Customer ID,Name,Phone Number,Email,Address,Notes,Created Date\n")
                
                customers.forEach { c ->
                    writer.append(escapeCsv(c.id)).append(",")
                        .append(escapeCsv(c.name)).append(",")
                        .append(escapeCsv(c.phone)).append(",")
                        .append(escapeCsv(c.email)).append(",")
                        .append(escapeCsv(c.address)).append(",")
                        .append(escapeCsv(c.notes)).append(",")
                        .append(escapeCsv(displayDateFormat.format(Date(c.createdAt)))).append("\n")
                }
                writer.flush()
            }
        }
        return file
    }

    fun exportCustomersPdf(context: Context, customers: List<Customer>): File {
        val fileName = "Customers_Report_${dateFormat.format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)
        val doc = PdfDocument()

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawHeader(c: android.graphics.Canvas, p: Paint) {
            p.color = Color.parseColor("#14532D")
            c.drawRect(0f, 0f, 595f, 75f, p)

            p.color = Color.WHITE
            p.textSize = 18f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText(ShopProfile.name(context), 30f, 32f, p)

            p.textSize = 10f
            p.typeface = Typeface.DEFAULT
            c.drawText("Phone: ${ShopProfile.phone(context)} | ${ShopProfile.address(context)}", 30f, 50f, p)
            c.drawText("Generated: ${displayDateFormat.format(Date())}", 30f, 65f, p)

            p.textAlign = Paint.Align.RIGHT
            p.textSize = 14f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("CUSTOMER LEDGER", 565f, 40f, p)
            p.textSize = 9f
            p.typeface = Typeface.DEFAULT
            c.drawText("Total Customers: ${customers.size}", 565f, 58f, p)
            p.textAlign = Paint.Align.LEFT
        }

        fun drawTableHeader(c: android.graphics.Canvas, p: Paint, y: Float) {
            p.color = Color.parseColor("#E8F5E9")
            c.drawRect(30f, y - 14f, 565f, y + 6f, p)

            p.color = Color.parseColor("#166534")
            p.textSize = 9.5f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("ID", 34f, y, p)
            c.drawText("CUSTOMER NAME", 65f, y, p)
            c.drawText("PHONE", 210f, y, p)
            c.drawText("ADDRESS", 320f, y, p)
            c.drawText("EMAIL / NOTES", 440f, y, p)
        }

        drawHeader(canvas, paint)

        // Summary on Page 1
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, 85f, 565f, 115f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Total Registered Customers: ${customers.size}", 40f, 103f, paint)
        canvas.drawText("Report Status: Complete Customer Directory", 270f, 103f, paint)

        var yPos = 145f
        drawTableHeader(canvas, paint, yPos)
        yPos += 18f

        customers.forEachIndexed { idx, item ->
            if (yPos > 790f) {
                paint.color = Color.GRAY
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
                paint.textAlign = Paint.Align.LEFT

                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                canvas = page.canvas
                drawHeader(canvas, paint)
                yPos = 95f
                drawTableHeader(canvas, paint, yPos)
                yPos += 18f
            }

            if (idx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, yPos - 12f, 565f, yPos + 4f, paint)
            }

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("#${item.id}", 34f, yPos, paint)

            val nameStr = if (item.name.length > 22) item.name.substring(0, 20) + "..." else item.name
            canvas.drawText(nameStr, 65f, yPos, paint)
            canvas.drawText(item.phone, 210f, yPos, paint)

            val addrStr = if (item.address.length > 20) item.address.substring(0, 18) + "..." else item.address
            canvas.drawText(if (addrStr.isNotBlank()) addrStr else "-", 320f, yPos, paint)

            val noteStr = if (item.email.isNotBlank()) item.email else (if (item.notes.isNotBlank()) item.notes else "-")
            val displayNote = if (noteStr.length > 22) noteStr.substring(0, 20) + "..." else noteStr
            canvas.drawText(displayNote, 440f, yPos, paint)

            yPos += 16f
        }

        paint.color = Color.GRAY
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
        paint.textAlign = Paint.Align.LEFT

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ==========================================
    // 3. SALES & INVOICES LEDGER
    // ==========================================

    fun exportSalesCsv(context: Context, sales: List<Sale>): File {
        val fileName = "Sales_Invoices_Ledger_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        
        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("Invoice ID,Date,Customer Name,Seller/Staff,Subtotal,Discount,Tax,Final Total,Payment Method,Payment Status,Notes\n")
                
                sales.forEach { s ->
                    writer.append(escapeCsv(s.id)).append(",")
                        .append(escapeCsv(displayDateFormat.format(Date(s.saleDate)))).append(",")
                        .append(escapeCsv(s.customerName)).append(",")
                        .append(escapeCsv(s.sellerName)).append(",")
                        .append(escapeCsv(s.totalAmount)).append(",")
                        .append(escapeCsv(s.discount)).append(",")
                        .append(escapeCsv(s.taxAmount)).append(",")
                        .append(escapeCsv(s.finalAmount)).append(",")
                        .append(escapeCsv(s.paymentMethod.name)).append(",")
                        .append(escapeCsv(s.paymentStatus.name)).append(",")
                        .append(escapeCsv(s.notes)).append("\n")
                }
                writer.flush()
            }
        }
        return file
    }

    fun exportSalesPdf(context: Context, sales: List<Sale>): File {
        val fileName = "Sales_Ledger_Report_${dateFormat.format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)
        val doc = PdfDocument()

        val totalSalesRevenue = sales.sumOf { it.finalAmount }
        val totalDiscounts = sales.sumOf { it.discount }

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawHeader(c: android.graphics.Canvas, p: Paint) {
            p.color = Color.parseColor("#14532D")
            c.drawRect(0f, 0f, 595f, 75f, p)

            p.color = Color.WHITE
            p.textSize = 18f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText(ShopProfile.name(context), 30f, 32f, p)

            p.textSize = 10f
            p.typeface = Typeface.DEFAULT
            c.drawText("Phone: ${ShopProfile.phone(context)} | ${ShopProfile.address(context)}", 30f, 50f, p)
            c.drawText("Generated: ${displayDateFormat.format(Date())}", 30f, 65f, p)

            p.textAlign = Paint.Align.RIGHT
            p.textSize = 14f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("SALES & INVOICES LEDGER", 565f, 40f, p)
            p.textSize = 9f
            p.typeface = Typeface.DEFAULT
            c.drawText("Total Invoices: ${sales.size}", 565f, 58f, p)
            p.textAlign = Paint.Align.LEFT
        }

        fun drawTableHeader(c: android.graphics.Canvas, p: Paint, y: Float) {
            p.color = Color.parseColor("#E8F5E9")
            c.drawRect(30f, y - 14f, 565f, y + 6f, p)

            p.color = Color.parseColor("#166534")
            p.textSize = 9.5f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("INV #", 34f, y, p)
            c.drawText("DATE", 75f, y, p)
            c.drawText("CUSTOMER", 145f, y, p)
            c.drawText("SELLER / STAFF", 290f, y, p)
            c.drawText("STATUS", 420f, y, p)
            p.textAlign = Paint.Align.RIGHT
            c.drawText("AMOUNT", 560f, y, p)
            p.textAlign = Paint.Align.LEFT
        }

        drawHeader(canvas, paint)

        // Summary
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, 85f, 565f, 115f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Total Invoices: ${sales.size}", 40f, 103f, paint)
        canvas.drawText("Total Revenue: ${totalSalesRevenue.money()}", 220f, 103f, paint)
        canvas.drawText("Total Discounts: ${totalDiscounts.money()}", 400f, 103f, paint)

        var yPos = 145f
        drawTableHeader(canvas, paint, yPos)
        yPos += 18f

        sales.forEachIndexed { idx, item ->
            if (yPos > 790f) {
                paint.color = Color.GRAY
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
                paint.textAlign = Paint.Align.LEFT

                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                canvas = page.canvas
                drawHeader(canvas, paint)
                yPos = 95f
                drawTableHeader(canvas, paint, yPos)
                yPos += 18f
            }

            if (idx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, yPos - 12f, 565f, yPos + 4f, paint)
            }

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("#${item.id.toString().padStart(4, '0')}", 34f, yPos, paint)
            canvas.drawText(shortDateFormat.format(Date(item.saleDate)), 75f, yPos, paint)

            val custStr = if (item.customerName.length > 20) item.customerName.substring(0, 18) + "..." else item.customerName
            canvas.drawText(custStr, 145f, yPos, paint)

            val sellerStr = if (item.sellerName.isNotBlank()) item.sellerName else "Direct"
            canvas.drawText(if (sellerStr.length > 18) sellerStr.substring(0, 16) + "..." else sellerStr, 290f, yPos, paint)

            if (item.paymentStatus == PaymentStatus.PAID) {
                paint.color = Color.parseColor("#166534")
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("PAID", 420f, yPos, paint)
            } else {
                paint.color = Color.parseColor("#DC2626")
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(item.paymentStatus.name, 420f, yPos, paint)
            }

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(item.finalAmount.money(), 560f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT

            yPos += 16f
        }

        paint.color = Color.GRAY
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
        paint.textAlign = Paint.Align.LEFT

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ==========================================
    // 4. DAILY PURCHASES & STOCK-IN LEDGER
    // ==========================================

    fun exportPurchasesCsv(context: Context, purchases: List<Purchase>): File {
        val fileName = "Purchases_Ledger_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        
        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("Purchase ID,Date,Supplier Name,Total Cost,Paid Amount,Payment Status,Notes\n")
                
                purchases.forEach { p ->
                    writer.append(escapeCsv(p.id)).append(",")
                        .append(escapeCsv(displayDateFormat.format(Date(p.purchaseDate)))).append(",")
                        .append(escapeCsv(p.supplierName)).append(",")
                        .append(escapeCsv(p.totalCost)).append(",")
                        .append(escapeCsv(p.paidAmount)).append(",")
                        .append(escapeCsv(p.paymentStatus.name)).append(",")
                        .append(escapeCsv(p.notes)).append("\n")
                }
                writer.flush()
            }
        }
        return file
    }

    fun exportPurchasesPdf(context: Context, purchases: List<Purchase>): File {
        val fileName = "Purchases_Report_${dateFormat.format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)
        val doc = PdfDocument()

        val totalPurchasesCost = purchases.sumOf { it.totalCost }
        val totalPaid = purchases.sumOf { it.paidAmount }

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawHeader(c: android.graphics.Canvas, p: Paint) {
            p.color = Color.parseColor("#14532D")
            c.drawRect(0f, 0f, 595f, 75f, p)

            p.color = Color.WHITE
            p.textSize = 18f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText(ShopProfile.name(context), 30f, 32f, p)

            p.textSize = 10f
            p.typeface = Typeface.DEFAULT
            c.drawText("Phone: ${ShopProfile.phone(context)} | ${ShopProfile.address(context)}", 30f, 50f, p)
            c.drawText("Generated: ${displayDateFormat.format(Date())}", 30f, 65f, p)

            p.textAlign = Paint.Align.RIGHT
            p.textSize = 14f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("DAILY PURCHASES LEDGER", 565f, 40f, p)
            p.textSize = 9f
            p.typeface = Typeface.DEFAULT
            c.drawText("Total Purchase Orders: ${purchases.size}", 565f, 58f, p)
            p.textAlign = Paint.Align.LEFT
        }

        fun drawTableHeader(c: android.graphics.Canvas, p: Paint, y: Float) {
            p.color = Color.parseColor("#E8F5E9")
            c.drawRect(30f, y - 14f, 565f, y + 6f, p)

            p.color = Color.parseColor("#166534")
            p.textSize = 9.5f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("PURCHASE #", 34f, y, p)
            c.drawText("DATE", 110f, y, p)
            c.drawText("SUPPLIER NAME", 195f, y, p)
            c.drawText("STATUS", 350f, y, p)
            p.textAlign = Paint.Align.RIGHT
            c.drawText("PAID", 470f, y, p)
            c.drawText("TOTAL COST", 560f, y, p)
            p.textAlign = Paint.Align.LEFT
        }

        drawHeader(canvas, paint)

        // Summary
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, 85f, 565f, 115f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Total Purchases: ${purchases.size}", 40f, 103f, paint)
        canvas.drawText("Total Cost Spent: ${totalPurchasesCost.money()}", 200f, 103f, paint)
        canvas.drawText("Total Supplier Paid: ${totalPaid.money()}", 380f, 103f, paint)

        var yPos = 145f
        drawTableHeader(canvas, paint, yPos)
        yPos += 18f

        purchases.forEachIndexed { idx, item ->
            if (yPos > 790f) {
                paint.color = Color.GRAY
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
                paint.textAlign = Paint.Align.LEFT

                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                canvas = page.canvas
                drawHeader(canvas, paint)
                yPos = 95f
                drawTableHeader(canvas, paint, yPos)
                yPos += 18f
            }

            if (idx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, yPos - 12f, 565f, yPos + 4f, paint)
            }

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("PO #${item.id.toString().padStart(4, '0')}", 34f, yPos, paint)
            canvas.drawText(shortDateFormat.format(Date(item.purchaseDate)), 110f, yPos, paint)

            val supStr = if (item.supplierName.length > 22) item.supplierName.substring(0, 20) + "..." else item.supplierName
            canvas.drawText(supStr, 195f, yPos, paint)

            if (item.paymentStatus == PaymentStatus.PAID) {
                paint.color = Color.parseColor("#166534")
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("PAID", 350f, yPos, paint)
            } else {
                paint.color = Color.parseColor("#D97706")
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(item.paymentStatus.name, 350f, yPos, paint)
            }

            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.parseColor("#166534")
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(item.paidAmount.money(), 470f, yPos, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(item.totalCost.money(), 560f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT

            yPos += 16f
        }

        paint.color = Color.GRAY
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
        paint.textAlign = Paint.Align.LEFT

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ==========================================
    // 5. REPAIRS & SERVICE TICKETS LEDGER
    // ==========================================

    fun exportRepairsCsv(context: Context, repairs: List<Repair>): File {
        val fileName = "Repairs_Ledger_${dateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        
        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("Ticket ID,Date Received,Customer Name,Phone Number,Device Brand,Device Model,IMEI,Issue Description,Status,Estimated Cost,Actual Cost\n")
                
                repairs.forEach { r ->
                    writer.append(escapeCsv(r.id)).append(",")
                        .append(escapeCsv(displayDateFormat.format(Date(r.receivedDate)))).append(",")
                        .append(escapeCsv(r.customerName)).append(",")
                        .append(escapeCsv(r.customerPhone)).append(",")
                        .append(escapeCsv(r.deviceBrand)).append(",")
                        .append(escapeCsv(r.deviceModel)).append(",")
                        .append(escapeCsv(r.imei)).append(",")
                        .append(escapeCsv(r.issueDescription)).append(",")
                        .append(escapeCsv(r.status.name)).append(",")
                        .append(escapeCsv(r.estimatedCost)).append(",")
                        .append(escapeCsv(r.actualCost)).append("\n")
                }
                writer.flush()
            }
        }
        return file
    }

    fun exportRepairsPdf(context: Context, repairs: List<Repair>): File {
        val fileName = "Repairs_Report_${dateFormat.format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)
        val doc = PdfDocument()

        val totalEstimated = repairs.sumOf { it.estimatedCost }
        val totalActual = repairs.sumOf { it.actualCost }

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawHeader(c: android.graphics.Canvas, p: Paint) {
            p.color = Color.parseColor("#14532D")
            c.drawRect(0f, 0f, 595f, 75f, p)

            p.color = Color.WHITE
            p.textSize = 18f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText(ShopProfile.name(context), 30f, 32f, p)

            p.textSize = 10f
            p.typeface = Typeface.DEFAULT
            c.drawText("Phone: ${ShopProfile.phone(context)} | ${ShopProfile.address(context)}", 30f, 50f, p)
            c.drawText("Generated: ${displayDateFormat.format(Date())}", 30f, 65f, p)

            p.textAlign = Paint.Align.RIGHT
            p.textSize = 14f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("REPAIR JOBS LEDGER", 565f, 40f, p)
            p.textSize = 9f
            p.typeface = Typeface.DEFAULT
            c.drawText("Total Jobs: ${repairs.size}", 565f, 58f, p)
            p.textAlign = Paint.Align.LEFT
        }

        fun drawTableHeader(c: android.graphics.Canvas, p: Paint, y: Float) {
            p.color = Color.parseColor("#E8F5E9")
            c.drawRect(30f, y - 14f, 565f, y + 6f, p)

            p.color = Color.parseColor("#166534")
            p.textSize = 9.5f
            p.typeface = Typeface.DEFAULT_BOLD
            c.drawText("TICKET", 34f, y, p)
            c.drawText("CUSTOMER", 85f, y, p)
            c.drawText("DEVICE MODEL", 195f, y, p)
            c.drawText("ISSUE", 310f, y, p)
            c.drawText("STATUS", 420f, y, p)
            p.textAlign = Paint.Align.RIGHT
            c.drawText("COST", 560f, y, p)
            p.textAlign = Paint.Align.LEFT
        }

        drawHeader(canvas, paint)

        // Summary
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, 85f, 565f, 115f, paint)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Total Jobs: ${repairs.size}", 40f, 103f, paint)
        canvas.drawText("Total Estimated: ${totalEstimated.money()}", 200f, 103f, paint)
        canvas.drawText("Total Billed: ${totalActual.money()}", 380f, 103f, paint)

        var yPos = 145f
        drawTableHeader(canvas, paint, yPos)
        yPos += 18f

        repairs.forEachIndexed { idx, item ->
            if (yPos > 790f) {
                paint.color = Color.GRAY
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
                paint.textAlign = Paint.Align.LEFT

                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                canvas = page.canvas
                drawHeader(canvas, paint)
                yPos = 95f
                drawTableHeader(canvas, paint, yPos)
                yPos += 18f
            }

            if (idx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, yPos - 12f, 565f, yPos + 4f, paint)
            }

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("#${item.id}", 34f, yPos, paint)

            val custStr = if (item.customerName.length > 18) item.customerName.substring(0, 16) + "..." else item.customerName
            canvas.drawText(custStr, 85f, yPos, paint)

            val devStr = "${item.deviceBrand} ${item.deviceModel}"
            canvas.drawText(if (devStr.length > 18) devStr.substring(0, 16) + "..." else devStr, 195f, yPos, paint)

            val issueStr = if (item.issueDescription.length > 18) item.issueDescription.substring(0, 16) + "..." else item.issueDescription
            canvas.drawText(issueStr, 310f, yPos, paint)

            paint.typeface = Typeface.DEFAULT_BOLD
            if (item.status == RepairStatus.DELIVERED) {
                paint.color = Color.parseColor("#166534")
            } else {
                paint.color = Color.parseColor("#D97706")
            }
            canvas.drawText(item.status.name, 420f, yPos, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textAlign = Paint.Align.RIGHT
            val costToDisplay = if (item.actualCost > 0) item.actualCost else item.estimatedCost
            canvas.drawText(costToDisplay.money(), 560f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT

            yPos += 16f
        }

        paint.color = Color.GRAY
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page $pageNumber • Mobile Inventory Toolkit • Hassan", 297f, 825f, paint)
        paint.textAlign = Paint.Align.LEFT

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ==========================================
    // 6. MASTER SHOP COMPLETE CONSOLIDATED REPORT
    // ==========================================

    fun exportMasterLedgerPdf(
        context: Context,
        products: List<Product>,
        sales: List<Sale>,
        purchases: List<Purchase>,
        customers: List<Customer>,
        repairs: List<Repair>
    ): File {
        val fileName = "Complete_Shop_Master_Ledger_${dateFormat.format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)
        val doc = PdfDocument()

        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Top Header
        paint.color = Color.parseColor("#14532D")
        canvas.drawRect(0f, 0f, 595f, 95f, paint)

        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(ShopProfile.name(context), 30f, 38f, paint)

        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Phone: ${ShopProfile.phone(context)} | ${ShopProfile.address(context)}", 30f, 58f, paint)
        canvas.drawText("Generated on: ${displayDateFormat.format(Date())}", 30f, 75f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 15f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("MASTER SHOP LEDGER", 565f, 48f, paint)
        paint.textAlign = Paint.Align.LEFT

        var y = 125f

        fun drawSectionCard(title: String, rows: List<Pair<String, String>>, colorHex: String) {
            paint.color = Color.parseColor(colorHex)
            canvas.drawRoundRect(30f, y, 565f, y + 26f + (rows.size * 20f), 12f, 12f, paint)

            paint.color = Color.parseColor("#14532D")
            paint.textSize = 12f
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(title, 45f, y + 18f, paint)

            paint.typeface = Typeface.DEFAULT
            paint.textSize = 10f
            var rowY = y + 36f
            rows.forEach { (label, value) ->
                paint.color = Color.parseColor("#334155")
                canvas.drawText(label, 45f, rowY, paint)

                paint.textAlign = Paint.Align.RIGHT
                paint.color = Color.parseColor("#0F172A")
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(value, 550f, rowY, paint)
                paint.typeface = Typeface.DEFAULT
                paint.textAlign = Paint.Align.LEFT

                rowY += 18f
            }
            y = rowY + 12f
        }

        val totalStockQty = products.sumOf { it.quantity }
        val totalStockVal = products.sumOf { it.quantity * it.purchasePrice }
        val modelsCount = products.size
        drawSectionCard(
            "📦 INVENTORY & PHONE STOCK",
            listOf(
                "Total Smartphone Models in Catalog" to "$modelsCount models",
                "Total Physical Stock Units on Hand" to "$totalStockQty units",
                "Total Inventory Valuation" to totalStockVal.money()
            ),
            "#F0FDF4"
        )

        val totalSalesRev = sales.sumOf { it.finalAmount }
        val totalSalesCount = sales.size
        drawSectionCard(
            "💰 SALES & INVOICES",
            listOf(
                "Total Invoices Generated" to "$totalSalesCount sales",
                "Total Sales Turnover" to totalSalesRev.money()
            ),
            "#EFF6FF"
        )

        val totalPurchasesCost = purchases.sumOf { it.totalCost }
        val totalPurchasesCount = purchases.size
        drawSectionCard(
            "📥 PURCHASES & SUPPLIER EXPENSES",
            listOf(
                "Total Purchase Orders" to "$totalPurchasesCount orders",
                "Total Cost Spent on Stock" to totalPurchasesCost.money()
            ),
            "#FFFBEB"
        )

        val totalCustomersCount = customers.size
        drawSectionCard(
            "👥 CUSTOMER DIRECTORY",
            listOf(
                "Registered Customers" to "$totalCustomersCount customers",
                "Contact Records" to "Complete names & phone numbers on file"
            ),
            "#FEF2F2"
        )

        val totalRepairsCount = repairs.size
        val activeRepairsCount = repairs.count { it.status != RepairStatus.DELIVERED && it.status != RepairStatus.CANCELLED }
        drawSectionCard(
            "🔧 REPAIRS & SERVICE TICKETS",
            listOf(
                "Total Repair Jobs Logged" to "$totalRepairsCount tickets",
                "Active Repairs Currently in Workshop" to "$activeRepairsCount active"
            ),
            "#F8FAFC"
        )

        // Footer
        paint.color = Color.GRAY
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Mobile Inventory Toolkit • Crafted with ❤ by Hassan", 297f, 810f, paint)
        paint.textAlign = Paint.Align.LEFT

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ==========================================
    // 7. FILE SHARING, OPENING & DIALOG HELPER
    // ==========================================

    fun openFile(context: Context, file: File, mimeType: String) {
        val authority = "${context.packageName}.fileprovider"
        try {
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Open File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = try {
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            Toast.makeText(context, "Error creating shareable link: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun showLedgerExportDialog(
        context: Context,
        ledgerTitle: String,
        onExportCsv: () -> File,
        onExportPdf: () -> File
    ) {
        val options = arrayOf(
            "📊 Open in Excel / CSV Spreadsheet (.csv)",
            "📄 Open in PDF Viewer (.pdf)",
            "📤 Share Excel Spreadsheet (WhatsApp, Mail, Drive)",
            "📤 Share PDF Document (WhatsApp, Mail, Drive)"
        )

        MaterialAlertDialogBuilder(context)
            .setTitle("Export $ledgerTitle")
            .setItems(options) { _, which ->
                try {
                    when (which) {
                        0 -> {
                            val file = onExportCsv()
                            openFile(context, file, "text/csv")
                        }
                        1 -> {
                            val file = onExportPdf()
                            openFile(context, file, "application/pdf")
                        }
                        2 -> {
                            val file = onExportCsv()
                            shareFile(context, file, "text/csv", "Share $ledgerTitle (Excel/CSV)")
                        }
                        3 -> {
                            val file = onExportPdf()
                            shareFile(context, file, "application/pdf", "Share $ledgerTitle (PDF)")
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun shareWhatsApp(context: Context, phone: String, message: String) {
        try {
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            val url = "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Send Message").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    fun openDialer(context: Context, phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
