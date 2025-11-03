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
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.AssignmentViewModel
import com.project362.sfuhive.database.AssignmentViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarActivity : ComponentActivity() {

    private lateinit var assignmentViewModel: AssignmentViewModel

    private lateinit var monthYearText: TextView
    private lateinit var selectedDateText: TextView
    private lateinit var calendarRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var tasksRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    private lateinit var taskAdapter: TaskAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // ✅ ViewModel
        assignmentViewModel = ViewModelProvider(
            this,
            Util.getViewModelFactory(this)
        ).get(AssignmentViewModel::class.java)

        // ✅ Bind UI
        monthYearText = findViewById(R.id.tvMonthYear)
        selectedDateText = findViewById(R.id.tvSelectedDate)
        calendarRecycler = findViewById(R.id.calendarRecycler)
        tasksRecycler = findViewById(R.id.tasksRecycler)
        btnPrev = findViewById(R.id.btnPrevMonth)
        btnNext = findViewById(R.id.btnNextMonth)

        // ✅ Tasks List Setup
        tasksRecycler.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(listOf())
        tasksRecycler.adapter = taskAdapter

        btnPrev.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            populateCalendar()
        }

        btnNext.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            populateCalendar()
        }

        // ✅ Observe ONLY ONCE
        assignmentViewModel.allAssignmentsLiveData.observe(this) {
            populateCalendar()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun populateCalendar() {
        val assignments = assignmentViewModel.allAssignmentsLiveData.value ?: return

        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        // ✅ Group assignments by date
        val assignmentMap: Map<LocalDate, List<Assignment>> =
            assignments.mapNotNull { item ->
                try {
                    Pair(LocalDate.parse(item.dueAt, formatter), item)
                } catch (_: Exception) {
                    null
                }
            }.groupBy({ it.first }, { it.second })

        val yearMonth = YearMonth.from(selectedDate)
        val daysInMonth = yearMonth.lengthOfMonth()

        val firstDay = selectedDate.withDayOfMonth(1)
        val shift = firstDay.dayOfWeek.value % 7

        val days: List<LocalDate?> =
            List(shift) { null } + (1..daysInMonth).map {
                selectedDate.withDayOfMonth(it)
            }

        monthYearText.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        calendarRecycler.layoutManager = GridLayoutManager(this, 7)
        calendarRecycler.adapter =
            CalendarAdapter(
                days,
                assignmentMap.mapValues { listOf("high") }, // fake "priority" strings
                selectedDate
            ) { clickDate ->
                showAssignmentsForDay(clickDate)
            }

        showAssignmentsForDay(selectedDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAssignmentsForDay(date: LocalDate) {
        selectedDate = date
        selectedDateText.text = date.format(DateTimeFormatter.ofPattern("MMM dd"))

        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val assignmentsToday = assignmentViewModel.allAssignmentsLiveData.value?.filter {
            try {
                LocalDate.parse(it.dueAt, formatter) == date
            } catch (_: Exception) {
                false
            }
        }.orEmpty()

        taskAdapter.update(assignmentsToday)
    }
}
