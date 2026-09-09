package com.shopkeeper.mobileshop.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

class ReportsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)
        
        val barChart = view.findViewById<BarChart>(R.id.barChartSales)
        val pieChart = view.findViewById<PieChart>(R.id.pieChartProfit)

        setupBarChart(barChart)
        setupPieChart(pieChart)

        return view
    }

    private fun setupBarChart(barChart: BarChart) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 1500f))
        entries.add(BarEntry(2f, 2300f))
        entries.add(BarEntry(3f, 1800f))
        entries.add(BarEntry(4f, 3200f))
        entries.add(BarEntry(5f, 2900f))

        val dataSet = BarDataSet(entries, "Weekly Sales")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 14f

        val data = BarData(dataSet)
        barChart.data = data
        barChart.description.isEnabled = false
        barChart.animateY(1000)
    }

    private fun setupPieChart(pieChart: PieChart) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(45f, "Smartphones"))
        entries.add(PieEntry(25f, "Accessories"))
        entries.add(PieEntry(20f, "Repairs"))
        entries.add(PieEntry(10f, "Used Phones"))

        val dataSet = PieDataSet(entries, "Revenue Source")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Profit"
        pieChart.animateY(1000)
    }
}
