package com.shopkeeper.mobileshop.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.FragmentReportsBinding
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        loadFinancialReports(db)
    }

    private fun loadFinancialReports(db: AppDatabase) {
        viewLifecycleOwner.lifecycleScope.launch {
            val sales = repository.allSales.first()
            val expenses = repository.allExpenses.first()
            val products = repository.allProducts.first()

            val revenue = sales.sumOf { it.finalAmount }
            val totalExpense = expenses.sumOf { it.amount }
            val count = sales.size
            val avg = if (count > 0) revenue / count else 0.0

            // Rough cost computation
            val estimatedCost = revenue * 0.75
            val grossProfit = revenue - estimatedCost
            val netProfit = grossProfit - totalExpense

            binding.tvTotalRevenue.text = revenue.money()
            binding.tvTotalProfit.text = grossProfit.money()
            binding.tvTotalExpenses.text = totalExpense.money()
            binding.tvNetProfit.text = netProfit.money()
            binding.tvTotalSales.text = "$count"
            binding.tvAvgSaleValue.text = avg.money()

            setupBarChart(sales)
            setupPieChart(products)
            setupTopProducts(products)
        }
    }

    private fun setupBarChart(sales: List<Sale>) {
        val entries = ArrayList<BarEntry>()
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val sampleValues = floatArrayOf(12000f, 18000f, 15000f, 22000f, 31000f, 45000f, 28000f)

        for (i in sampleValues.indices) {
            entries.add(BarEntry(i.toFloat(), sampleValues[i]))
        }

        val dataSet = BarDataSet(entries, "Sales (₹)").apply {
            color = Color.parseColor("#166534")
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            animateY(800)
            invalidate()
        }
    }

    private fun setupPieChart(products: List<com.shopkeeper.mobileshop.data.db.entity.Product>) {
        val entries = ArrayList<PieEntry>()
        val catMap = products.groupBy { it.category }
        catMap.forEach { (cat, list) ->
            entries.add(PieEntry(list.size.toFloat(), cat.name.take(8)))
        }
        if (entries.isEmpty()) {
            entries.add(PieEntry(100f, "Smartphones"))
        }

        val colors = listOf(
            Color.parseColor("#15803D"),
            Color.parseColor("#0284C7"),
            Color.parseColor("#D97706"),
            Color.parseColor("#9333EA"),
            Color.parseColor("#E11D48")
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextColor = Color.WHITE
            valueTextSize = 11f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            animateXY(800, 800)
            invalidate()
        }
    }

    private fun setupTopProducts(products: List<com.shopkeeper.mobileshop.data.db.entity.Product>) {
        binding.llTopProducts.removeAllViews()
        products.take(5).forEachIndexed { i, p ->
            val tv = TextView(requireContext()).apply {
                text = "${i + 1}. ${p.name} — ${p.sellingPrice.money()} (${p.quantity} in stock)"
                setPadding(0, 8, 0, 8)
                textSize = 13f
            }
            binding.llTopProducts.addView(tv)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
