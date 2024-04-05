package com.example.pbd_jwr.ui.dashboard

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pbd_jwr.R
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.ui.transaction.TransactionViewModel
import com.github.mikephil.charting.animation.Easing

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var viewModel: TransactionViewModel
    private lateinit var encryptedSharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        pieChart = view.findViewById(R.id.pieChart)
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserEmail = encryptedSharedPref.getString("email", "") ?: ""
        viewModel.getTransactionsByEmail(currentUserEmail).observe(viewLifecycleOwner) { transactions ->
            loadPieChartData(transactions)
        }
    }

    private fun loadPieChartData(transactions: List<Transaction>) {

        val totalPerCategory = transactions.groupBy { it.category }
            .mapValues { entry ->
                entry.value.sumOf { it.amount }
            }

        val entries = ArrayList<PieEntry>()
        for ((category, total) in totalPerCategory) {
            entries.add(
                PieEntry(
                    total.toFloat(),
                    category.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                )
            )
        }

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.setColors(*ColorTemplate.MATERIAL_COLORS) // Atur warna
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate() // refresh chart
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        pieChart.centerText = "Transactions\nCategory Amount"
        pieChart.setCenterTextSize(20F)
    }
}

