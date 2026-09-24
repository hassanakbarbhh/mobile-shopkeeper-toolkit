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
import com.shopkeeper.mobileshop.databinding.ItemDashboardRecentSaleBinding
import com.shopkeeper.mobileshop.domain.DeadStockDetector
import com.shopkeeper.mobileshop.domain.NetProfitEngine
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.ShopProfile
import com.shopkeeper.mobileshop.utils.UserAuthManager
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        val user = UserAuthManager.getCurrentUser(requireContext())
        val mode = AppPreferences.getMode(requireContext()) ?: user?.role ?: AppMode.SHOP_OWNER

        // Dynamic time-based greeting
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingPrefix = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
        val displayName = user?.displayName?.substringBefore("@") ?: "Partner"
        binding.tvGreeting.text = "$greetingPrefix, $displayName"

        // Formatted Date
        val dateFmt = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        binding.tvDashboardDate.text = dateFmt.format(Date())

        // Shop Name
        binding.tvShopName.text = ShopProfile.name(requireContext())

        // Role Badge
        binding.tvModeBadge.text = when (mode) {
            AppMode.OWNER, AppMode.SHOP_OWNER -> "👑 Shop Owner (Master)"
            AppMode.SELLER_STAFF -> "💼 Seller / Staff Mode"
            AppMode.REPAIR_TECH -> "🔧 Repair Tech Mode"
            else -> "👤 ${mode.name.replace('_', ' ')}"
        }

        // Quick New Sale inside hero card
        binding.btnQuickSale.setOnClickListener {
            findNavController().navigate(R.id.navigation_new_sale)
        }

        // Navigation Card Clicks
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
        binding.cardImeiTracker.setOnClickListener {
            findNavController().navigate(R.id.navigation_imei)
        }
        binding.cardOnlineCatalog.setOnClickListener {
            findNavController().navigate(R.id.navigation_online_catalog)
        }
        binding.cardSettings.setOnClickListener {
            findNavController().navigate(R.id.navigation_settings)
        }

        // Performance Stat Cards Clicks
        binding.cardInventoryStats.setOnClickListener {
            findNavController().navigate(R.id.navigation_inventory)
        }
        binding.cardDuesStats.setOnClickListener {
            findNavController().navigate(R.id.navigation_dues)
        }
        binding.cardRepairStats.setOnClickListener {
            findNavController().navigate(R.id.navigation_repairs)
        }
        binding.cardPurchases.setOnClickListener {
            findNavController().navigate(R.id.navigation_purchases)
        }

        // Action Buttons
        binding.btnViewLowStock.setOnClickListener {
            findNavController().navigate(R.id.navigation_inventory)
        }
        binding.btnViewDeadStock.setOnClickListener {
            findNavController().navigate(R.id.navigation_inventory)
        }
        binding.btnViewAllSales.setOnClickListener {
            findNavController().navigate(R.id.navigation_sales)
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
        val db = AppDatabase.getDatabase(requireContext())

        // 1. Today's Total Gross Sales
        viewLifecycleOwner.lifecycleScope.launch {
            db.saleDao().getTotalSalesAmount(startOfDay, endOfDay).collectLatest { sales ->
                _binding?.tvTodaySales?.text = (sales ?: 0.0).money()
            }
        }

        // 2. Today's Net Profit & Recent Invoices
        viewLifecycleOwner.lifecycleScope.launch {
            db.saleDao().getSalesByDateRange(startOfDay, endOfDay).collectLatest { sales ->
                var totalProfit = 0.0
                val profitEngine = NetProfitEngine()
                for (sale in sales) {
                    val items = repository.getSaleItems(sale.id)
                    for (item in items) {
                        val product = repository.getProduct(item.productId)
                        val purchasePrice = product?.purchasePrice ?: 0.0
                        totalProfit += profitEngine.calculateNetProfit(item.totalPrice, purchasePrice * item.quantity, 0.0, 0.0)
                    }
                    totalProfit -= sale.discount
                }
                _binding?.tvTodayProfit?.text = totalProfit.money()

                // Populate Recent Sales Feed
                renderRecentSales(sales)
            }
        }

        // 3. Transactions Volume Count
        viewLifecycleOwner.lifecycleScope.launch {
            db.saleDao().getTotalSalesCount(startOfDay, endOfDay).collectLatest { count ->
                _binding?.tvSalesCount?.text = "$count transactions today"
            }
        }

        // 4. Inventory Capital
        viewLifecycleOwner.lifecycleScope.launch {
            repository.totalInventoryValue.collectLatest { value ->
                _binding?.tvInventoryValue?.text = (value ?: 0.0).money()
            }
        }

        // 5. Total Products Count
        viewLifecycleOwner.lifecycleScope.launch {
            repository.totalProductCount.collectLatest { count ->
                _binding?.tvProductCount?.text = "$count products in catalog"
            }
        }

        // 6. Pending Khata Dues
        viewLifecycleOwner.lifecycleScope.launch {
            repository.totalPendingAmount.collectLatest { dues ->
                _binding?.tvPendingPayments?.text = (dues ?: 0.0).money()
            }
        }

        // 7. Active Repair Jobs
        viewLifecycleOwner.lifecycleScope.launch {
            repository.activeRepairCount.collectLatest { count ->
                _binding?.tvActiveRepairs?.text = "$count active jobs"
            }
        }

        // 8. Low Stock Alert
        viewLifecycleOwner.lifecycleScope.launch {
            repository.lowStockProducts.collectLatest { lowStock ->
                if (lowStock.isNotEmpty()) {
                    _binding?.cardLowStock?.visibility = View.VISIBLE
                    _binding?.tvLowStockCount?.text = "${lowStock.size} products below threshold"
                } else {
                    _binding?.cardLowStock?.visibility = View.GONE
                }
            }
        }

        // 9. Dead Stock Detector
        viewLifecycleOwner.lifecycleScope.launch {
            repository.allProducts.collectLatest { products ->
                val deadStockDetector = DeadStockDetector()
                var deadStockCount = 0
                var lockedCapital = 0.0
                for (product in products) {
                    val info = deadStockDetector.analyzeStock(product.updatedAt, product.quantity, product.purchasePrice)
                    if (info.isDead) {
                        deadStockCount++
                        lockedCapital += info.lockedCapital
                    }
                }
                if (deadStockCount > 0) {
                    _binding?.cardDeadStock?.visibility = View.VISIBLE
                    _binding?.tvDeadStockCount?.text = "$deadStockCount slow-moving items detected"
                    _binding?.tvDeadStockCapital?.text = "Locked Capital: " + lockedCapital.money()
                } else {
                    _binding?.cardDeadStock?.visibility = View.GONE
                }
            }
        }
    }

    private fun renderRecentSales(sales: List<com.shopkeeper.mobileshop.data.db.entity.Sale>) {
        val container = _binding?.layoutRecentSales ?: return
        val emptyView = _binding?.layoutEmptySales ?: return
        container.removeAllViews()

        if (sales.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            container.visibility = View.GONE
            return
        }

        emptyView.visibility = View.GONE
        container.visibility = View.VISIBLE

        val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        val recentList = sales.sortedByDescending { it.saleDate }.take(4)

        for (sale in recentList) {
            val itemBinding = ItemDashboardRecentSaleBinding.inflate(layoutInflater, container, false)
            itemBinding.tvRecentCustomerName.text = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer"
            itemBinding.tvRecentSaleTime.text = "${timeFmt.format(Date(sale.saleDate))} • ${sale.paymentMethod}"
            itemBinding.tvRecentSaleAmount.text = sale.finalAmount.money()
            itemBinding.tvRecentSaleStatus.text = sale.paymentStatus.name
            
            when (sale.paymentStatus) {
                com.shopkeeper.mobileshop.data.db.entity.PaymentStatus.PAID -> {
                    itemBinding.tvRecentSaleStatus.setTextColor(android.graphics.Color.parseColor("#065F46"))
                    itemBinding.tvRecentSaleStatus.setBackgroundResource(R.drawable.bg_badge_green)
                }
                com.shopkeeper.mobileshop.data.db.entity.PaymentStatus.PENDING -> {
                    itemBinding.tvRecentSaleStatus.setTextColor(android.graphics.Color.parseColor("#991B1B"))
                    itemBinding.tvRecentSaleStatus.setBackgroundResource(R.drawable.bg_badge_red)
                }
                com.shopkeeper.mobileshop.data.db.entity.PaymentStatus.PARTIAL -> {
                    itemBinding.tvRecentSaleStatus.setTextColor(android.graphics.Color.parseColor("#92400E"))
                    itemBinding.tvRecentSaleStatus.setBackgroundResource(R.drawable.bg_badge_amber)
                }
            }

            itemBinding.root.setOnClickListener {
                findNavController().navigate(R.id.navigation_sales)
            }

            container.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
