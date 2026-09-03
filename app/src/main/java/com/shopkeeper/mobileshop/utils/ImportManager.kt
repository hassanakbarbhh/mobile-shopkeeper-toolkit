package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.entity.Customer
import com.shopkeeper.mobileshop.data.db.entity.Product
import com.shopkeeper.mobileshop.data.db.entity.ProductCategory
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogImportDataBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ImportManager {

    val SAMPLE_PRODUCTS_CSV = """
        Name,Brand,Category,Quantity,CostPrice,SellingPrice,IMEI,Storage,RAM,Color
        iPhone 15 Pro,Apple,SMARTPHONE,5,280000,310000,359123456789012,256GB,8GB,Titanium
        Samsung Galaxy S24,Samsung,SMARTPHONE,8,220000,245000,358765432109876,128GB,8GB,Onyx Black
        Redmi Note 13,Xiaomi,SMARTPHONE,12,52000,58000,351234567890123,128GB,6GB,Ocean Blue
        20W USB-C Fast Charger,Apple,ACCESSORY,25,2200,3500,,,,,White
        67W GaN Super Fast Charger,Xiaomi,CHARGER,15,1800,2800,,,,,White
        AirPods Pro 2 ANC,Apple,ACCESSORY,6,48000,56000,,,,,White
        Privacy Glass Protector 15 Pro,Baseus,PROTECTOR,50,300,800,,,,,Clear
    """.trimIndent()

    val SAMPLE_CUSTOMERS_CSV = """
        Name,Phone,Email,Address,Notes
        Muhammad Ali,+923001122334,ali@example.com,Shop 14 Main Saddar,Regular wholesale customer
        Hamza Farooq,+923214455667,hamza@example.com,DHA Phase 5 Lahore,Prefers Apple flagship devices
        Zeeshan Khan,+923337788990,zeeshan@example.com,Blue Area Islamabad,Prompt payer
    """.trimIndent()

    suspend fun importProducts(csvContent: String, repository: ShopRepository): Int = withContext(Dispatchers.IO) {
        var count = 0
        val lines = csvContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return@withContext 0

        // Check if first line is header
        val startIdx = if (lines[0].startsWith("name", ignoreCase = true) || lines[0].contains("brand", ignoreCase = true)) 1 else 0

        for (i in startIdx until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.isEmpty() || cols[0].isBlank()) continue

            val name = cols.getOrNull(0) ?: ""
            val brand = cols.getOrNull(1) ?: "Generic"
            val categoryStr = cols.getOrNull(2) ?: "SMARTPHONE"
            val category = runCatching {
                ProductCategory.valueOf(categoryStr.uppercase().replace(" ", "_"))
            }.getOrDefault(ProductCategory.SMARTPHONE)

            val qty = cols.getOrNull(3)?.toIntOrNull() ?: 1
            val costPrice = cols.getOrNull(4)?.toDoubleOrNull() ?: 0.0
            val sellingPrice = cols.getOrNull(5)?.toDoubleOrNull() ?: 0.0
            val imei = cols.getOrNull(6) ?: ""
            val storage = cols.getOrNull(7) ?: ""
            val ram = cols.getOrNull(8) ?: ""
            val color = cols.getOrNull(9) ?: ""

            val product = Product(
                name = name,
                brand = brand,
                model = brand,
                category = category,
                quantity = qty,
                purchasePrice = costPrice,
                sellingPrice = sellingPrice,
                imei = imei,
                storage = storage,
                ram = ram,
                color = color
            )
            repository.insertProduct(product)
            count++
        }
        count
    }

    suspend fun importCustomers(csvContent: String, repository: ShopRepository): Int = withContext(Dispatchers.IO) {
        var count = 0
        val lines = csvContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return@withContext 0

        val startIdx = if (lines[0].startsWith("name", ignoreCase = true)) 1 else 0

        for (i in startIdx until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.isEmpty() || cols[0].isBlank()) continue

            val name = cols.getOrNull(0) ?: ""
            val phone = cols.getOrNull(1) ?: ""
            val email = cols.getOrNull(2) ?: ""
            val address = cols.getOrNull(3) ?: ""
            val notes = cols.getOrNull(4) ?: ""

            val customer = Customer(
                name = name,
                phone = phone,
                email = email,
                address = address,
                notes = notes
            )
            repository.insertCustomer(customer)
            count++
        }
        count
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var sb = StringBuilder()
        var inQuotes = false

        for (c in line) {
            if (c == '\"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb = StringBuilder()
            } else {
                sb.append(c)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    fun showImportDialog(
        ctx: Context,
        repository: ShopRepository,
        defaultTypeIsProducts: Boolean = true,
        onSuccess: () -> Unit
    ) {
        val binding = DialogImportDataBinding.inflate(LayoutInflater.from(ctx))
        var isProductMode = defaultTypeIsProducts

        fun updateUiForType() {
            if (isProductMode) {
                binding.toggleImportType.check(R.id.btnTypeProducts)
                binding.tvFormatHint.text = "Format: Name, Brand, Category, Qty, Cost, Price, IMEI, Storage, RAM, Color"
                binding.etCsvInput.hint = "Paste Products CSV here or click Load Sample"
            } else {
                binding.toggleImportType.check(R.id.btnTypeCustomers)
                binding.tvFormatHint.text = "Format: Name, Phone, Email, Address, Notes"
                binding.etCsvInput.hint = "Paste Customers CSV here or click Load Sample"
            }
        }

        updateUiForType()

        binding.toggleImportType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isProductMode = checkedId == R.id.btnTypeProducts
                updateUiForType()
            }
        }

        binding.btnLoadSample.setOnClickListener {
            binding.etCsvInput.setText(if (isProductMode) SAMPLE_PRODUCTS_CSV else SAMPLE_CUSTOMERS_CSV)
        }

        var dialog: AlertDialog? = null

        binding.btnCancelImport.setOnClickListener { dialog?.dismiss() }

        binding.btnExecuteImport.setOnClickListener {
            val text = binding.etCsvInput.text?.toString().orEmpty().trim()
            if (text.isEmpty()) {
                Toast.makeText(ctx, "Please enter or paste CSV content first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnExecuteImport.isEnabled = false
            binding.btnExecuteImport.text = "Importing…"

            CoroutineScope(Dispatchers.Main).launch {
                runCatching {
                    val count = if (isProductMode) {
                        importProducts(text, repository)
                    } else {
                        importCustomers(text, repository)
                    }
                    Toast.makeText(ctx, "Successfully imported $count ${if (isProductMode) "products" else "customers"}!", Toast.LENGTH_LONG).show()
                    dialog?.dismiss()
                    onSuccess()
                }.onFailure {
                    Toast.makeText(ctx, "Import error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    binding.btnExecuteImport.isEnabled = true
                    binding.btnExecuteImport.text = "Import Now"
                }
            }
        }

        dialog = MaterialAlertDialogBuilder(ctx)
            .setView(binding.root)
            .create()
        dialog.show()
    }
}
