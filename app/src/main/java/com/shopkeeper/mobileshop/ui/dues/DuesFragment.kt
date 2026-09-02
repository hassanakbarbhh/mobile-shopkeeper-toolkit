package com.shopkeeper.mobileshop.ui.dues

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
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Payment
import com.shopkeeper.mobileshop.data.db.entity.PaymentMethod
import com.shopkeeper.mobileshop.data.db.entity.PaymentType
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogReceivePaymentBinding
import com.shopkeeper.mobileshop.databinding.FragmentDuesBinding
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DuesFragment : Fragment() {

    private var _binding: FragmentDuesBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: DueAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDuesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        adapter = DueAdapter { sale -> showReceiveDialog(sale) }
        binding.rvDues.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDues.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repository.dueSales.collectLatest { list ->
                adapter.submitList(list)
                binding.tvNoDues.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showReceiveDialog(sale: Sale) {
        val dBinding = DialogReceivePaymentBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Collect Payment")
            .setView(dBinding.root)
            .create()

        dBinding.tvDueSummary.text = "Customer: ${sale.customerName}\nPending Amount: ${sale.finalAmount.money()}"
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
                customerId = sale.customerId,
                amount = amount,
                paymentMethod = method,
                paymentType = PaymentType.RECEIVED,
                notes = "Collected udhaar"
            )

            viewLifecycleOwner.lifecycleScope.launch {
                repository.recordPayment(payment)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Payment recorded! Status updated.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
