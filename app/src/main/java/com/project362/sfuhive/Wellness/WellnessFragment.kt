package com.project362.sfuhive.Wellness

import android.R.attr.button
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.project362.sfuhive.R
import com.project362.sfuhive.Util.getViewModelFactory
import com.project362.sfuhive.database.Badge.BadgeDatabase
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.Wellness.Goal
import kotlinx.coroutines.launch

class WellnessFragment : Fragment() {
    private lateinit var energyBtn1: Button
    private lateinit var energyBtn2: Button
    private lateinit var energyBtn3: Button
    private lateinit var energyBtn4: Button
    private lateinit var energyBtn5: Button

    // goal labels and check boxes
    private lateinit var goal1Title: TextView
    private lateinit var goal2Title: TextView
    private lateinit var goal3Title: TextView


    private lateinit var energyViewModel: EnergyViewModel
    private lateinit var goalViewModel: DataViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wellness, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chartBtn = view.findViewById<Button>(R.id.viewChart)
        val goalsBtn = view.findViewById<TextView>(R.id.viewGoals)

        chartBtn.setOnClickListener {
            val intent = Intent(requireContext(), EnergyMgmtActivity::class.java)
            startActivity(intent)
        }

        goalsBtn.setOnClickListener {
            val intent = Intent(requireContext(), GoalsActivity::class.java)
            startActivity(intent)
        }

        // view models and databases
        energyViewModel = ViewModelProvider(this).get(EnergyViewModel::class.java)
        val badgeDb = BadgeDatabase.getInstance(requireContext()) // Ensure badges are inserted
        val factory = getViewModelFactory(requireContext())
        goalViewModel = ViewModelProvider(this, factory).get(DataViewModel::class.java)


        // energy label functionality ===================================================================
        energyBtn1 = view.findViewById<Button>(R.id.btn_1)
        energyBtn2 = view.findViewById<Button>(R.id.btn_2)
        energyBtn3 = view.findViewById<Button>(R.id.btn_3)
        energyBtn4 = view.findViewById<Button>(R.id.btn_4)
        energyBtn5 = view.findViewById<Button>(R.id.btn_5)

        energyBtn1.tag = 1
        energyBtn2.tag = 2
        energyBtn3.tag = 3
        energyBtn4.tag = 4
        energyBtn5.tag = 5

        val id1 = "inpok4MKVLM"
        val id2 = "ZToicYcHIOU"
        val id3 = "2OEL4P1Rz04"

        // Find views
        val video1 = view.findViewById<View>(R.id.videoTile1)
        val video2 = view.findViewById<View>(R.id.videoTile2)
        val video3 = view.findViewById<View>(R.id.videoTile3)

        val thumb1 = view.findViewById<ImageView>(R.id.thumb1)
        val thumb2 = view.findViewById<ImageView>(R.id.thumb2)
        val thumb3 = view.findViewById<ImageView>(R.id.thumb3)

        val clickListener = View.OnClickListener { view ->
            val value = view.tag as Int
            // Log.d("wellness", "saving energy from wellness frag $value")
            energyViewModel.addEnergies(requireContext(), value)
            Toast.makeText(requireActivity(), "Your energy is saved", Toast.LENGTH_SHORT).show()
        }
        energyBtn1.setOnClickListener(clickListener)
        energyBtn2.setOnClickListener(clickListener)
        energyBtn3.setOnClickListener(clickListener)
        energyBtn4.setOnClickListener(clickListener)
        energyBtn5.setOnClickListener(clickListener)

        // youtube stuff  ===================================================================
        loadThumbnail(thumb1, id1)
        loadThumbnail(thumb2, id2)
        loadThumbnail(thumb3, id3)

        setYoutubeClick(video1, id1)
        setYoutubeClick(video2, id2)
        setYoutubeClick(video3, id3)

        // goals functionality  ===================================================================
        // need to make sure the titles and checkboxes are in sync with the goals activity
        goal1Title = view.findViewById(R.id.goal1_title)
        goal2Title = view.findViewById(R.id.goal2_title)
        goal3Title = view.findViewById(R.id.goal3_title)

        lifecycleScope.launch {
            goalViewModel.getAllGoals().collect { goals ->
                val g1 = goals.firstOrNull { it.id == 1L }
                val g2 = goals.firstOrNull { it.id == 2L }
                val g3 = goals.firstOrNull { it.id == 3L }

                updateGoalUI(1, g1)
                updateGoalUI(2, g2)
                updateGoalUI(3, g3)
            }
        }

    }

    // youtube media ==============================================================================
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

    // for the goal stuff ========================================
    private fun updateGoalUI(index: Int, goal: Goal?) {
        val titleView: TextView
        val checkBox: CheckBox

        when (index) {
            1 -> { titleView = goal1Title }
            2 -> { titleView = goal2Title }
            else -> {
                titleView = goal3Title
            }
        }

        val name = goal?.goalName.orEmpty()

        if (name.isBlank()) {
            titleView.text = "Not set"
        } else {
            titleView.text = name
        }
    }
}
