package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {
    fun generateInvoice(
        context: Context,
        shopName: String,
        customerName: String,
        items: List<Pair<String, Double>>,
        total: Double
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
        }

        var yPos = 50f
        
        // Shop Name
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(shopName, 50f, yPos, paint)
        yPos += 40f
        
        // Title
        paint.textSize = 18f
        paint.isFakeBoldText = false
        canvas.drawText("Sales Invoice", 50f, yPos, paint)
        yPos += 30f

        // Date
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        paint.textSize = 14f
        canvas.drawText("Date: $dateStr", 50f, yPos, paint)
        yPos += 20f

        // Customer
        canvas.drawText("Customer: $customerName", 50f, yPos, paint)
        yPos += 40f

        // Items Header
        paint.isFakeBoldText = true
        canvas.drawText("Item", 50f, yPos, paint)
        canvas.drawText("Price", 450f, yPos, paint)
        yPos += 20f
        
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 20f

        // Items List
        paint.isFakeBoldText = false
        items.forEach { (name, price) ->
            canvas.drawText(name, 50f, yPos, paint)
            canvas.drawText(String.format(Locale.getDefault(), "Rs %.2f", price), 450f, yPos, paint)
            yPos += 20f
        }

        yPos += 10f
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 30f

        // Total
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Total:", 350f, yPos, paint)
        canvas.drawText(String.format(Locale.getDefault(), "Rs %.2f", total), 450f, yPos, paint)

        pdfDocument.finishPage(page)

        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "Invoice_${System.currentTimeMillis()}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }
}
