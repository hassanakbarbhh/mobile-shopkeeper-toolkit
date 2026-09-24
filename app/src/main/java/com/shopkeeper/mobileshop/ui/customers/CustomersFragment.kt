package com.shopkeeper.mobileshop.ui.customers

import android.os.Bundle
import com.shopkeeper.mobileshop.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Customer
import com.shopkeeper.mobileshop.data.db.entity.Payment
import com.shopkeeper.mobileshop.data.db.entity.PaymentMethod
import com.shopkeeper.mobileshop.data.db.entity.PaymentStatus
import com.shopkeeper.mobileshop.data.db.entity.PaymentType
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogCustomerBinding
import com.shopkeeper.mobileshop.databinding.DialogCustomerDetailsBinding
import com.shopkeeper.mobileshop.databinding.DialogReceivePaymentBinding
import com.shopkeeper.mobileshop.databinding.FragmentCustomersBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import com.shopkeeper.mobileshop.utils.ShopProfile
import com.shopkeeper.mobileshop.utils.money
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest

import com.shopkeeper.mobileshop.domain.UdhaarAgingAnalyzer
import kotlinx.coroutines.launch

class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: CustomerAdapter
    private var currentCustomers: List<Customer> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        adapter = CustomerAdapter { customer ->
            showCustomerDetailDialog(customer)
        }

        binding.rvCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomers.adapter = adapter

        binding.fabAddCustomer.setOnClickListener { showAddCustomerDialog(null) }

        binding.btnImportCustomers.setOnClickListener {
            com.shopkeeper.mobileshop.utils.ImportManager.showImportDialog(requireContext(), repository, defaultTypeIsProducts = false) {}
        }

        binding.btnExportCustomers.setOnClickListener {
            if (currentCustomers.isEmpty()) {
                Toast.makeText(requireContext(), "No customer records to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ExportManager.showLedgerExportDialog(
                context = requireContext(),
                ledgerTitle = "Customers Ledger",
                onExportCsv = { ExportManager.exportCustomersCsv(requireContext(), currentCustomers) },
                onExportPdf = { ExportManager.exportCustomersPdf(requireContext(), currentCustomers) }
            )
        }

        binding.etCustomerSearch.doAfterTextChanged { text ->
            val q = text?.toString().orEmpty()
            viewLifecycleOwner.lifecycleScope.launch {
                repository.searchCustomers(q).collectLatest { list ->
                    currentCustomers = list
                    adapter.submitList(list)
                    binding.layoutEmptyCustomers.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvCustomers.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allCustomers.collectLatest { list ->
                currentCustomers = list
                adapter.submitList(list)
                binding.layoutEmptyCustomers.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvCustomers.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showCustomerDetailDialog(customer: Customer) {
        val dBinding = DialogCustomerDetailsBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dBinding.root)
            .create()

        dBinding.tvDetailName.text = customer.name
        dBinding.tvDetailPhone.text = if (customer.phone.isNotBlank()) customer.phone else "No phone provided"
        dBinding.tvDetailInitials.text = customer.name.take(1).uppercase()

        val db = AppDatabase.getDatabase(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            val allSales = db.saleDao().getAllSalesList()
            val customerSales = allSales.filter { it.customerId == customer.id || it.customerName.equals(customer.name, ignoreCase = true) }
            val pendingSales = customerSales.filter { it.paymentStatus != PaymentStatus.PAID }
            val totalDue = pendingSales.sumOf { it.finalAmount }
            dBinding.tvDetailOutstanding.text = totalDue.money()

            dBinding.layoutCustomerTransactions.removeAllViews()
            if (customerSales.isEmpty()) {
                dBinding.tvNoTransactions.visibility = View.VISIBLE
            } else {
                dBinding.tvNoTransactions.visibility = View.GONE
                val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                customerSales.take(5).forEach { sale ->
                    val row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 10, 0, 10)
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    val info = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        text = "Inv #${sale.invoiceNumber} • ${dateFmt.format(Date(sale.saleDate))}"
                        textSize = 13f
                        setTextColor(android.graphics.Color.parseColor("#334155"))
                    }
                    val badge = TextView(requireContext()).apply {
                        text = "${sale.finalAmount.money()} [${sale.paymentStatus}]"
                        textSize = 12.5f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(if (sale.paymentStatus == PaymentStatus.PAID)
                            android.graphics.Color.parseColor("#15803D") else android.graphics.Color.parseColor("#DC2626"))
                    }
                    row.addView(info)
                    row.addView(badge)
                    dBinding.layoutCustomerTransactions.addView(row)
                }
            }

            dBinding.btnDetailReceivePayment.setOnClickListener {
                if (pendingSales.isEmpty()) {
                    Toast.makeText(requireContext(), "No outstanding balance for this customer", Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                    showReceivePaymentForCustomer(customer, pendingSales.first())
                }
            }
        }

        dBinding.btnDetailCall.setOnClickListener {
            ExportManager.openDialer(requireContext(), customer.phone)
        }

        dBinding.btnDetailWhatsApp.setOnClickListener {
            ExportManager.shareWhatsApp(requireContext(), customer.phone, "Assalam-o-Alaikum ${customer.name}, greeting from ${ShopProfile.name(requireContext())}.")
        }

        dBinding.btnDetailNewSale.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.navigation_new_sale)
        }

        dBinding.btnDetailEdit.setOnClickListener {
            dialog.dismiss()
            showAddCustomerDialog(customer)
        }

        dBinding.btnDetailClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showReceivePaymentForCustomer(customer: Customer, sale: Sale) {
        val dBinding = DialogReceivePaymentBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Collect Payment: ${customer.name}")
            .setView(dBinding.root)
            .create()

        dBinding.tvDueSummary.text = "Invoice #${sale.invoiceNumber}\nPending Amount: ${sale.finalAmount.money()}"
        dBinding.etReceiveAmount.setText(sale.finalAmount.toString())

        val methods = PaymentMethod.values().map { it.name }
        dBinding.spReceiveMethod.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, methods)

        dBinding.btnReceive.setOnClickListener {
            val amount = dBinding.etReceiveAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (amount <= 0.0) {
                Toast.makeText(requireContext(), "Valid amount required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val method = PaymentMethod.valueOf(dBinding.spReceiveMethod.selectedItem.toString())
            val payment = Payment(
                saleId = sale.id,
                customerId = customer.id,
                amount = amount,
                paymentMethod = method,
                paymentType = PaymentType.RECEIVED,
                notes = "Collected from ${customer.name}"
            )
            viewLifecycleOwner.lifecycleScope.launch {
                repository.recordPayment(payment)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Payment of ${amount.money()} recorded successfully!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun confirmDeleteCustomer(customer: Customer) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Customer Record?")
            .setMessage("Are you sure you want to remove '${customer.name}' from customer records?")
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.deleteCustomer(customer)
                    Toast.makeText(requireContext(), "Customer '${customer.name}' removed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddCustomerDialog(existingCustomer: Customer?) {
        val dBinding = DialogCustomerBinding.inflate(layoutInflater)

        if (existingCustomer != null) {
            dBinding.etCustName.setText(existingCustomer.name)
            dBinding.etCustPhone.setText(existingCustomer.phone)
            dBinding.etCustEmail.setText(existingCustomer.email)
            dBinding.etCustAddress.setText(existingCustomer.address)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existingCustomer == null) "Add New Customer" else "Edit Customer")
            .setView(dBinding.root)
            .create()

        dBinding.btnSaveCustomer.setOnClickListener {
            val name = dBinding.etCustName.text.toString().trim()
            val phone = dBinding.etCustPhone.text.toString().trim()
            val email = dBinding.etCustEmail.text.toString().trim()
            val address = dBinding.etCustAddress.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Name & Phone required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val toSave = (existingCustomer ?: Customer(name = name, phone = phone, email = email, address = address)).copy(
                    name = name,
                    phone = phone,
                    email = email,
                    address = address
                )
                if (existingCustomer == null) {
                    repository.insertCustomer(toSave)
                    Toast.makeText(requireContext(), "Customer added", Toast.LENGTH_SHORT).show()
                } else {
                    repository.updateCustomer(toSave)
                    Toast.makeText(requireContext(), "Customer updated", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
