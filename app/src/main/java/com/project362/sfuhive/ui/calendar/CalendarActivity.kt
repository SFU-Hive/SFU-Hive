package com.project362.sfuhive.ui.calendar

import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.AssignmentViewModel
import com.project362.sfuhive.database.AssignmentViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarActivity : ComponentActivity() {

    private lateinit var monthYearText: TextView
    private lateinit var selectedDateText: TextView
    private lateinit var calendarRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var tasksRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var assignmentViewModel: AssignmentViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // ✅ ViewModel Setup
        assignmentViewModel =
            ViewModelProvider(this, Util.getViewModelFactory(this))
                .get(AssignmentViewModel::class.java)

        // ✅ UI References
        monthYearText = findViewById(R.id.tvMonthYear)
        selectedDateText = findViewById(R.id.tvSelectedDate)
        calendarRecycler = findViewById(R.id.calendarRecycler)
        tasksRecycler = findViewById(R.id.tasksRecycler)
        btnPrev = findViewById(R.id.btnPrevMonth)
        btnNext = findViewById(R.id.btnNextMonth)

        tasksRecycler.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(listOf())
        tasksRecycler.adapter = taskAdapter

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {
        assignmentViewModel.allAssignmentsLiveData.observe(this) { assignments ->

            // Group assignments by due date with priority list
            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val assignmentsByDate = assignments.groupBy(
                keySelector = {
                    try { LocalDate.parse(it.dueAt, formatter) } catch (_: Exception) { null }
                },
                valueTransform = { "high" } // placeholder priority for now
            ).filterKeys { it != null }.mapKeys { it.key!! }

            val yearMonth = YearMonth.from(selectedDate)
            val daysInMonth = yearMonth.lengthOfMonth()
            val firstDay = selectedDate.withDayOfMonth(1)
            val shift = firstDay.dayOfWeek.value % 7

            val days = MutableList(shift) { null } +
                    (1..daysInMonth).map { selectedDate.withDayOfMonth(it) }

            monthYearText.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

            val adapter = CalendarAdapter(
                days = days,
                assignmentsByDate = assignmentsByDate,
                selectedDate = selectedDate
            ) { date ->
                selectedDate = date
                (calendarRecycler.adapter as CalendarAdapter).updateSelectedDate(date)
                showAssignmentsForDay(date)
            }

            calendarRecycler.layoutManager = GridLayoutManager(this, 7)
            calendarRecycler.adapter = adapter

            showAssignmentsForDay(selectedDate)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAssignmentsForDay(date: LocalDate) {
        selectedDate = date
        selectedDateText.text = date.format(DateTimeFormatter.ofPattern("MMM dd"))

        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val filteredList = assignmentViewModel.allAssignmentsLiveData.value?.filter {
            try { LocalDate.parse(it.dueAt, formatter) == date }
            catch (_: Exception) { false }
        }.orEmpty()

        taskAdapter.update(filteredList)
    }
}
