package com.shopkeeper.mobileshop.ui.purchases

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogNewPurchaseBinding
import com.shopkeeper.mobileshop.databinding.DialogPurchaseDetailsBinding
import com.shopkeeper.mobileshop.databinding.DialogSupplierBinding
import com.shopkeeper.mobileshop.databinding.DialogSupplierDetailsBinding
import com.shopkeeper.mobileshop.databinding.FragmentPurchasesBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import com.shopkeeper.mobileshop.utils.money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PurchasesFragment : Fragment() {

    private var _binding: FragmentPurchasesBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var purchaseAdapter: PurchaseAdapter
    private lateinit var supplierAdapter: SupplierAdapter

    private var allPurchasesList: List<Purchase> = emptyList()
    private var allSuppliersList: List<Supplier> = emptyList()

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

        purchaseAdapter = PurchaseAdapter { purchase ->
            showPurchaseDetailsDialog(purchase)
        }
        supplierAdapter = SupplierAdapter { supplier ->
            showSupplierDetailsDialog(supplier)
        }

        binding.rvPurchases.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPurchases.adapter = purchaseAdapter

        binding.rvSuppliers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuppliers.adapter = supplierAdapter

        binding.chipGroupTab.setOnCheckedStateChangeListener { _, _ ->
            val isPurchases = binding.chipPurchases.isChecked
            binding.rvPurchases.visibility = if (isPurchases) View.VISIBLE else View.GONE
            binding.rvSuppliers.visibility = if (isPurchases) View.GONE else View.VISIBLE
            binding.tilSearchPurchases.hint = if (isPurchases) "Search purchase order # or supplier" else "Search supplier name or company"
            filterCurrentTab()
        }

        binding.etSearchPurchases.doAfterTextChanged {
            filterCurrentTab()
        }

        binding.btnExportPurchases.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val list = allPurchasesList
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
            repository.allPurchases.collectLatest { list ->
                allPurchasesList = list
                updatePurchasesSummary(list)
                filterCurrentTab()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allSuppliers.collectLatest { list ->
                allSuppliersList = list
                filterCurrentTab()
            }
        }
    }

    private fun updatePurchasesSummary(list: List<Purchase>) {
        val totalVolume = list.sumOf { it.totalCost }
        val totalPaid = list.sumOf { it.paidAmount }
        val totalPayable = (totalVolume - totalPaid).coerceAtLeast(0.0)
        _binding?.tvPurchasesSummary?.text =
            "Total Purchases: ${totalVolume.money()} • Paid: ${totalPaid.money()} • Due: ${totalPayable.money()} (${list.size} orders)"
    }

    private fun filterCurrentTab() {
        val query = _binding?.etSearchPurchases?.text?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""
        val isPurchases = _binding?.chipPurchases?.isChecked == true

        if (isPurchases) {
            val filtered = if (query.isEmpty()) {
                allPurchasesList
            } else {
                allPurchasesList.filter {
                    it.supplierName.lowercase(Locale.getDefault()).contains(query) ||
                    it.id.toString().contains(query) ||
                    it.notes.lowercase(Locale.getDefault()).contains(query)
                }
            }
            purchaseAdapter.submitList(filtered)
            _binding?.layoutEmptyPurchases?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            _binding?.rvPurchases?.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
            _binding?.layoutEmptySuppliers?.visibility = View.GONE
        } else {
            val filtered = if (query.isEmpty()) {
                allSuppliersList
            } else {
                allSuppliersList.filter {
                    it.name.lowercase(Locale.getDefault()).contains(query) ||
                    it.company.lowercase(Locale.getDefault()).contains(query) ||
                    it.phone.contains(query)
                }
            }
            supplierAdapter.submitList(filtered)
            _binding?.layoutEmptySuppliers?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            _binding?.rvSuppliers?.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
            _binding?.layoutEmptyPurchases?.visibility = View.GONE
        }
    }

    private fun showPurchaseDetailsDialog(purchase: Purchase) {
        viewLifecycleOwner.lifecycleScope.launch {
            var curPurchase = purchase
            val items = repository.getPurchaseItems(curPurchase.id)

            val dBinding = DialogPurchaseDetailsBinding.inflate(layoutInflater)
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dBinding.root)
                .create()

            fun renderPurchaseState() {
                dBinding.tvPurchaseTitle.text = "Purchase Order ${curPurchase.orderNumber}"
                val dateFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                dBinding.tvPurchaseDate.text = dateFmt.format(Date(curPurchase.purchaseDate))
                dBinding.tvPurchaseSupplier.text = curPurchase.supplierName.ifEmpty { "Cash Supplier" }
                dBinding.tvPurchaseNotes.text = if (curPurchase.notes.isNotBlank()) curPurchase.notes else "Restock Purchase"

                dBinding.tvPurchaseTotal.text = curPurchase.totalCost.money()
                dBinding.tvPurchasePaid.text = curPurchase.paidAmount.money()
                val balanceDue = (curPurchase.totalCost - curPurchase.paidAmount).coerceAtLeast(0.0)
                dBinding.tvPurchaseBalance.text = balanceDue.money()

                dBinding.tvPurchaseStatusBadge.text = curPurchase.paymentStatus.name
                if (curPurchase.paymentStatus == PaymentStatus.PAID) {
                    dBinding.tvPurchaseStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
                    dBinding.tvPurchaseStatusBadge.setTextColor(Color.parseColor("#166534"))
                    dBinding.btnPaySupplierDue.visibility = View.GONE
                } else {
                    dBinding.tvPurchaseStatusBadge.setBackgroundResource(R.drawable.bg_badge_red)
                    dBinding.tvPurchaseStatusBadge.setTextColor(Color.parseColor("#991B1B"))
                    dBinding.btnPaySupplierDue.visibility = View.VISIBLE
                    dBinding.btnPaySupplierDue.text = "💰 Pay Remaining Balance (${balanceDue.money()})"
                }

                dBinding.layoutPurchaseItems.removeAllViews()
                if (items.isEmpty()) {
                    val noItemsTv = TextView(requireContext()).apply {
                        text = "No line items logged."
                        textSize = 12f
                        setTextColor(Color.parseColor("#64748B"))
                    }
                    dBinding.layoutPurchaseItems.addView(noItemsTv)
                } else {
                    items.forEach { item ->
                        val itemRow = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(0, 10, 0, 10)
                        }
                        val top = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.HORIZONTAL
                        }
                        val name = TextView(requireContext()).apply {
                            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                            text = "${item.productName} × ${item.quantity}"
                            textSize = 13.5f
                            setTypeface(null, Typeface.BOLD)
                            setTextColor(Color.parseColor("#1E293B"))
                        }
                        val price = TextView(requireContext()).apply {
                            text = (item.unitCost * item.quantity).money()
                            textSize = 13.5f
                            setTypeface(null, Typeface.BOLD)
                            setTextColor(Color.parseColor("#0F172A"))
                        }
                        top.addView(name)
                        top.addView(price)
                        itemRow.addView(top)

                        val detailLine = TextView(requireContext()).apply {
                            val imeiPart = if (item.imei.isNotBlank()) " • IMEI: ${item.imei}" else ""
                            text = "Unit Cost: ${item.unitCost.money()}$imeiPart"
                            textSize = 11.5f
                            setTextColor(Color.parseColor("#64748B"))
                            setPadding(0, 2, 0, 0)
                        }
                        itemRow.addView(detailLine)

                        dBinding.layoutPurchaseItems.addView(itemRow)
                    }
                }
            }

            renderPurchaseState()

            dBinding.btnPaySupplierDue.setOnClickListener {
                val balanceDue = (curPurchase.totalCost - curPurchase.paidAmount).coerceAtLeast(0.0)
                val input = EditText(requireContext()).apply {
                    setText(balanceDue.toString())
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    setPadding(50, 40, 50, 40)
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pay Supplier")
                    .setMessage("Enter amount paid to ${curPurchase.supplierName}:")
                    .setView(input)
                    .setPositiveButton("Record Payment") { _, _ ->
                        val amt = input.text.toString().toDoubleOrNull() ?: 0.0
                        if (amt <= 0.0) {
                            Toast.makeText(requireContext(), "Invalid payment amount", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        val newPaid = (curPurchase.paidAmount + amt).coerceAtMost(curPurchase.totalCost)
                        val newStatus = if (newPaid >= curPurchase.totalCost) PaymentStatus.PAID else PaymentStatus.PARTIAL
                        val updated = curPurchase.copy(paidAmount = newPaid, paymentStatus = newStatus)

                        viewLifecycleOwner.lifecycleScope.launch {
                            repository.updatePurchase(updated)
                            curPurchase = updated
                            renderPurchaseState()
                            Toast.makeText(requireContext(), "Supplier payment recorded!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            dBinding.btnSharePurchase.setOnClickListener {
                val summary = buildString {
                    appendLine("📦 PURCHASE ORDER SUMMARY ${curPurchase.orderNumber}")
                    appendLine("Supplier: ${curPurchase.supplierName}")
                    appendLine("Total: ${curPurchase.totalCost.money()}")
                    appendLine("Paid: ${curPurchase.paidAmount.money()}")
                    appendLine("Status: ${curPurchase.paymentStatus}")
                    if (items.isNotEmpty()) {
                        appendLine("\nItems:")
                        items.forEach { appendLine("• ${it.productName} (${it.quantity}x @ ${it.unitCost.money()})") }
                    }
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, summary)
                }
                startActivity(Intent.createChooser(intent, "Share Purchase Summary"))
            }

            dBinding.btnClosePurchaseDialog.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun showSupplierDetailsDialog(supplier: Supplier) {
        viewLifecycleOwner.lifecycleScope.launch {
            val dBinding = DialogSupplierDetailsBinding.inflate(layoutInflater)
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dBinding.root)
                .create()

            dBinding.tvSupplierDetailInitials.text = supplier.name.take(1).uppercase(Locale.getDefault())
            dBinding.tvSupplierDetailName.text = supplier.name
            dBinding.tvSupplierDetailCompany.text = supplier.company.ifEmpty { "Wholesaler / Distributor" }
            dBinding.tvSupplierDetailPhone.text = supplier.phone.ifEmpty { "No phone recorded" }

            val supplierPurchases = allPurchasesList.filter {
                it.supplierId == supplier.id || it.supplierName.equals(supplier.name, ignoreCase = true)
            }
            val totalPurchased = supplierPurchases.sumOf { it.totalCost }
            val totalPaid = supplierPurchases.sumOf { it.paidAmount }
            val pendingPayable = (totalPurchased - totalPaid).coerceAtLeast(0.0)

            dBinding.tvSupplierTotalPurchased.text = totalPurchased.money()
            dBinding.tvSupplierPendingPayable.text = pendingPayable.money()
            dBinding.tvSupplierOrderCount.text = "${supplierPurchases.size} Purchase Orders recorded"

            dBinding.btnSupplierCall.setOnClickListener {
                if (supplier.phone.isNotBlank()) {
                    ExportManager.openDialer(requireContext(), supplier.phone)
                } else {
                    Toast.makeText(requireContext(), "No phone recorded for this supplier", Toast.LENGTH_SHORT).show()
                }
            }

            dBinding.btnSupplierWhatsApp.setOnClickListener {
                if (supplier.phone.isNotBlank()) {
                    val msg = "Assalam-o-Alaikum ${supplier.name}, contacting regarding mobile inventory orders."
                    ExportManager.shareWhatsApp(requireContext(), supplier.phone, msg)
                } else {
                    Toast.makeText(requireContext(), "No phone recorded for this supplier", Toast.LENGTH_SHORT).show()
                }
            }

            dBinding.btnBuyFromSupplier.setOnClickListener {
                dialog.dismiss()
                showAddPurchaseDialog(preSelectedSupplier = supplier.name)
            }

            dBinding.btnCloseSupplierDetails.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
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
                Toast.makeText(requireContext(), "Supplier added successfully!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showAddPurchaseDialog(preSelectedSupplier: String? = null) {
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

            if (preSelectedSupplier != null) {
                val idx = names.indexOfFirst { it.equals(preSelectedSupplier, ignoreCase = true) }
                if (idx != -1) dBinding.spSupplier.setSelection(idx)
            }

            dBinding.btnSavePurchase.setOnClickListener {
                val prodName = dBinding.etProductName.text.toString().trim()
                val brand = dBinding.etBrand.text.toString().trim().ifEmpty { "Generic" }
                val imei = dBinding.etImei.text.toString().trim()
                val qty = dBinding.etQty.text.toString().toIntOrNull() ?: 1
                val cost = dBinding.etUnitCost.text.toString().toDoubleOrNull() ?: 0.0
                val sell = dBinding.etSellPrice.text.toString().toDoubleOrNull() ?: cost

                if (prodName.isEmpty() || cost <= 0.0) {
                    Toast.makeText(requireContext(), "Product name and cost are required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val total = cost * qty
                val rawPaid = dBinding.etPaid.text.toString().toDoubleOrNull() ?: total

                if (rawPaid < 0.0 || rawPaid > total) {
                    Toast.makeText(requireContext(), "Paid amount cannot exceed total bill (${total.money()})", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val paid = rawPaid.coerceIn(0.0, total)

                val supName = dBinding.spSupplier.selectedItem?.toString() ?: "Cash Supplier"
                val matchedSup = suppliers.find { it.name == supName }
                val supplierId = matchedSup?.id ?: 1L

                val purchase = Purchase(
                    supplierId = supplierId,
                    supplierName = supName,
                    totalCost = total,
                    paidAmount = paid,
                    paymentStatus = if (paid >= total) PaymentStatus.PAID else if (paid > 0.0) PaymentStatus.PARTIAL else PaymentStatus.PENDING
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
