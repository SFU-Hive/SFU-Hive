package com.project362.sfuhive.Calendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.project362.sfuhive.R
import com.project362.sfuhive.Calendar.CalendarActivity

class CalendarFragment : Fragment() {

    private lateinit var calendarButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        //added for calendar
        calendarButton = view.findViewById(R.id.btnCalendar)

        calendarButton.setOnClickListener {
            val intent = Intent(requireContext(), CalendarActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}