package com.project362.sfuhive.Dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.project362.sfuhive.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    data class ImportantDate(
        val name: String,
        val date: String,
        val task: String,
        val isComplete: Boolean
    )

    data class RecentFile(
        val fileName: String,
        val date: String,
        val size: String
    )

    data class StreakInfo(
        val date: String,
        val isComplete: Boolean
    )


    private lateinit var datesData: List<ImportantDate>
    private lateinit var recentFilesData: List<RecentFile>
    private lateinit var adapter: ImportantDateAdapter
    private lateinit var streakIcons: Array<ImageView?>
    private lateinit var recentFiles: GridView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeDummyData()
        initializeDummyFiles()


        val importantDates = view.findViewById<ListView>(R.id.list_view)
        recentFiles = view.findViewById(R.id.recent_files)

        importantDates.adapter = ImportantDateAdapter(requireContext(), datesData)


        streakIcons = arrayOf(
            view.findViewById(R.id.streak_item1),
            view.findViewById(R.id.streak_item2),
            view.findViewById(R.id.streak_item3),
            view.findViewById(R.id.streak_item4),
            view.findViewById(R.id.streak_item5),
            view.findViewById(R.id.streak_item6),
            view.findViewById(R.id.streak_item7)
        )

        //TODO Adjust to take in streaks data

        for (i in 0 until streakIcons.size) {
            streakIcons[i]?.setImageResource(R.drawable.ic_ring)
        }

        recentFiles.adapter = RecentFilesAdaptar(requireContext(), recentFilesData)
    }


    //Testing Code for Important Dates
    private fun initializeDummyData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        datesData = listOf(
            ImportantDate("CMPT 362", today, "Milestone 3 Due", false),
            ImportantDate("CMPT 354", "2025-12-05", "Final Exam", false)
        )
    }
    //Testing Code for Files
    private fun initializeDummyFiles() {

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        recentFilesData = listOf(
            RecentFile("Milestone3_Report.pdf", today, "1.2 MB"),
            RecentFile("Lecture_Slides_Week10.pptx", "2025-11-15", "5.8 MB"),
            RecentFile("Lab08_Instructions.docx", "2025-11-14", "312 KB"),
            RecentFile("Final_Exam_Study_Guide.pdf", "2025-11-12", "850 KB")
        )
    }


}