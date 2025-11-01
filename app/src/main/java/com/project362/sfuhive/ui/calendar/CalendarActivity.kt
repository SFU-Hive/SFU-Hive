package com.project362.sfuhive.ui.calendar

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import android.widget.ImageButton

class CalendarActivity : ComponentActivity(){

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecycler: RecyclerView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        monthYearText = findViewById(R.id.tvMonthYear)
        calendarRecycler = findViewById(R.id.calendarRecycler)
        btnPrev = findViewById<ImageButton>(R.id.btnPrevMonth)
        btnNext = findViewById<ImageButton>(R.id.btnNextMonth)


        btnPrev.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            updateCalendar()
        }

        btnNext.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            updateCalendar()
        }

        updateCalendar()
    }

    private fun updateCalendar() {
        val yearMonth = YearMonth.from(selectedDate)
        val daysInMonth = yearMonth.lengthOfMonth()

        val firstOfMonth = selectedDate.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7 // shift to align Sunday first

        val days = mutableListOf<LocalDate?>()

        for (i in 1..dayOfWeek) {
            days.add(null)
        }
        for (day in 1..daysInMonth) {
            days.add(selectedDate.withDayOfMonth(day))
        }

        monthYearText.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        calendarRecycler.layoutManager = GridLayoutManager(this, 7)
        calendarRecycler.adapter = CalendarAdapter(days) { date ->
            // TODO: Later show assignments for the selected day
        }
    }
}
