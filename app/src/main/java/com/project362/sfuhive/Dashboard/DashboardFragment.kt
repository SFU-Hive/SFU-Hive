package com.project362.sfuhive.Dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.project362.sfuhive.R

class DashboardFragment : Fragment() {

    data class ImportantDate(
        val name: String,
        val date: String,
        val task: String,
        val isComplete: Boolean
    )

    val datesData = listOf(
        ImportantDate("CMPT 362", "November 6", "Homework", false),
        ImportantDate("CMPT 362", "November 13", "Homework", false),
        ImportantDate("CMPT 362", "November 20", "Homework", false),
        )


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val importantDates = view.findViewById<ListView>(R.id.list_view)
        importantDates.adapter = ImportantDateAdapter(requireContext(), datesData)



        val streakIcons = view.findViewById<ImageView>(R.id.streak_checkmark1)
        streakIcons.setImageResource(R.drawable.ic_checkmark)

        val streakIcons2 = view.findViewById<ImageView>(R.id.streak_checkmark2)
        streakIcons2.setImageResource(R.drawable.ic_checkmark)

        val streakIcons3 = view.findViewById<ImageView>(R.id.streak_cross1)
        streakIcons3.setImageResource(R.drawable.ic_cross)

        val streakIcons4 = view.findViewById<ImageView>(R.id.streak_cross2)
        streakIcons4.setImageResource(R.drawable.ic_cross)

        val streakIcons5 = view.findViewById<ImageView>(R.id.streak_checkmark3)
        streakIcons5.setImageResource(R.drawable.ic_checkmark)

        val streakIcons6 = view.findViewById<ImageView>(R.id.streak_ring1)
        streakIcons6.setImageResource(R.drawable.ic_ring)

        val streakIcons7 = view.findViewById<ImageView>(R.id.streak_ring2)
        streakIcons7.setImageResource(R.drawable.ic_ring)

    }
}