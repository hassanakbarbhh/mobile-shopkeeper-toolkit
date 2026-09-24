package com.shopkeeper.mobileshop.ui.sales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.PaymentStatus
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogSaleDetailsBinding
import com.shopkeeper.mobileshop.databinding.FragmentSalesListBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import com.shopkeeper.mobileshop.utils.InvoiceGenerator
import com.shopkeeper.mobileshop.utils.money
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SalesListFragment : Fragment() {

    private var _binding: FragmentSalesListBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: SaleAdapter
    private var allSales: List<Sale> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        adapter = SaleAdapter { sale -> showSaleDetails(sale) }
        binding.rvSales.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSales.adapter = adapter

        binding.fabNewSale.setOnClickListener {
            findNavController().navigate(R.id.navigation_new_sale)
        }

        binding.btnExportSales.setOnClickListener {
            if (allSales.isEmpty()) {
                Toast.makeText(requireContext(), "No sales to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ExportManager.showLedgerExportDialog(
                context = requireContext(),
                ledgerTitle = "Sales & Invoices Ledger",
                onExportCsv = { ExportManager.exportSalesCsv(requireContext(), allSales) },
                onExportPdf = { ExportManager.exportSalesPdf(requireContext(), allSales) }
            )
        }

        binding.etSearchSales.doAfterTextChanged { filterSales() }
        binding.chipGroupSalesFilter.setOnCheckedStateChangeListener { _, _ -> filterSales() }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allSales.collectLatest { list ->
                allSales = list
                updateTodaySummary(list)
                filterSales()
            }
        }
    }

    private fun updateTodaySummary(list: List<Sale>) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val todaySales = list.filter { it.saleDate >= startOfDay }
        val todayTotal = todaySales.sumOf { it.finalAmount }
        _binding?.tvTodaySalesSummary?.text = "Today: ${todayTotal.money()} (${todaySales.size} invoices)"
    }

    private fun filterSales() {
        val query = _binding?.etSearchSales?.text?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""
        val checkedChipId = _binding?.chipGroupSalesFilter?.checkedChipId ?: R.id.chipAllSales

        var filtered = allSales

        // Status chip filter
        filtered = when (checkedChipId) {
            R.id.chipPaidSales -> filtered.filter { it.paymentStatus == PaymentStatus.PAID }
            R.id.chipCreditSales -> filtered.filter { it.paymentStatus != PaymentStatus.PAID }
            else -> filtered
        }

        // Query filter
        if (query.isNotEmpty()) {
            filtered = filtered.filter { sale ->
                sale.customerName.lowercase(Locale.getDefault()).contains(query) ||
                sale.invoiceNumber.lowercase(Locale.getDefault()).contains(query) ||
                sale.id.toString().contains(query)
            }
        }

        adapter.submitList(filtered)
        _binding?.layoutEmptySales?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        _binding?.rvSales?.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showSaleDetails(sale: Sale) {
        viewLifecycleOwner.lifecycleScope.launch {
            val items = repository.getSaleItems(sale.id)

            val dBinding = DialogSaleDetailsBinding.inflate(layoutInflater)
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dBinding.root)
                .create()

            dBinding.tvInvoiceTitle.text = "Invoice #${sale.invoiceNumber}"
            val dateFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            dBinding.tvInvoiceDate.text = dateFmt.format(Date(sale.saleDate))
            dBinding.tvInvoiceCustomer.text = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer"
            dBinding.tvInvoiceSeller.text = if (sale.sellerName.isNotBlank()) sale.sellerName else "Store Admin"
            dBinding.tvInvoicePaymentMethod.text = "${sale.paymentMethod} (${sale.paymentStatus})"
            dBinding.tvInvoiceStatusBadge.text = sale.paymentStatus.name

            if (sale.paymentStatus == PaymentStatus.PAID) {
                dBinding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
                dBinding.tvInvoiceStatusBadge.setTextColor(android.graphics.Color.parseColor("#166534"))
            } else {
                dBinding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_red)
                dBinding.tvInvoiceStatusBadge.setTextColor(android.graphics.Color.parseColor("#991B1B"))
            }

            dBinding.tvInvoiceSubtotal.text = sale.totalAmount.money()
            dBinding.tvInvoiceDiscount.text = "-${sale.discount.money()}"
            dBinding.tvInvoiceTotal.text = sale.finalAmount.money()

            dBinding.layoutInvoiceItems.removeAllViews()
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
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(android.graphics.Color.parseColor("#1E293B"))
                }
                val price = TextView(requireContext()).apply {
                    text = item.totalPrice.money()
                    textSize = 13.5f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(android.graphics.Color.parseColor("#0F172A"))
                }
                top.addView(name)
                top.addView(price)
                itemRow.addView(top)

                if (item.imei.isNotBlank()) {
                    val imeiTv = TextView(requireContext()).apply {
                        text = "IMEI: ${item.imei}"
                        textSize = 11.5f
                        setTextColor(android.graphics.Color.parseColor("#64748B"))
                        setPadding(0, 2, 0, 0)
                    }
                    itemRow.addView(imeiTv)
                }
                dBinding.layoutInvoiceItems.addView(itemRow)
            }

            dBinding.btnInvoiceThermal.setOnClickListener {
                com.shopkeeper.mobileshop.utils.ThermalPrintHelper.showSaleReceiptDialog(requireContext(), sale, items)
            }

            dBinding.btnInvoiceSharePdf.setOnClickListener {
                val file = InvoiceGenerator.generate(requireContext(), sale, items)
                val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
            }

            dBinding.btnInvoiceClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
