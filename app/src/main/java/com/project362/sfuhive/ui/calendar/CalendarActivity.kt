package com.project362.sfuhive.ui.calendar

import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.AssignmentViewModel
import com.google.api.services.calendar.model.Event
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
    private lateinit var googleHelper: GoogleCalendarHelper

    // Keep Google events in memory only
    private val googleEventsByDate = mutableMapOf<LocalDate, List<Event>>()

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

        // ✅ Google Calendar Setup
        googleHelper = GoogleCalendarHelper(this) { events ->
            handleGoogleEvents(events)
        }
        googleHelper.setupGoogleSignIn()

        findViewById<Button>(R.id.signInButton).setOnClickListener {
            signInLauncher.launch(googleHelper.getSignInIntent())
        }

        findViewById<Button>(R.id.signOutButton).setOnClickListener {
            googleHelper.signOut()
            googleEventsByDate.clear()
            updateCalendar()
        }

        updateCalendar()
    }

    // ✅ Observe DB assignments and merge with Google events (in memory)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {
        assignmentViewModel.allAssignmentsLiveData.observe(this) { assignments ->
            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

            // Canvas assignments from DB
            val canvasAssignments = assignments.groupBy(
                keySelector = {
                    try { LocalDate.parse(it.dueAt, formatter) } catch (_: Exception) { null }
                },
                valueTransform = { "Assignment" }
            ).filterKeys { it != null }.mapKeys { it.key!! }

            // Merge with in-memory Google events
            val mergedAssignments = mutableMapOf<LocalDate, List<String>>()
            for ((date, list) in canvasAssignments) mergedAssignments[date] = list
            for ((date, events) in googleEventsByDate) {
                val existing = mergedAssignments[date] ?: listOf()
                mergedAssignments[date] = existing + events.map { it.summary ?: "Untitled Event" }
            }

            // Build calendar UI
            val yearMonth = YearMonth.from(selectedDate)
            val daysInMonth = yearMonth.lengthOfMonth()
            val firstDay = selectedDate.withDayOfMonth(1)
            val shift = firstDay.dayOfWeek.value % 7

            val days = MutableList(shift) { null } +
                    (1..daysInMonth).map { selectedDate.withDayOfMonth(it) }

            monthYearText.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

            val adapter = CalendarAdapter(
                days = days,
                assignmentsByDate = mergedAssignments,
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

    // ✅ Display both Canvas and Google events (not saved)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAssignmentsForDay(date: LocalDate) {
        selectedDate = date
        selectedDateText.text = date.format(DateTimeFormatter.ofPattern("MMM dd"))

        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        // Canvas (from DB)
        val canvasAssignments = assignmentViewModel.allAssignmentsLiveData.value?.filter {
            try { LocalDate.parse(it.dueAt, formatter) == date } catch (_: Exception) { false }
        }.orEmpty()

        // Google (in-memory, not stored)
        val googleAssignments = googleEventsByDate[date]?.map { event ->
            Assignment(
                assignmentId = 0L,
                courseName = "Google Calendar",
                assignmentName = event.summary ?: "Untitled Event",
                dueAt = date.toString(),
                pointsPossible = 0.0
            )
        }.orEmpty()

        // Merge both (in memory only)
        val combined = canvasAssignments + googleAssignments
        taskAdapter.update(combined)
    }

    // ✅ Handle Google sign-in results
    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                googleHelper.handleSignInResult(GoogleCalendarHelper.RC_SIGN_IN, result.data!!)
            }
        }

    // ✅ Called when Google events fetched (not persisted)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleGoogleEvents(events: List<Event>) {
        googleEventsByDate.clear()
        for (event in events) {
            val start = event.start?.dateTime ?: event.start?.date
            if (start != null) {
                val date = LocalDate.parse(start.toString().substring(0, 10))
                googleEventsByDate[date] =
                    googleEventsByDate.getOrDefault(date, listOf()) + event
            }
        }
        updateCalendar()
    }
}
