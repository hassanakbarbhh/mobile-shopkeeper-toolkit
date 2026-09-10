package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.db.entity.SaleItem
import java.io.File
import java.io.FileOutputStream

object InvoiceGenerator {

    fun generate(ctx: Context, sale: Sale, items: List<SaleItem>): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val c = page.canvas
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        p.color = Color.parseColor("#14532D")
        c.drawRect(0f, 0f, 595f, 120f, p)
        
        p.color = Color.WHITE
        p.textSize = 22f
        p.isFakeBoldText = true
        c.drawText(ShopProfile.name(ctx), 40f, 55f, p)
        p.isFakeBoldText = false
        p.textSize = 10f
        c.drawText(ShopProfile.address(ctx), 40f, 75f, p)
        c.drawText("Ph: ${ShopProfile.phone(ctx)}", 40f, 90f, p)
        p.textAlign = Paint.Align.RIGHT
        p.textSize = 12f
        p.isFakeBoldText = true
        c.drawText("INVOICE #${sale.id.toString().padStart(4, '0')}", 555f, 50f, p)
        p.isFakeBoldText = false
        p.textSize = 10f
        c.drawText(sale.saleDate.dateTimeText(), 555f, 68f, p)
        c.drawText("Customer: ${sale.customerName}", 555f, 84f, p)
        p.textAlign = Paint.Align.LEFT

        var y = 160f
        p.textSize = 11f
        p.isFakeBoldText = true
        p.color = Color.parseColor("#166534")
        c.drawText("ITEM", 40f, y, p)
        c.drawText("QTY", 430f, y, p)
        p.textAlign = Paint.Align.RIGHT
        c.drawText("AMOUNT", 555f, y, p)
        p.textAlign = Paint.Align.LEFT
        c.drawLine(40f, y + 8, 555f, y + 8, p)
        y += 26f

        items.forEach { it ->
            p.isFakeBoldText = true
            p.color = Color.BLACK
            p.textSize = 11f
            c.drawText(it.productName, 40f, y, p)
            p.isFakeBoldText = false
            c.drawText("${it.quantity}", 430f, y, p)
            p.textAlign = Paint.Align.RIGHT
            c.drawText(it.totalPrice.money(), 555f, y, p)
            p.textAlign = Paint.Align.LEFT
            y += 15f
            if (it.imei.isNotBlank()) {
                p.color = Color.GRAY
                p.textSize = 9f
                c.drawText("IMEI: ${it.imei}", 40f, y, p)
                y += 15f
            }
            y += 6f
        }

        y += 10f
        c.drawLine(40f, y, 555f, y, p)
        y += 22f
        p.textSize = 11f
        p.color = Color.BLACK
        fun row(label: String, value: String, bold: Boolean = false) {
            p.isFakeBoldText = bold
            c.drawText(label, 340f, y, p)
            p.textAlign = Paint.Align.RIGHT
            c.drawText(value, 555f, y, p)
            p.textAlign = Paint.Align.LEFT
            y += 20f
        }
        row("Subtotal", sale.totalAmount.money())
        row("Discount", "- ${sale.discount.money()}")
        row("Tax", "+ ${sale.taxAmount.money()}")
        p.color = Color.parseColor("#14532D")
        row("GRAND TOTAL", sale.finalAmount.money(), bold = true)
        p.color = Color.BLACK
        row("Payment", "${sale.paymentMethod} • ${sale.paymentStatus}")

        p.color = Color.GRAY
        p.textSize = 10f
        p.textAlign = Paint.Align.CENTER
        c.drawText("Thank you for your business!", 297f, 770f, p)
        c.drawText("Crafted with ❤ by Hassan — Mobile Shopkeeper Toolkit", 297f, 790f, p)
        p.textAlign = Paint.Align.LEFT

        doc.finishPage(page)
        val file = File(ctx.cacheDir, "INV-${sale.id}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
// Block C complete
