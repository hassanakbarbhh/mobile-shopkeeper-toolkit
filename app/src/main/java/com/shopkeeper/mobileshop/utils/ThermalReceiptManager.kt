package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.shopkeeper.mobileshop.data.db.entity.Repair
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.db.entity.SaleItem
import java.io.File
import java.io.FileOutputStream

object ThermalReceiptManager {

    fun getPixelWidth(widthMm: Int): Int {
        return if (widthMm == 80) 576 else 384
    }

    /**
     * Creates a high-contrast 58mm (384px) or 80mm (576px) monochrome Bitmap
     * suitable for all Bluetooth, USB, and WiFi ESC/POS thermal printers.
     */
    fun generateSaleReceiptBitmap(
        ctx: Context,
        sale: Sale,
        items: List<SaleItem>,
        widthMm: Int = AppPreferences.getThermalPaperWidth(ctx)
    ): Bitmap {
        val width = getPixelWidth(widthMm)
        val scale = if (widthMm == 80) 1.4f else 1.0f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.MONOSPACE
        }

        // Measure needed height
        val baseLineHeight = (18f * scale)
        val headerLines = 9
        val itemLines = items.sumOf { if (it.imei.isNotBlank()) 2 else 1 }
        val footerLines = 14
        val totalLines = headerLines + itemLines + footerLines + 6
        val estimatedHeight = (totalLines * baseLineHeight + 100 * scale).toInt()

        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var y = 24f * scale
        val left = 14f * scale
        val right = width - (14f * scale)
        val centerX = width / 2f

        fun drawDivider(doubleLine: Boolean = false) {
            paint.strokeWidth = if (doubleLine) 2.5f * scale else 1.5f * scale
            paint.style = Paint.Style.STROKE
            paint.pathEffect = if (doubleLine) null else DashPathEffect(floatArrayOf(6f * scale, 4f * scale), 0f)
            canvas.drawLine(left, y, right, y, paint)
            paint.pathEffect = null
            paint.style = Paint.Style.FILL
            y += 16f * scale
        }

        // Shop Name
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 20f * scale
        paint.isFakeBoldText = true
        canvas.drawText(ShopProfile.name(ctx).ifBlank { "MOBILE SHOP" }, centerX, y, paint)
        y += 20f * scale

        // Shop Subtitle & Phone
        paint.textSize = 12f * scale
        paint.isFakeBoldText = false
        val address = ShopProfile.address(ctx)
        if (address.isNotBlank()) {
            canvas.drawText(address, centerX, y, paint)
            y += 16f * scale
        }
        val phone = ShopProfile.phone(ctx)
        if (phone.isNotBlank()) {
            canvas.drawText("Tel: $phone", centerX, y, paint)
            y += 16f * scale
        }
        y += 4f * scale
        drawDivider(doubleLine = true)

        // Meta info
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f * scale
        paint.isFakeBoldText = true
        canvas.drawText("RECEIPT #: ${sale.id.toString().padStart(5, '0')}", left, y, paint)
        y += 16f * scale
        paint.isFakeBoldText = false
        canvas.drawText("Date: ${sale.saleDate.dateTimeText()}", left, y, paint)
        y += 16f * scale
        if (sale.sellerName.isNotBlank()) {
            canvas.drawText("Staff: ${sale.sellerName}", left, y, paint)
            y += 16f * scale
        }
        canvas.drawText("Customer: ${sale.customerName.ifBlank { "Cash Customer" }}", left, y, paint)
        y += 16f * scale

        drawDivider()

