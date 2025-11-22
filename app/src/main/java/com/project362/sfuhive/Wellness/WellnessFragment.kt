package com.project362.sfuhive.Wellness

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.project362.sfuhive.R

class WellnessFragment : Fragment() {
    private lateinit var energyBtn1: Button
    private lateinit var energyBtn2: Button
    private lateinit var energyBtn3: Button
    private lateinit var energyBtn4: Button
    private lateinit var energyBtn5: Button

    private lateinit var energyViewModel: EnergyViewModel

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
        val goalsLabel = view.findViewById<TextView>(R.id.goals_label)

        chartBtn.setOnClickListener {
            val intent = Intent(requireContext(), EnergyMgmtActivity::class.java)
            startActivity(intent)
        }

        goalsLabel.setOnClickListener {
            val intent = Intent(requireContext(), GoalsActivity::class.java)
            startActivity(intent)
        }

        // energy label functionality ===================================================================
        energyViewModel = ViewModelProvider(this).get(EnergyViewModel::class.java)
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

        // youtube stuff
        loadThumbnail(thumb1, id1)
        loadThumbnail(thumb2, id2)
        loadThumbnail(thumb3, id3)

        setYoutubeClick(video1, id1)
        setYoutubeClick(video2, id2)
        setYoutubeClick(video3, id3)

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


}
