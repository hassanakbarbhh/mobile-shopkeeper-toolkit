package com.shopkeeper.mobileshop.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Customer
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogCustomerBinding
import com.shopkeeper.mobileshop.databinding.FragmentCustomersBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import kotlinx.coroutines.flow.collectLatest
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
            showCustomerOptionsDialog(customer)
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
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allCustomers.collectLatest { list ->
                currentCustomers = list
                adapter.submitList(list)
            }
        }
    }

    private fun showCustomerOptionsDialog(customer: Customer) {
        val options = arrayOf("Call Customer", "WhatsApp Message", "Edit Information", "Delete Customer Record")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(customer.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ExportManager.openDialer(requireContext(), customer.phone)
                    1 -> ExportManager.shareWhatsApp(requireContext(), customer.phone, "Assalam-o-Alaikum ${customer.name}, greeting from our Mobile Shop.")
                    2 -> showAddCustomerDialog(customer)
                    3 -> confirmDeleteCustomer(customer)
                }
            }
            .setNegativeButton("Close", null)
            .show()
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