        // Table Header
        paint.isFakeBoldText = true
        paint.textSize = 11.5f * scale
        canvas.drawText("ITEM", left, y, paint)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("QTY", left + (right - left) * 0.65f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT", right, y, paint)
        y += 16f * scale
        drawDivider()

        // Table Items
        items.forEach { item ->
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = true
            paint.textSize = 12f * scale
            // Truncate long name to fit
            val maxLen = if (widthMm == 80) 24 else 16
            val shortName = if (item.productName.length > maxLen) item.productName.take(maxLen - 1) + "…" else item.productName
            canvas.drawText(shortName, left, y, paint)

            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = false
            canvas.drawText("${item.quantity}", left + (right - left) * 0.65f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(item.totalPrice.money(), right, y, paint)
            y += 16f * scale

            if (item.imei.isNotBlank()) {
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 10f * scale
                paint.isFakeBoldText = false
                canvas.drawText("  IMEI: ${item.imei}", left, y, paint)
                y += 15f * scale
            }
        }

        drawDivider()

        // Totals
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f * scale
        paint.isFakeBoldText = false
        canvas.drawText("Subtotal:", left, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(sale.totalAmount.money(), right, y, paint)
        y += 16f * scale

        if (sale.discount > 0) {
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Discount:", left, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("- ${sale.discount.money()}", right, y, paint)
            y += 16f * scale
        }

        drawDivider()

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 15f * scale
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL PAID:", left, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(sale.finalAmount.money(), right, y, paint)
        y += 22f * scale

        drawDivider(doubleLine = true)

        // Policies & Footer
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 11f * scale
        paint.isFakeBoldText = false
        canvas.drawText("* 3 Days Checking Warranty *", centerX, y, paint)
        y += 15f * scale
        canvas.drawText("* Goods sold cannot be returned *", centerX, y, paint)
        y += 15f * scale
        paint.isFakeBoldText = true
        canvas.drawText("Thank you for your visit!", centerX, y, paint)
        y += 18f * scale
        paint.textSize = 9.5f * scale
        paint.isFakeBoldText = false
        canvas.drawText("Software by Hassan Akbar (+923172377565)", centerX, y, paint)
        y += 18f * scale

        // Crop bitmap to actual y
        val finalHeight = (y + 16f * scale).toInt()
        return Bitmap.createBitmap(bitmap, 0, 0, width, finalHeight.coerceAtLeast(100))
    }

    /**
     * Generates a 2-part Repair Thermal Roll:
     * 1) Customer Repair Claim Receipt
     * 2) Device Attachment Tag (Tear off to tape onto the mobile phone)
     */
    fun generateRepairThermalTagBitmap(
        ctx: Context,
        repair: Repair,
        widthMm: Int = AppPreferences.getThermalPaperWidth(ctx)
    ): Bitmap {
        val width = getPixelWidth(widthMm)
        val scale = if (widthMm == 80) 1.4f else 1.0f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.MONOSPACE
        }

        val estimatedHeight = (680 * scale).toInt()
        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var y = 24f * scale
        val left = 14f * scale
        val right = width - (14f * scale)
        val centerX = width / 2f

        fun drawDivider(doubleLine: Boolean = false) {
            paint.strokeWidth = if (doubleLine) 2.5f * scale else 1.5f * scale
            paint.style = Paint.Style.STROKE
            paint.pathEffect = if (doubleLine) null else DashPathEffect(floatArrayOf(6f * scale, 4f * scale), 0f)
            canvas.drawLine(left, y, right, y, paint)
            paint.pathEffect = null
            paint.style = Paint.Style.FILL
            y += 16f * scale
        }

        // ================= PART 1: CUSTOMER CLAIM SLIP =================
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 18f * scale
        paint.isFakeBoldText = true
        canvas.drawText(ShopProfile.name(ctx).ifBlank { "REPAIR LAB" }, centerX, y, paint)
        y += 18f * scale

        paint.textSize = 12f * scale
        canvas.drawText("REPAIR CLAIM RECEIPT", centerX, y, paint)
        y += 16f * scale
        drawDivider(doubleLine = true)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f * scale
        paint.isFakeBoldText = true
        canvas.drawText("JOB TICKET: #${repair.id.toString().padStart(4, '0')}", left, y, paint)
        y += 18f * scale

        paint.textSize = 12f * scale
        paint.isFakeBoldText = false
        canvas.drawText("Date: ${repair.receivedDate.dateTimeText()}", left, y, paint)
        y += 16f * scale
        canvas.drawText("Customer: ${repair.customerName}", left, y, paint)
        y += 16f * scale
        if (repair.customerPhone.isNotBlank()) {
            canvas.drawText("Phone: ${repair.customerPhone}", left, y, paint)
            y += 16f * scale
        }

        drawDivider()

        paint.isFakeBoldText = true
        canvas.drawText("DEVICE: ${repair.deviceModel}", left, y, paint)
        y += 16f * scale
        if (repair.imei.isNotBlank()) {
            paint.isFakeBoldText = false
            paint.textSize = 10.5f * scale
            canvas.drawText("IMEI/SN: ${repair.imei}", left, y, paint)
            y += 15f * scale
        }
        paint.isFakeBoldText = false
        paint.textSize = 11.5f * scale
        canvas.drawText("FAULT: ${repair.issueDescription}", left, y, paint)
        y += 18f * scale

        drawDivider()

        canvas.drawText("Estimated Cost:", left, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.isFakeBoldText = true
        canvas.drawText(repair.estimatedCost.money(), right, y, paint)
        y += 16f * scale

        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = false
        canvas.drawText("Advance Paid:", left, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(repair.actualCost.money(), right, y, paint)
        y += 16f * scale

        val due = (repair.estimatedCost - repair.actualCost).coerceAtLeast(0.0)
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        canvas.drawText("Balance Due:", left, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(due.money(), right, y, paint)
        y += 18f * scale

        drawDivider()

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 9.5f * scale
        paint.isFakeBoldText = false
        canvas.drawText("• Present this slip upon device collection", centerX, y, paint)
        y += 14f * scale
        canvas.drawText("• Unclaimed devices disposed after 30 days", centerX, y, paint)
        y += 24f * scale

        // ================= TEAR-OFF LINE =================
        paint.color = Color.DKGRAY
        paint.strokeWidth = 2f * scale
        paint.style = Paint.Style.STROKE
        paint.pathEffect = DashPathEffect(floatArrayOf(12f * scale, 6f * scale), 0f)
        canvas.drawLine(left, y, right, y, paint)
        paint.pathEffect = null
        paint.style = Paint.Style.FILL
        y += 16f * scale

        paint.color = Color.BLACK
        paint.textSize = 10f * scale
        paint.isFakeBoldText = true
        canvas.drawText("✂  TEAR & ATTACH TO PHONE / BOX  ✂", centerX, y, paint)
        y += 20f * scale

        // ================= PART 2: DEVICE ATTACHMENT TAG =================
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 15f * scale
        paint.isFakeBoldText = true
        canvas.drawText("TAG #${repair.id.toString().padStart(4, '0')} | ${repair.deviceBrand} ${repair.deviceModel}", left, y, paint)
        y += 18f * scale

        paint.textSize = 12f * scale
        paint.isFakeBoldText = false
        canvas.drawText("Cust: ${repair.customerName} (${repair.customerPhone})", left, y, paint)
        y += 16f * scale
        canvas.drawText("Issue: ${repair.issueDescription}", left, y, paint)
        y += 16f * scale
        if (repair.notes.isNotBlank()) {
            canvas.drawText("Note/Lock: ${repair.notes}", left, y, paint)
            y += 16f * scale
        }
        canvas.drawText("Status: ${repair.status.name.replace('_', ' ')}", left, y, paint)
        y += 20f * scale

        val finalHeight = (y + 16f * scale).toInt()
        return Bitmap.createBitmap(bitmap, 0, 0, width, finalHeight.coerceAtLeast(100))
    }

    /**
     * Formats receipt as plain text suitable for clipboard, SMS, or RawBT printer app.
     */
    fun generateSaleReceiptText(ctx: Context, sale: Sale, items: List<SaleItem>): String {
        val sb = StringBuilder()
        val div = "================================"
        val subDiv = "--------------------------------"
        sb.appendLine(ShopProfile.name(ctx).ifBlank { "MOBILE SHOP" })
        if (ShopProfile.address(ctx).isNotBlank()) sb.appendLine(ShopProfile.address(ctx))
        if (ShopProfile.phone(ctx).isNotBlank()) sb.appendLine("Tel: ${ShopProfile.phone(ctx)}")
        sb.appendLine(div)
        sb.appendLine("RECEIPT #: ${sale.id.toString().padStart(5, '0')}")
        sb.appendLine("Date: ${sale.saleDate.dateTimeText()}")
        if (sale.sellerName.isNotBlank()) sb.appendLine("Staff: ${sale.sellerName}")
        sb.appendLine("Customer: ${sale.customerName.ifBlank { "Cash Customer" }}")
        sb.appendLine(subDiv)
        sb.appendLine("ITEM             QTY   AMOUNT")
        sb.appendLine(subDiv)
        items.forEach { item ->
            sb.appendLine("${item.productName.take(16).padEnd(17)}${item.quantity.toString().padEnd(6)}${item.totalPrice.money()}")
            if (item.imei.isNotBlank()) {
                sb.appendLine("  IMEI: ${item.imei}")
            }
        }
        sb.appendLine(subDiv)
        sb.appendLine("Subtotal:       ${sale.totalAmount.money()}")
        if (sale.discount > 0) sb.appendLine("Discount:      -${sale.discount.money()}")
        sb.appendLine("NET TOTAL:      ${sale.finalAmount.money()}")
        sb.appendLine(div)
        sb.appendLine("* 3 Days Checking Warranty *")
        sb.appendLine("Thank you for shopping with us!")
        sb.appendLine("App by Hassan Akbar (+923172377565)")
        return sb.toString()
    }

    fun generateRepairTagText(ctx: Context, repair: Repair): String {
        val sb = StringBuilder()
        val div = "================================"
        val subDiv = "--------------------------------"
        sb.appendLine(ShopProfile.name(ctx).ifBlank { "MOBILE REPAIR" })
        sb.appendLine("REPAIR CLAIM TICKET: #${repair.id.toString().padStart(4, '0')}")
        sb.appendLine(div)
        sb.appendLine("Customer: ${repair.customerName} (${repair.customerPhone})")
        sb.appendLine("Device:   ${repair.deviceBrand} ${repair.deviceModel}")
        if (repair.imei.isNotBlank()) sb.appendLine("IMEI/SN:  ${repair.imei}")
        sb.appendLine("Fault:    ${repair.issueDescription}")
        sb.appendLine(subDiv)
        sb.appendLine("Est Cost:     ${repair.estimatedCost.money()}")
        sb.appendLine("Advance Paid: ${repair.actualCost.money()}")
        val due = (repair.estimatedCost - repair.actualCost).coerceAtLeast(0.0)
        sb.appendLine("Balance Due:  ${due.money()}")
        sb.appendLine(div)
        sb.appendLine("Present this receipt to collect device.")
        sb.appendLine("Contact: ${ShopProfile.phone(ctx)}")
        return sb.toString()
    }

    /**
     * Saves bitmap as PNG in app cache/files and returns shareable URI using FileProvider.
     */
    fun saveBitmapToFile(ctx: Context, bitmap: Bitmap, fileName: String): File {
        val dir = File(ctx.cacheDir, "thermal_receipts").apply { mkdirs() }
        val file = File(dir, "$fileName.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    fun getShareUri(ctx: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            file
        )
    }

    /**
     * Shares thermal receipt image to WhatsApp or system share dialog.
     */
    fun shareReceiptImage(ctx: Context, bitmap: Bitmap, title: String) {
        runCatching {
            val file = saveBitmapToFile(ctx, bitmap, "receipt_${System.currentTimeMillis()}")
            val uri = getShareUri(ctx, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Thermal Receipt").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(chooser)
        }.onFailure {
            Toast.makeText(ctx, "Failed to share receipt: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sends plain text / image to thermal printer companion apps (like RawBT, Bluetooth Print, ESC POS Print Service).
     */
    fun sendToPrinterApp(ctx: Context, bitmap: Bitmap, plainText: String) {
        runCatching {
            val file = saveBitmapToFile(ctx, bitmap, "print_${System.currentTimeMillis()}")
            val uri = getShareUri(ctx, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, plainText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Select Thermal Printer App (e.g. RawBT)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(chooser)
        }.onFailure {
            Toast.makeText(ctx, "Print intent error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
