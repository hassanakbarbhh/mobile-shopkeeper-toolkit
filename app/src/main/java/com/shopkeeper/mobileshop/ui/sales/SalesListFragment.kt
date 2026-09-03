package com.shopkeeper.mobileshop.ui.sales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.FragmentSalesListBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import com.shopkeeper.mobileshop.utils.InvoiceGenerator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SalesListFragment : Fragment() {

    private var _binding: FragmentSalesListBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: SaleAdapter
    private var currentSales: List<Sale> = emptyList()

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
            if (currentSales.isEmpty()) {
                Toast.makeText(requireContext(), "No sales to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ExportManager.showLedgerExportDialog(
                context = requireContext(),
                ledgerTitle = "Sales & Invoices Ledger",
                onExportCsv = { ExportManager.exportSalesCsv(requireContext(), currentSales) },
                onExportPdf = { ExportManager.exportSalesPdf(requireContext(), currentSales) }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allSales.collectLatest { list ->
                currentSales = list
                adapter.submitList(list)
                binding.tvSalesCount.text = "${list.size} invoices recorded"
            }
        }
    }

    private fun showSaleDetails(sale: Sale) {
        viewLifecycleOwner.lifecycleScope.launch {
            val items = repository.getSaleItems(sale.id)
            val itemsText = items.joinToString("\n") {
                "• ${it.productName} x${it.quantity} = Rs.${it.totalPrice}" +
                    if (it.imei.isNotBlank()) " (IMEI: ${it.imei})" else ""
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Invoice #${sale.id} • ${sale.customerName}")
                .setMessage("Date: ${sale.saleDate}\nSold by: ${sale.sellerName}\nTotal: Rs.${sale.finalAmount}\nPayment: ${sale.paymentMethod} (${sale.paymentStatus})\n\nItems:\n$itemsText")
                .setPositiveButton("🖨️ Thermal Receipt") { _, _ ->
                    com.shopkeeper.mobileshop.utils.ThermalPrintHelper.showSaleReceiptDialog(requireContext(), sale, items)
                }
                .setNeutralButton("PDF Invoice") { _, _ ->
                    val file = InvoiceGenerator.generate(requireContext(), sale, items)
                    val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
                }
                .setNegativeButton("Close", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
