package com.shopkeeper.mobileshop.ui.purchases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogNewPurchaseBinding
import com.shopkeeper.mobileshop.databinding.DialogSupplierBinding
import com.shopkeeper.mobileshop.databinding.FragmentPurchasesBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PurchasesFragment : Fragment() {

    private var _binding: FragmentPurchasesBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var purchaseAdapter: PurchaseAdapter
    private lateinit var supplierAdapter: SupplierAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchasesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        purchaseAdapter = PurchaseAdapter {}
        supplierAdapter = SupplierAdapter {}

        binding.rvPurchases.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPurchases.adapter = purchaseAdapter

        binding.rvSuppliers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuppliers.adapter = supplierAdapter

        binding.chipGroupTab.setOnCheckedStateChangeListener { _, checkedIds ->
            val isPurchases = binding.chipPurchases.isChecked
            binding.rvPurchases.visibility = if (isPurchases) View.VISIBLE else View.GONE
            binding.rvSuppliers.visibility = if (isPurchases) View.GONE else View.VISIBLE
        }

        binding.btnExportPurchases.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val list = repository.allPurchases.first()
                if (list.isEmpty()) {
                    Toast.makeText(requireContext(), "No purchase orders to export", Toast.LENGTH_SHORT).show()
                } else {
                    ExportManager.showLedgerExportDialog(
                        context = requireContext(),
                        ledgerTitle = "Daily Purchases Ledger",
                        onExportCsv = { ExportManager.exportPurchasesCsv(requireContext(), list) },
                        onExportPdf = { ExportManager.exportPurchasesPdf(requireContext(), list) }
                    )
                }
            }
        }

        binding.fabAdd.setOnClickListener {
            if (binding.chipPurchases.isChecked) showAddPurchaseDialog()
            else showAddSupplierDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allPurchases.collectLatest { purchaseAdapter.submitList(it) }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repository.allSuppliers.collectLatest { supplierAdapter.submitList(it) }
        }
    }

    private fun showAddSupplierDialog() {
        val dBinding = DialogSupplierBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Supplier")
            .setView(dBinding.root)
            .create()

        dBinding.btnSaveSupplier.setOnClickListener {
            val name = dBinding.etSupName.text.toString().trim()
            val phone = dBinding.etSupPhone.text.toString().trim()
            val comp = dBinding.etSupCompany.text.toString().trim()
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Name & Phone required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                repository.insertSupplier(Supplier(name = name, phone = phone, company = comp))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showAddPurchaseDialog() {
        val dBinding = DialogNewPurchaseBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Purchase / Buy Stock")
            .setView(dBinding.root)
            .create()

        viewLifecycleOwner.lifecycleScope.launch {
            val suppliers = repository.allSuppliers.first()
            val names = suppliers.map { it.name } + listOf("+ Add New Supplier")
            val spAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
            dBinding.spSupplier.adapter = spAdapter

            dBinding.btnSavePurchase.setOnClickListener {
                val prodName = dBinding.etProductName.text.toString().trim()
                val brand = dBinding.etBrand.text.toString().trim().ifEmpty { "Generic" }
                val imei = dBinding.etImei.text.toString().trim()
                val qty = dBinding.etQty.text.toString().toIntOrNull() ?: 1
                val cost = dBinding.etUnitCost.text.toString().toDoubleOrNull() ?: 0.0
                val sell = dBinding.etSellPrice.text.toString().toDoubleOrNull() ?: cost
                val paid = dBinding.etPaid.text.toString().toDoubleOrNull() ?: (cost * qty)

                if (prodName.isEmpty() || cost <= 0.0) {
                    Toast.makeText(requireContext(), "Product name and cost are required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val total = cost * qty
                val supName = dBinding.spSupplier.selectedItem?.toString() ?: "Cash Supplier"

                val purchase = Purchase(
                    supplierId = 1,
                    supplierName = supName,
                    totalCost = total,
                    paidAmount = paid,
                    paymentStatus = if (paid >= total) PaymentStatus.PAID else PaymentStatus.PARTIAL
                )

                val item = PurchaseItem(
                    purchaseId = 0,
                    productName = prodName,
                    supplierName = supName,
                    imei = imei,
                    quantity = qty,
                    unitCost = cost
                )

                val newProd = Product(
                    name = prodName,
                    brand = brand,
                    model = "",
                    imei = imei,
                    category = ProductCategory.SMARTPHONE,
                    purchasePrice = cost,
                    sellingPrice = sell,
                    quantity = qty
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    repository.insertPurchase(purchase, listOf(item))
                    repository.insertProduct(newProd)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Purchase recorded and stock added!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
