package com.shopkeeper.mobileshop.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import java.io.FileOutputStream

object DirectPrintHelper {

    /**
     * Prints a bitmap directly using Android's native PrintManager spooler.
     * This supports any connected Bluetooth, Wi-Fi, USB, or Network thermal printer,
     * as well as ESC/POS print services and Save to PDF.
     */
    fun printBitmapDirectly(activity: Activity, jobName: String, bitmap: Bitmap) {
        try {
            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(activity, "Print service is not available on this device", Toast.LENGTH_SHORT).show()
                return
            }

            val printAdapter = object : PrintDocumentAdapter() {
                private var pdfDocument: PdfDocument? = null

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback,
                    metadata: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onLayoutCancelled()
                        return
                    }

                    val info = PrintDocumentInfo.Builder("$jobName.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()

                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    pdfDocument = PdfDocument()

                    try {
                        val mediaSize = PrintAttributes.MediaSize.ISO_A4
                        val pageWidth = mediaSize.widthMils * 72 / 1000
                        val pageHeight = mediaSize.heightMils * 72 / 1000

                        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                        val page = pdfDocument!!.startPage(pageInfo)

                        if (cancellationSignal?.isCanceled == true) {
                            callback.onWriteCancelled()
                            pdfDocument?.close()
                            return
                        }

                        val canvas: Canvas = page.canvas
                        canvas.drawColor(Color.WHITE)

                        // Center the thermal receipt on page
                        val destWidth = (pageWidth * 0.75f).toInt()
                        val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                        val destHeight = (destWidth * aspectRatio).toInt()
                        val left = (pageWidth - destWidth) / 2
                        val top = 36

                        val destRect = Rect(left, top, left + destWidth, top + destHeight)
                        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                        canvas.drawBitmap(bitmap, null, destRect, paint)

                        pdfDocument!!.finishPage(page)

                        FileOutputStream(destination.fileDescriptor).use { out ->
                            pdfDocument!!.writeTo(out)
                        }

                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.localizedMessage)
                    } finally {
                        pdfDocument?.close()
                        pdfDocument = null
                    }
                }
            }

            val printAttributes = PrintAttributes.Builder()
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .build()

            printManager.print(jobName, printAdapter, printAttributes)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error starting print spooler: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates raw ESC/POS command bytes for standard thermal receipt printers.
     */
    fun buildEscPosCommands(text: String, cutPaper: Boolean = true): ByteArray {
        val bytes = mutableListOf<Byte>()

        // ESC @ : Initialize printer
        bytes.addAll(byteArrayOf(0x1B, 0x40).toList())

        // Character code table: PC437 (USA, Standard Europe)
        bytes.addAll(byteArrayOf(0x1B, 0x74, 0x00).toList())

        // Line spacing standard: ESC 2
        bytes.addAll(byteArrayOf(0x1B, 0x32).toList())

        // Content
        val textBytes = text.toByteArray(Charsets.US_ASCII)
        bytes.addAll(textBytes.toList())

        // Feed 4 lines: ESC d 4
        bytes.addAll(byteArrayOf(0x1B, 0x64, 0x04).toList())

        // GS V 66 0 : Cut paper (partial cut)
        if (cutPaper) {
            bytes.addAll(byteArrayOf(0x1D, 0x56, 0x42, 0x00).toList())
        }

        return bytes.toByteArray()
    }
}
