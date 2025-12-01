package com.project362.sfuhive.Wellness


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.project362.sfuhive.R
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry;
import java.text.SimpleDateFormat
import java.util.Locale
import com.bumptech.glide.Glide



class EnergyMgmtActivity : AppCompatActivity() {
    // energy
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

        // youtube link unique ids
        val id1 = "inpok4MKVLM"
        val id2 = "ZToicYcHIOU"
        val id3 = "2OEL4P1Rz04"

        // Find views
        val video1 = findViewById<View>(R.id.videoTile1)
        val video2 = findViewById<View>(R.id.videoTile2)
        val video3 = findViewById<View>(R.id.videoTile3)

        val thumb1 = findViewById<ImageView>(R.id.thumb1)
        val thumb2 = findViewById<ImageView>(R.id.thumb2)
        val thumb3 = findViewById<ImageView>(R.id.thumb3)

        // observer energy from view model
        energyViewModel.energies.observe(this) { list ->
            // Log.d("EnergyList", "Size = ${list.size}, data=$list")

            if (!list.isNullOrEmpty()) {
                renderChart(list)
            }
        }

        // load energies
        energyViewModel.loadEnergies(this)

        // add energy on click
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


        // youtube stuff
        loadThumbnail(thumb1, id1)
        loadThumbnail(thumb2, id2)
        loadThumbnail(thumb3, id3)

        setYoutubeClick(video1, id1)
        setYoutubeClick(video2, id2)
        setYoutubeClick(video3, id3)
    }

    // rendering chart ==============================================================================
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

    // youtube media ==============================================================================
    // show the thumbnail
    fun loadThumbnail(imageView: ImageView, videoId: String) {
        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
        Glide.with(this).load(thumbnailUrl).into(imageView)
    }

    private fun setYoutubeClick(view: View, videoId: String) {
        view.setOnClickListener { openYouTube(videoId) }
    }

    private fun openYouTube(videoId: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))

        try {
            startActivity(appIntent)
        } catch (e: Exception) {
            startActivity(webIntent)
        }
    }

}