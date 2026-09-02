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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: CustomerAdapter

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
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(customer.name)
                .setMessage("Phone: ${customer.phone}\nEmail: ${customer.email}\nAddress: ${customer.address}")
                .setPositiveButton("Close", null)
                .show()
        }

        binding.rvCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomers.adapter = adapter

        binding.fabAddCustomer.setOnClickListener { showAddCustomerDialog() }

        binding.etCustomerSearch.doAfterTextChanged { text ->
            val q = text?.toString().orEmpty()
            viewLifecycleOwner.lifecycleScope.launch {
                repository.searchCustomers(q).collectLatest { list ->
                    adapter.submitList(list)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allCustomers.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    private fun showAddCustomerDialog() {
        val dBinding = DialogCustomerBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Customer")
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
                repository.insertCustomer(
                    Customer(name = name, phone = phone, email = email, address = address)
                )
                dialog.dismiss()
                Toast.makeText(requireContext(), "Customer added", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
