package com.shopkeeper.mobileshop.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Product
import com.shopkeeper.mobileshop.data.db.entity.ProductCategory
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogProductBinding
import com.shopkeeper.mobileshop.databinding.FragmentInventoryBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: ProductAdapter
    private var fullList: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        setupRecyclerView()
        setupListeners()
        observeProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { showAddEditDialog(it) },
            onMoreClick = { showProductOptions(it) }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddProduct.setOnClickListener { showAddEditDialog(null) }

        binding.btnImportInventory.setOnClickListener {
            com.shopkeeper.mobileshop.utils.ImportManager.showImportDialog(requireContext(), repository, defaultTypeIsProducts = true) {}
        }

        binding.btnExportInventory.setOnClickListener {
            if (fullList.isEmpty()) {
                Toast.makeText(requireContext(), "No products in inventory to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ExportManager.showLedgerExportDialog(
                context = requireContext(),
                ledgerTitle = "Inventory & Stock Ledger",
                onExportCsv = { ExportManager.exportProductsCsv(requireContext(), fullList) },
                onExportPdf = { ExportManager.exportProductsPdf(requireContext(), fullList) }
            )
        }

        binding.etSearch.doAfterTextChanged { text ->
            filterList(text?.toString().orEmpty())
        }

        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            filterList(binding.etSearch.text?.toString().orEmpty())
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.allProducts.collectLatest { list ->
                fullList = list
                filterList(binding.etSearch.text?.toString().orEmpty())
            }
        }
    }

    private fun filterList(query: String) {
        var filtered = if (query.isBlank()) fullList else {
            fullList.filter {
                it.name.contains(query, true) ||
                it.brand.contains(query, true) ||
                it.imei.contains(query, true)
            }
        }

        when (binding.chipGroupCategory.checkedChipId) {
            R.id.chipInStock -> filtered = filtered.filter { it.quantity > 0 }
            R.id.chipLowStock -> filtered = filtered.filter { it.quantity in 1..3 }
            R.id.chipOutOfStock -> filtered = filtered.filter { it.quantity <= 0 }
            R.id.chipSmartphones -> filtered = filtered.filter { it.category == ProductCategory.SMARTPHONE }
            R.id.chipAccessories -> filtered = filtered.filter { it.category != ProductCategory.SMARTPHONE }
        }

        adapter.submitList(filtered)
        binding.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showAddEditDialog(product: Product?) {
        val dialogBinding = DialogProductBinding.inflate(layoutInflater)
        val categories = ProductCategory.values().map { it.name.replace('_', ' ') }
        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.actvCategory.setAdapter(catAdapter)

        if (product != null) {
            dialogBinding.etName.setText(product.name)
            dialogBinding.etBrand.setText(product.brand)
            dialogBinding.etModel.setText(product.model)
            dialogBinding.etImei.setText(product.imei)
            dialogBinding.actvCategory.setText(product.category.name.replace('_', ' '), false)
            dialogBinding.etPurchasePrice.setText(product.purchasePrice.toString())
            dialogBinding.etSellingPrice.setText(product.sellingPrice.toString())
            dialogBinding.etQuantity.setText(product.quantity.toString())
            dialogBinding.etRam.setText(product.ram)
            dialogBinding.etStorage.setText(product.storage)
            dialogBinding.etColor.setText(product.color)
            if (product.warrantyMonths > 0) dialogBinding.etWarranty.setText(product.warrantyMonths.toString())
            dialogBinding.etCustomNotes.setText(product.notes)
        } else {
            dialogBinding.actvCategory.setText(ProductCategory.SMARTPHONE.name.replace('_', ' '), false)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (product == null) "Add Product" else "Edit Product")
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etName.text.toString().trim()
            val brand = dialogBinding.etBrand.text.toString().trim()
            val model = dialogBinding.etModel.text.toString().trim()
            val imei = dialogBinding.etImei.text.toString().trim()
            val buyPrice = dialogBinding.etPurchasePrice.text.toString().toDoubleOrNull() ?: 0.0
            val sellPrice = dialogBinding.etSellingPrice.text.toString().toDoubleOrNull() ?: 0.0
            val qty = dialogBinding.etQuantity.text.toString().toIntOrNull() ?: 0
            val ram = dialogBinding.etRam.text.toString().trim()
            val storage = dialogBinding.etStorage.text.toString().trim()
            val color = dialogBinding.etColor.text.toString().trim()
            val warranty = dialogBinding.etWarranty.text.toString().toIntOrNull() ?: 0
            val notes = dialogBinding.etCustomNotes.text.toString().trim()

            if (name.isEmpty() || brand.isEmpty()) {
                Toast.makeText(requireContext(), "Name & Brand are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catName = dialogBinding.actvCategory.text.toString().replace(' ', '_')
            val category = runCatching { ProductCategory.valueOf(catName) }.getOrDefault(ProductCategory.OTHER)

            val toSave = (product ?: Product(
                name = name, brand = brand, model = model,
                category = category, purchasePrice = buyPrice,
                sellingPrice = sellPrice, quantity = qty, imei = imei,
                ram = ram, storage = storage, color = color,
                warrantyMonths = warranty, notes = notes
            )).copy(
                name = name, brand = brand, model = model,
                category = category, purchasePrice = buyPrice,
                sellingPrice = sellPrice, quantity = qty, imei = imei,
                ram = ram, storage = storage, color = color,
                warrantyMonths = warranty, notes = notes,
                updatedAt = System.currentTimeMillis()
            )

            viewLifecycleOwner.lifecycleScope.launch {
                if (product == null) repository.insertProduct(toSave)
                else repository.updateProduct(toSave)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showProductOptions(product: Product) {
        val options = mutableListOf("🛒 Sell at POS", "✏️ Edit Details", "📦 Adjust Stock Quantity")
        if (product.imei.isNotBlank()) {
            options.add("🔍 View IMEI (${product.imei})")
        }
        options.add("🗑️ Delete Product")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(product.name)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "🛒 Sell at POS" -> {
                        findNavController().navigate(R.id.navigation_new_sale)
                    }
                    "✏️ Edit Details" -> showAddEditDialog(product)
                    "📦 Adjust Stock Quantity" -> showAdjustStockDialog(product)
                    "🗑️ Delete Product" -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Delete Product?")
                            .setMessage("Are you sure you want to delete ${product.name}?")
                            .setPositiveButton("Delete") { _, _ ->
                                viewLifecycleOwner.lifecycleScope.launch {
                                    repository.deleteProduct(product)
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                    else -> {
                        findNavController().navigate(R.id.navigation_imei)
                    }
                }
            }
            .show()
    }

    private fun showAdjustStockDialog(product: Product) {
        val input = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(product.quantity.toString())
            setSelection(text.length)
        }
        val container = android.widget.FrameLayout(requireContext()).apply {
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, 0)
            addView(input)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Adjust Stock: ${product.name}")
            .setMessage("Current stock: ${product.quantity} units. Enter new quantity:")
            .setView(container)
            .setPositiveButton("Update") { _, _ ->
                val newQty = input.text.toString().toIntOrNull()
                if (newQty != null && newQty >= 0) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.updateProduct(product.copy(quantity = newQty, updatedAt = System.currentTimeMillis()))
                        Toast.makeText(requireContext(), "Stock updated to $newQty units", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
