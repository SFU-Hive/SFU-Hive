package com.project362.sfuhive.Wellness


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.util.Log
import com.project362.sfuhive.R
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry;
import java.text.SimpleDateFormat
import java.util.Locale


class EnergyMgmtActivity : AppCompatActivity() {
    private lateinit var energyBtn1: Button
    private lateinit var energyBtn2: Button
    private lateinit var energyBtn3: Button
    private lateinit var energyBtn4: Button
    private lateinit var energyBtn5: Button

    private lateinit var energyViewModel: EnergyViewModel
    private lateinit var lineChart: LineChart


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_energy_mgmt)

        // entering energy levels =======================================

        energyViewModel = ViewModelProvider(this).get(EnergyViewModel::class.java)
        lineChart = findViewById(R.id.lineChart)

        energyBtn1 = findViewById(R.id.btn_1)
        energyBtn2 = findViewById(R.id.btn_2)
        energyBtn3 = findViewById(R.id.btn_3)
        energyBtn4 = findViewById(R.id.btn_4)
        energyBtn5 = findViewById(R.id.btn_5)

        energyBtn1.tag = 1
        energyBtn2.tag = 2
        energyBtn3.tag = 3
        energyBtn4.tag = 4
        energyBtn5.tag = 5

        // observer energy from view model
        energyViewModel.energies.observe(this) { list ->
            // Log.d("EnergyList", "Size = ${list.size}, data=$list")

            if (!list.isNullOrEmpty()) {
                renderChart(list)
            }
        }

        // load energies
        energyViewModel.loadEnergies(this)


        val clickListener = View.OnClickListener { view ->
            val value = view.tag as Int
            Toast.makeText(this, "Your energy is saved", Toast.LENGTH_SHORT).show()
            energyViewModel.addEnergies(this, value)
        }
        energyBtn1.setOnClickListener(clickListener)
        energyBtn2.setOnClickListener(clickListener)
        energyBtn3.setOnClickListener(clickListener)
        energyBtn4.setOnClickListener(clickListener)
        energyBtn5.setOnClickListener(clickListener)
    }

    // rendering chart =======================================
    private fun renderChart(energyList: List<Pair<String, Int>>) {
        if (energyList.isEmpty()) return

        // extract day part
        val dayList = energyList.map { extractDay(it.first) }

        // build list of unique days
        val uniqueDays = dayList.distinct()

        // prep entries
        val entries = energyList.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second.toFloat())
        }

        // configure dataset
        val dataSet = LineDataSet(entries, "Energy Level")
        lineChart.data = LineData(dataSet)

        lineChart.xAxis.apply {
            valueFormatter = ChartFormatter(dayList, uniqueDays)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelRotationAngle = -30f
        }


        // style the chart axes & description
        lineChart.apply {
            visibility = View.VISIBLE
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            animateX(700)
            description.isEnabled = false
        }

        lineChart.invalidate() // refresh
    }

    private fun extractDay(dateString: String): String {
        return try {
            val inFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
            val date = inFormat.parse(dateString)

            val outFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            outFormat.format(date!!)
        } catch (e: Exception) {
            dateString // fallback
        }
    }
}