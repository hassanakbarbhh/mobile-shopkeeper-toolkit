package com.shopkeeper.mobileshop.ui.sellers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Seller
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogAddSellerBinding
import com.shopkeeper.mobileshop.databinding.FragmentSellersBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SellersFragment : Fragment() {

    private var _binding: FragmentSellersBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: SellerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        adapter = SellerAdapter(
            onCallClick = { seller ->
                if (seller.phone.isNotBlank()) {
                    ExportManager.openDialer(requireContext(), seller.phone)
                } else {
                    Toast.makeText(requireContext(), "No phone number for ${seller.name}", Toast.LENGTH_SHORT).show()
                }
            },
            onWhatsAppClick = { seller ->
                if (seller.phone.isNotBlank()) {
                    ExportManager.shareWhatsApp(
                        requireContext(),
                        seller.phone,
                        "Assalam-o-Alaikum ${seller.name}, message from Mobile Shop Owner."
                    )
                } else {
                    Toast.makeText(requireContext(), "No phone number for ${seller.name}", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { seller ->
                showDeleteSellerDialog(seller)
            }
        )

        binding.rvSellers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSellers.adapter = adapter

        binding.fabAddSeller.setOnClickListener {
            showAddSellerDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allSellers.collectLatest { list ->
                adapter.submitList(list)
                binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.tvStaffSummary.text = "Active staff members: ${list.count { it.isActive }} • Total registered sellers: ${list.size}"
            }
        }
    }

    private fun showAddSellerDialog() {
        val dBinding = DialogAddSellerBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dBinding.root)
            .create()

        dBinding.btnSaveSeller.setOnClickListener {
            val name = dBinding.etSellerName.text.toString().trim()
            val phone = dBinding.etSellerPhone.text.toString().trim()
            val role = dBinding.etSellerRole.text.toString().trim().ifEmpty { "Sales Executive" }
            val commission = dBinding.etCommissionPercent.text.toString().toDoubleOrNull() ?: 0.0
            val pin = dBinding.etSellerPin.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Seller name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val seller = Seller(
                name = name,
                phone = phone,
                role = role,
                commissionPercent = commission,
                pin = pin,
                isActive = true
            )

            viewLifecycleOwner.lifecycleScope.launch {
                repository.insertSeller(seller)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Seller member $name added!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showDeleteSellerDialog(seller: Seller) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Seller Member?")
            .setMessage("Are you sure you want to remove '${seller.name}' from your shop staff list? Past sales by this seller will still remain in historical records.")
            .setPositiveButton("Remove") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.deleteSeller(seller)
                    Toast.makeText(requireContext(), "Seller '${seller.name}' removed", Toast.LENGTH_SHORT).show()
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
