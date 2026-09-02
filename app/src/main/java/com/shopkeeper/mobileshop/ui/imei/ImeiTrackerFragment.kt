package com.shopkeeper.mobileshop.ui.imei

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.databinding.FragmentImeiTrackerBinding
import com.shopkeeper.mobileshop.utils.dateText
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.launch

class ImeiTrackerFragment : Fragment() {

    private var _binding: FragmentImeiTrackerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImeiTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())

        binding.btnTrace.setOnClickListener {
            val imei = binding.etImei.text?.toString()?.trim().orEmpty()
            if (imei.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an IMEI number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            traceImei(db, imei)
        }
    }

    private fun traceImei(db: AppDatabase, imei: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val product = db.productDao().getByImei(imei)
            val purchaseItems = db.purchaseDao().itemsByImei(imei)
            val saleItems = db.saleDao().saleItemsByImei(imei)
            val repairs = db.repairDao().byImei(imei)

            binding.tvEmpty.visibility = View.GONE

            if (product != null) {
                binding.cardProduct.visibility = View.VISIBLE
                binding.tvProduct.text = "📱 CATALOG / INVENTORY\n${product.name} (${product.brand})\nCurrent Stock: ${product.quantity}\nSelling: ${product.sellingPrice.money()}"
            } else {
                binding.cardProduct.visibility = View.GONE
            }

            if (purchaseItems.isNotEmpty()) {
                binding.cardBought.visibility = View.VISIBLE
                val p = purchaseItems.first()
                binding.tvBought.text = "📦 PURCHASE / SUPPLIER\nBought: ${p.productName}\nSupplier: ${p.supplierName}\nCost: ${p.unitCost.money()}"
            } else {
                binding.cardBought.visibility = View.GONE
            }

            if (saleItems.isNotEmpty()) {
                binding.cardSold.visibility = View.VISIBLE
                val s = saleItems.first()
                binding.tvSold.text = "💰 SALE / INVOICE\nSold: ${s.productName}\nInvoice: #${s.saleId}\nPrice: ${s.totalPrice.money()}"
            } else {
                binding.cardSold.visibility = View.GONE
            }

            if (repairs.isNotEmpty()) {
                binding.cardService.visibility = View.VISIBLE
                val r = repairs.first()
                binding.tvService.text = "🔧 REPAIR HISTORY\nDevice: ${r.deviceBrand} ${r.deviceModel}\nCustomer: ${r.customerName}\nIssue: ${r.issueDescription}\nStatus: ${r.status}"
            } else {
                binding.cardService.visibility = View.GONE
            }

            if (product == null && purchaseItems.isEmpty() && saleItems.isEmpty() && repairs.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text = "No records found for IMEI: $imei"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
