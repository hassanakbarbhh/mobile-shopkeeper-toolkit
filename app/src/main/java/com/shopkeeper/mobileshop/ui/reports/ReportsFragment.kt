package com.shopkeeper.mobileshop.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ReportsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)
        
        val barChart = view.findViewById<BarChart>(R.id.barChartSales)
        val pieChart = view.findViewById<PieChart>(R.id.pieChartProfit)

        loadLiveReportData(barChart, pieChart)

        return view
    }

    private fun loadLiveReportData(barChart: BarChart, pieChart: PieChart) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val allSales = db.saleDao().getAllSalesList()
            val allSaleItems = db.saleDao().getAllSaleItemsList() // Wait, I need this
            
            // Generate real chart data
            val barEntries = ArrayList<BarEntry>()
            
            // Group sales by day (last 5 days)
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis
            
            for (i in 0..4) {
                val dayStart = todayStart - ((4 - i) * 24 * 60 * 60 * 1000L)
                val dayEnd = dayStart + (24 * 60 * 60 * 1000L)
                val daySales = allSales.filter { it.saleDate in dayStart until dayEnd }.sumOf { it.finalAmount }
                barEntries.add(BarEntry((i + 1).toFloat(), daySales.toFloat()))
            }

            // Pie chart data by Category (using dummy categories or product names if category not available)
            // Or just profit logic if available. Let's do revenue by item name for top 4 items.
            val itemRevenue = mutableMapOf<String, Float>()
            allSaleItems.forEach { item ->
                itemRevenue[item.productName] = (itemRevenue[item.productName] ?: 0f) + item.totalPrice.toFloat()
            }
            val topItems = itemRevenue.entries.sortedByDescending { it.value }.take(4)
            val pieEntries = ArrayList<PieEntry>()
            topItems.forEach {
                pieEntries.add(PieEntry(it.value, it.key.take(12)))
            }
            if (pieEntries.isEmpty()) {
                pieEntries.add(PieEntry(1f, "No Sales Yet"))
            }

            withContext(Dispatchers.Main) {
                setupBarChart(barChart, barEntries)
                setupPieChart(pieChart, pieEntries)
            }
        }
    }

    private fun setupBarChart(barChart: BarChart, entries: List<BarEntry>) {
        val dataSet = BarDataSet(entries, "Last 5 Days Sales")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 14f

        val data = BarData(dataSet)
        barChart.data = data
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun setupPieChart(pieChart: PieChart, entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "Top Products Revenue")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Revenue"
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
