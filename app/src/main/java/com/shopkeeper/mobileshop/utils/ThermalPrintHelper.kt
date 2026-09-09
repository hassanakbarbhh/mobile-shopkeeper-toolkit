package com.shopkeeper.mobileshop.utils

import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers






import android.app.Activity
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
import com.shopkeeper.mobileshop.databinding.DialogCustomThermalPrintBinding
import com.shopkeeper.mobileshop.databinding.DialogThermalCustomOptionsBinding
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

        fun refreshReceipt(widthMm: Int = currentWidthMm) {
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

        // Direct System Print (USB / Bluetooth / Wi-Fi / System Spooler)
        binding.btnPrintDirect.setOnClickListener {
            val savedAddress = AppPreferences.getBluetoothPrinterAddress(ctx)
            if (savedAddress.isEmpty()) {
                BluetoothPrinterManager.showPrinterSelectionDialog(ctx) { selectedAddress ->
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        BluetoothPrinterManager.connectAndPrint(ctx, selectedAddress, currentText)
                    }
                }
            } else {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    BluetoothPrinterManager.connectAndPrint(ctx, savedAddress, currentText)
                }
            }
        }

        // Send to RawBT or other thermal printer companion apps
        binding.btnPrintThermal.setOnClickListener {
            ThermalReceiptManager.sendToPrinterApp(ctx, currentBitmap, currentText)
        }

        // Share via WhatsApp or image apps
        binding.btnShareWhatsApp.setOnClickListener {
            WhatsAppHelper.sendTextToWhatsApp(ctx, "", currentText)
        }

        // Custom Thermal Options (Header, Footer, Barcode, etc.)
        binding.btnThermalOptions.setOnClickListener {
            showThermalCustomOptionsDialog(ctx) {
                refreshReceipt(currentWidthMm)
            }
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

        fun refreshTag(widthMm: Int = currentWidthMm) {
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

        binding.btnPrintDirect.setOnClickListener {
            val savedAddress = AppPreferences.getBluetoothPrinterAddress(ctx)
            if (savedAddress.isEmpty()) {
                BluetoothPrinterManager.showPrinterSelectionDialog(ctx) { selectedAddress ->
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        BluetoothPrinterManager.connectAndPrint(ctx, selectedAddress, currentText)
                    }
                }
            } else {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    BluetoothPrinterManager.connectAndPrint(ctx, savedAddress, currentText)
                }
            }
        }

        binding.btnPrintThermal.setOnClickListener {
            ThermalReceiptManager.sendToPrinterApp(ctx, currentBitmap, currentText)
        }

        binding.btnShareWhatsApp.setOnClickListener {
            WhatsAppHelper.sendTextToWhatsApp(ctx, repair.customerPhone, currentText)
        }

        binding.btnThermalOptions.setOnClickListener {
            showThermalCustomOptionsDialog(ctx) {
                refreshTag(currentWidthMm)
            }
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

    /**
     * Dialog to configure thermal printer options: Custom Header, Custom Footer, Barcode, IMEI, Copies.
     */
    fun showThermalCustomOptionsDialog(ctx: Context, onSaved: () -> Unit = {}) {
        val binding = DialogThermalCustomOptionsBinding.inflate(LayoutInflater.from(ctx))

        binding.etThermalHeader.setText(AppPreferences.getThermalCustomHeader(ctx))
        binding.etThermalFooter.setText(AppPreferences.getThermalCustomFooter(ctx))
        binding.switchPrintBarcode.isChecked = AppPreferences.isThermalShowBarcode(ctx)
        binding.switchPrintImei.isChecked = AppPreferences.isThermalShowImei(ctx)
        binding.switchPrintPhone.isChecked = AppPreferences.isThermalShowPhone(ctx)

        var copies = AppPreferences.getThermalCopies(ctx)
        binding.tvCopiesCount.text = copies.toString()

        binding.btnMinusCopy.setOnClickListener {
            if (copies > 1) {
                copies--
                binding.tvCopiesCount.text = copies.toString()
            }
        }

        binding.btnPlusCopy.setOnClickListener {
            if (copies < 5) {
                copies++
                binding.tvCopiesCount.text = copies.toString()
            }
        }

        val currentWidth = AppPreferences.getThermalPaperWidth(ctx)
        if (currentWidth == 80) {
            binding.togglePaperSizeOptions.check(R.id.btnOption80mm)
        } else {
            binding.togglePaperSizeOptions.check(R.id.btnOption58mm)
        }

        var dialog: AlertDialog? = null

        binding.btnCancelOptions.setOnClickListener {
            dialog?.dismiss()
        }

        binding.btnSaveOptions.setOnClickListener {
            val header = binding.etThermalHeader.text?.toString().orEmpty().trim()
            val footer = binding.etThermalFooter.text?.toString().orEmpty().trim()
            val showBarcode = binding.switchPrintBarcode.isChecked
            val showImei = binding.switchPrintImei.isChecked
            val showPhone = binding.switchPrintPhone.isChecked
            val selectedWidth = if (binding.togglePaperSizeOptions.checkedButtonId == R.id.btnOption80mm) 80 else 58

            AppPreferences.setThermalCustomHeader(ctx, header)
            AppPreferences.setThermalCustomFooter(ctx, footer)
            AppPreferences.setThermalShowBarcode(ctx, showBarcode)
            AppPreferences.setThermalShowImei(ctx, showImei)
            AppPreferences.setThermalShowPhone(ctx, showPhone)
            AppPreferences.setThermalCopies(ctx, copies)
            AppPreferences.setThermalPaperWidth(ctx, selectedWidth)

            Toast.makeText(ctx, "Thermal printer options saved!", Toast.LENGTH_SHORT).show()
            onSaved()
            dialog?.dismiss()
        }

        dialog = MaterialAlertDialogBuilder(ctx)
            .setView(binding.root)
            .create()
        dialog.show()
    }

    /**
     * Dialog for arbitrary custom thermal printing: user can create custom bills, repair tokens, or notices.
     */
    fun showCustomThermalPrintDialog(ctx: Context) {
        val binding = DialogCustomThermalPrintBinding.inflate(LayoutInflater.from(ctx))
        var currentWidthMm = AppPreferences.getThermalPaperWidth(ctx)

        if (currentWidthMm == 80) {
            binding.toggleCustomWidth.check(R.id.btnCustom80mm)
        } else {
            binding.toggleCustomWidth.check(R.id.btnCustom58mm)
        }

        binding.toggleCustomWidth.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentWidthMm = if (checkedId == R.id.btnCustom80mm) 80 else 58
            }
        }

        // Quick Preset Chips
        binding.chipPresetAdvance.setOnClickListener {
            binding.etCustomPrintTitle.setText("CUSTOMER ADVANCE TOKEN")
            binding.etCustomPrintContent.setText(
                "Customer: John Doe\nPhone: +1 555-0199\nItem: Screen Replacement\nAdvance Paid: $30.00\nBalance Due:  $20.00\n==============================\nDelivery Date: Tomorrow 5 PM\nPlease bring this slip to collect."
            )
        }

        binding.chipPresetRepairQuote.setOnClickListener {
            binding.etCustomPrintTitle.setText("REPAIR ESTIMATE & QUOTE")
            binding.etCustomPrintContent.setText(
                "Device: Samsung Galaxy S22\nIssue: No Display / Broken OLED\n------------------------------\n* OLED Screen Assembly: $120.00\n* Labor & Waterproofing: $25.00\n==============================\n* Total Estimate: $145.00\nQuote valid for 7 business days."
            )
        }

        binding.chipPresetWarranty.setOnClickListener {
            binding.etCustomPrintTitle.setText("WARRANTY CERTIFICATE")
            binding.etCustomPrintContent.setText(
                "Product: Fast Charger 65W GaN\nSerial: GAN-2026-9921\nWarranty Period: 6 Months\nValid Until: 05 September 2026\n------------------------------\nWarranty covers manufacturer defects.\nPhysical or liquid damage void."
            )
        }

        binding.chipPresetNotice.setOnClickListener {
            binding.etCustomPrintTitle.setText("STORE COUNTER NOTICE")
            binding.etCustomPrintContent.setText(
                "NOTICE TO OUR VALUED CUSTOMERS:\n------------------------------\nDevices left over 30 days without\nprior notice will be recycled.\nPlease collect repaired items on time.\nThank you for choosing us!"
            )
        }

        var dialog: AlertDialog? = null

        fun getReceiptData(): Pair<Bitmap, String> {
            val title = binding.etCustomPrintTitle.text?.toString().orEmpty().ifBlank { "CUSTOM RECEIPT" }
            val content = binding.etCustomPrintContent.text?.toString().orEmpty().ifBlank { "No content" }
            val bitmap = ThermalReceiptManager.generateCustomReceiptBitmap(ctx, title, content, currentWidthMm)
            val text = ThermalReceiptManager.generateCustomReceiptText(ctx, title, content)
            return Pair(bitmap, text)
        }

        binding.btnCustomDirectPrint.setOnClickListener {
            val (_, text) = getReceiptData()
            val savedAddress = AppPreferences.getBluetoothPrinterAddress(ctx)
            if (savedAddress.isEmpty()) {
                BluetoothPrinterManager.showPrinterSelectionDialog(ctx) { selectedAddress ->
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        BluetoothPrinterManager.connectAndPrint(ctx, selectedAddress, text)
                    }
                }
            } else {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    BluetoothPrinterManager.connectAndPrint(ctx, savedAddress, text)
                }
            }
        }

        binding.btnCustomRawBt.setOnClickListener {
            val (bitmap, text) = getReceiptData()
            ThermalReceiptManager.sendToPrinterApp(ctx, bitmap, text)
        }

        binding.btnCustomShareWhatsApp.setOnClickListener {
            val (_, text) = getReceiptData()
            WhatsAppHelper.sendTextToWhatsApp(ctx, "", text)
        }

        binding.btnCustomDismiss.setOnClickListener {
            dialog?.dismiss()
        }

        dialog = MaterialAlertDialogBuilder(ctx)
            .setView(binding.root)
            .create()
        dialog.show()
    }
}
