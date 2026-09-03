package com.shopkeeper.mobileshop.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.data.catalog.OnlineCatalogRepository
import com.shopkeeper.mobileshop.data.catalog.OnlinePhoneModel
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Product
import com.shopkeeper.mobileshop.data.db.entity.ProductCategory
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogProductBinding
import com.shopkeeper.mobileshop.databinding.FragmentOnlineCatalogBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import kotlinx.coroutines.launch

class OnlineCatalogFragment : Fragment() {

    private var _binding: FragmentOnlineCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: OnlineCatalogAdapter
    private var selectedBrand: String = "All"
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnlineCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        adapter = OnlineCatalogAdapter(
            onAddToStockClick = { model -> showAddToStockDialog(model) },
            onShareSpecsClick = { model ->
                ExportManager.shareWhatsApp(
                    requireContext(),
                    "",
                    model.toShareableSpecs()
                )
            }
        )

        binding.rvOnlineModels.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOnlineModels.adapter = adapter

        setupBrandChips()

        binding.etCatalogSearch.doAfterTextChanged { text ->
            searchQuery = text?.toString().orEmpty()
            updateList()
        }

        updateList()
    }

    private fun setupBrandChips() {
        binding.chipGroupBrands.removeAllViews()
        val brands = OnlineCatalogRepository.getBrands()

        brands.forEachIndexed { index, brand ->
            val chip = Chip(requireContext()).apply {
                text = brand
                isCheckable = true
                isChecked = index == 0
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedBrand = brand
                        updateList()
                    }
                }
            }
            binding.chipGroupBrands.addView(chip)
        }
    }

    private fun updateList() {
        val filtered = OnlineCatalogRepository.filter(selectedBrand, searchQuery)
        adapter.submitList(filtered)
        binding.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.tvCatalogCount.text = "${filtered.size} phone models available online (0 local stock • no price set yet). Tap 'Add to Shop Stock' to import into your inventory."
    }

    private fun showAddToStockDialog(model: OnlinePhoneModel) {
        val dialogBinding = DialogProductBinding.inflate(layoutInflater)
        val categories = ProductCategory.values().map { it.name.replace('_', ' ') }
        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.actvCategory.setAdapter(catAdapter)

        // Pre-fill model details from the online catalog
        dialogBinding.etName.setText(model.fullName)
        dialogBinding.etBrand.setText(model.brand)
        dialogBinding.etModel.setText(model.modelName)
        dialogBinding.actvCategory.setText(ProductCategory.SMARTPHONE.name.replace('_', ' '), false)
        dialogBinding.etRam.setText(model.ramOptions.substringBefore('/'))
        dialogBinding.etStorage.setText(model.storageOptions.substringBefore('/'))
        dialogBinding.etQuantity.setText("1")
        dialogBinding.etCustomNotes.setText("Imported from Online Catalog: ${model.specs}")

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add '${model.fullName}' to Stock")
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etName.text.toString().trim()
            val brand = dialogBinding.etBrand.text.toString().trim()
            val modelName = dialogBinding.etModel.text.toString().trim()
            val imei = dialogBinding.etImei.text.toString().trim()
            val buyPrice = dialogBinding.etPurchasePrice.text.toString().toDoubleOrNull() ?: 0.0
            val sellPrice = dialogBinding.etSellingPrice.text.toString().toDoubleOrNull() ?: 0.0
            val qty = dialogBinding.etQuantity.text.toString().toIntOrNull() ?: 1
            val ram = dialogBinding.etRam.text.toString().trim()
            val storage = dialogBinding.etStorage.text.toString().trim()
            val color = dialogBinding.etColor.text.toString().trim()
            val warranty = dialogBinding.etWarranty.text.toString().toIntOrNull() ?: 12
            val notes = dialogBinding.etCustomNotes.text.toString().trim()

            if (name.isEmpty() || brand.isEmpty()) {
                Toast.makeText(requireContext(), "Name & Brand are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val product = Product(
                name = name,
                brand = brand,
                model = modelName,
                category = ProductCategory.SMARTPHONE,
                purchasePrice = buyPrice,
                sellingPrice = sellPrice,
                quantity = qty,
                imei = imei,
                ram = ram,
                storage = storage,
                color = color,
                warrantyMonths = warranty,
                notes = notes
            )

            viewLifecycleOwner.lifecycleScope.launch {
                repository.insertProduct(product)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Added '${product.name}' to shop inventory!", Toast.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
