package com.shopkeeper.mobileshop.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.entity.Repair
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.db.entity.SaleItem
import com.shopkeeper.mobileshop.databinding.DialogThermalReceiptBinding

object ThermalPrintHelper {

    fun showSaleReceiptDialog(ctx: Context, sale: Sale, items: List<SaleItem>) {
        var currentWidthMm = AppPreferences.getThermalPaperWidth(ctx)
        var currentBitmap: Bitmap = ThermalReceiptManager.generateSaleReceiptBitmap(ctx, sale, items, currentWidthMm)
        var currentText: String = ThermalReceiptManager.generateSaleReceiptText(ctx, sale, items)

        val binding = DialogThermalReceiptBinding.inflate(LayoutInflater.from(ctx))
        binding.tvThermalDialogTitle.text = "POS Thermal Receipt #${sale.id}"
        binding.ivReceiptPreview.setImageBitmap(currentBitmap)

        if (currentWidthMm == 80) {
            binding.togglePaperSize.check(R.id.btn80mm)
        } else {
            binding.togglePaperSize.check(R.id.btn58mm)
        }

        var dialog: AlertDialog? = null

        fun refreshReceipt(widthMm: Int) {
            currentWidthMm = widthMm
            AppPreferences.setThermalPaperWidth(ctx, widthMm)
            currentBitmap = ThermalReceiptManager.generateSaleReceiptBitmap(ctx, sale, items, widthMm)
            currentText = ThermalReceiptManager.generateSaleReceiptText(ctx, sale, items)
            binding.ivReceiptPreview.setImageBitmap(currentBitmap)
        }

        binding.togglePaperSize.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btn80mm) refreshReceipt(80) else refreshReceipt(58)
            }
        }

        binding.btnPrintThermal.setOnClickListener {
            ThermalReceiptManager.sendToPrinterApp(ctx, currentBitmap, currentText)
        }

        binding.btnShareWhatsApp.setOnClickListener {
            ThermalReceiptManager.shareReceiptImage(ctx, currentBitmap, "Receipt #${sale.id}")
        }

        binding.btnCopyText.setOnClickListener {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Receipt Text", currentText))
            Toast.makeText(ctx, "Receipt text copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        binding.btnDismissReceipt.setOnClickListener {
            dialog?.dismiss()
        }

        dialog = MaterialAlertDialogBuilder(ctx)
            .setView(binding.root)
            .create()
        dialog.show()
    }

    fun showRepairTagDialog(ctx: Context, repair: Repair) {
        var currentWidthMm = AppPreferences.getThermalPaperWidth(ctx)
        var currentBitmap: Bitmap = ThermalReceiptManager.generateRepairThermalTagBitmap(ctx, repair, currentWidthMm)
        var currentText: String = ThermalReceiptManager.generateRepairTagText(ctx, repair)

        val binding = DialogThermalReceiptBinding.inflate(LayoutInflater.from(ctx))
        binding.tvThermalDialogTitle.text = "Repair Tag #${repair.id}"
        binding.ivReceiptPreview.setImageBitmap(currentBitmap)

        if (currentWidthMm == 80) {
            binding.togglePaperSize.check(R.id.btn80mm)
        } else {
            binding.togglePaperSize.check(R.id.btn58mm)
        }

        var dialog: AlertDialog? = null

        fun refreshTag(widthMm: Int) {
            currentWidthMm = widthMm
            AppPreferences.setThermalPaperWidth(ctx, widthMm)
            currentBitmap = ThermalReceiptManager.generateRepairThermalTagBitmap(ctx, repair, widthMm)
            currentText = ThermalReceiptManager.generateRepairTagText(ctx, repair)
            binding.ivReceiptPreview.setImageBitmap(currentBitmap)
        }

        binding.togglePaperSize.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btn80mm) refreshTag(80) else refreshTag(58)
            }
        }

        binding.btnPrintThermal.setOnClickListener {
            ThermalReceiptManager.sendToPrinterApp(ctx, currentBitmap, currentText)
        }

        binding.btnShareWhatsApp.setOnClickListener {
            ThermalReceiptManager.shareReceiptImage(ctx, currentBitmap, "Repair Tag #${repair.id}")
        }

        binding.btnCopyText.setOnClickListener {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Repair Tag Text", currentText))
            Toast.makeText(ctx, "Repair tag copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        binding.btnDismissReceipt.setOnClickListener {
            dialog?.dismiss()
        }

        dialog = MaterialAlertDialogBuilder(ctx)
            .setView(binding.root)
            .create()
        dialog.show()
    }
}
