package com.shopkeeper.mobileshop.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.FragmentDashboardBinding
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.ShopProfile
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        setupUI()
        loadDashboardData()
    }

    private fun setupUI() {
        val mode = AppPreferences.getMode(requireContext()) ?: AppMode.SHOP_OWNER
        binding.tvModeBadge.text = if (mode == AppMode.SHOP_OWNER) "🏪 Shop Owner Mode" else "🔧 Repair Tech Mode"
        binding.tvShopName.text = ShopProfile.name(requireContext())

        binding.cardNewSale.setOnClickListener {
            findNavController().navigate(R.id.navigation_new_sale)
        }
        binding.cardAddProduct.setOnClickListener {
            findNavController().navigate(R.id.navigation_inventory)
        }
        binding.cardNewRepair.setOnClickListener {
            findNavController().navigate(R.id.navigation_repairs)
        }
        binding.cardAddCustomer.setOnClickListener {
            findNavController().navigate(R.id.navigation_customers)
        }
        binding.cardReports.setOnClickListener {
            findNavController().navigate(R.id.navigation_reports)
        }
        binding.cardSettings.setOnClickListener {
            findNavController().navigate(R.id.navigation_settings)
        }
        binding.btnViewLowStock.setOnClickListener {
            findNavController().navigate(R.id.navigation_inventory)
        }
    }

    private fun loadDashboardData() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = System.currentTimeMillis()

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

            db.saleDao().getTotalSalesAmount(startOfDay, endOfDay).collect { sales ->
                binding.tvTodaySales.text = (sales ?: 0.0).money()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.saleDao().getTotalSalesCount(startOfDay, endOfDay).collect { count ->
                binding.tvSalesCount.text = "$count transactions today"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.totalInventoryValue.collect { value ->
                binding.tvInventoryValue.text = (value ?: 0.0).money()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.totalProductCount.collect { count ->
                binding.tvProductCount.text = "$count products in catalog"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.totalPendingAmount.collect { dues ->
                binding.tvPendingPayments.text = (dues ?: 0.0).money()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.activeRepairCount.collect { count ->
                binding.tvActiveRepairs.text = "$count in shop"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.lowStockProducts.collect { lowStock ->
                if (lowStock.isNotEmpty()) {
                    binding.cardLowStock.visibility = View.VISIBLE
                    binding.tvLowStockCount.text = "${lowStock.size} products are running low on stock!"
                } else {
                    binding.cardLowStock.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
