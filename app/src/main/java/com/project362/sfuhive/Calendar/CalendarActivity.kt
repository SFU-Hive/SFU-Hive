package com.project362.sfuhive.Calendar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import com.google.api.services.calendar.model.Event
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarActivity : FragmentActivity() {

    private lateinit var monthYearText: TextView
    private lateinit var selectedDateText: TextView
    private lateinit var calendarRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var tasksRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var dataViewModel: DataViewModel
    private lateinit var googleHelper: GoogleCalendarHelper

    private val googleEventsByDate = mutableMapOf<LocalDate, List<Event>>()

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        dataViewModel =
            ViewModelProvider(this, Util.getViewModelFactory(this))
                .get(DataViewModel::class.java)

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

        findViewById<Button>(R.id.refreshButton)?.setOnClickListener {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                Toast.makeText(this, "Refreshing Google Calendar...", Toast.LENGTH_SHORT).show()
                googleHelper.fetchCalendarEvents(account)
            } else {
                Toast.makeText(this, "Please sign in with Google first.", Toast.LENGTH_SHORT).show()
            }
        }

        /** ADD TASK BUTTON **/
        findViewById<Button>(R.id.addTaskButton).setOnClickListener {
            val intent = Intent(this, TaskScanActivity::class.java)
            startActivity(intent)
        }

        updateCalendar()
    }

    /** ---------- SAVE CUSTOM TASK ---------- **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveNewTask(title: String, date: String, start: String, end: String) {
        val newAssignment = Assignment(
            assignmentId = 0L,
            courseName = "Custom Task",
            assignmentName = title,
            dueAt = "${date}T$start",
            pointsPossible = 0.0
        )

        dataViewModel.insertAssignment(newAssignment)

        Toast.makeText(this, "Task Added!", Toast.LENGTH_SHORT).show()

        updateCalendar()
        showAssignmentsForDay(LocalDate.parse(date))
    }

    /** ---------- FLEXIBLE DATE PARSER ---------- **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseDateFlexible(raw: String): LocalDate? {
        return try {
            LocalDate.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (_: Exception) {
            try {
                LocalDate.parse(raw.substring(0, 10))
            } catch (_: Exception) {
                null
            }
        }
    }

    /** ---------- UPDATE CALENDAR UI ---------- **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {

        dataViewModel.allAssignmentsLiveData.observe(this) { assignments ->

            val prioritizedMap = mutableMapOf<LocalDate, List<String>>()

            for (assignment in assignments) {
                val date = parseDateFlexible(assignment.dueAt)
                if (date != null) {
                    val diff = date.toEpochDay() - selectedDate.toEpochDay()

                    val priority = when (diff) {
                        0L -> "high"
                        -1L, 1L -> "medium"
                        else -> "low"
                    }

                    val existing = prioritizedMap[date] ?: listOf()
                    prioritizedMap[date] = existing + priority
                }
            }

            for ((date, events) in googleEventsByDate) {
                val diff = date.toEpochDay() - selectedDate.toEpochDay()
                val priority = when (diff) {
                    0L -> "high"
                    -1L, 1L -> "medium"
                    else -> "low"
                }
                val existing = prioritizedMap[date] ?: listOf()
                prioritizedMap[date] = existing + List(events.size) { priority }
            }

            val yearMonth = YearMonth.from(selectedDate)
            val daysInMonth = yearMonth.lengthOfMonth()
            val firstDay = selectedDate.withDayOfMonth(1)
            val shift = firstDay.dayOfWeek.value % 7

            val days = MutableList(shift) { null } +
                    (1..daysInMonth).map { selectedDate.withDayOfMonth(it) }

            monthYearText.text =
                selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

            val adapter = CalendarAdapter(
                days = days,
                assignmentsByDate = prioritizedMap,
                selectedDate = selectedDate
            ) { date ->

                if (date == selectedDate) {
                    val intent = Intent(this, DayViewActivity::class.java)
                    intent.putExtra("selected_date", date.toString())
                    GoogleEventCache.events[date.toString()] =
                        googleEventsByDate[date] ?: listOf()
                    startActivity(intent)
                } else {
                    selectedDate = date
                    (calendarRecycler.adapter as CalendarAdapter).updateSelectedDate(date)
                    showAssignmentsForDay(date)
                }
            }

            calendarRecycler.layoutManager = GridLayoutManager(this, 7)
            calendarRecycler.adapter = adapter

            showAssignmentsForDay(selectedDate)
        }
    }

    /** ---------- SHOW TASKS BELOW CALENDAR ---------- **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAssignmentsForDay(date: LocalDate) {
        selectedDateText.text = date.format(DateTimeFormatter.ofPattern("MMM dd"))

        val canvasAssignments = dataViewModel.allAssignmentsLiveData.value?.filter {
            parseDateFlexible(it.dueAt) == date
        }.orEmpty()

        val googleAssignments = googleEventsByDate[date]?.map { event ->
            Assignment(
                assignmentId = 0L,
                courseName = "Google Calendar",
                assignmentName = event.summary ?: "Untitled Event",
                dueAt = date.toString(),
                pointsPossible = 0.0
            )
        }.orEmpty()

        val combined = canvasAssignments + googleAssignments
        taskAdapter.update(combined)
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                googleHelper.handleSignInResult(
                    GoogleCalendarHelper.RC_SIGN_IN,
                    result.data!!
                )
            }
        }

    /** ---------- GOOGLE EVENTS ---------- **/
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
        Toast.makeText(this, "Google Calendar updated!", Toast.LENGTH_SHORT).show()
    }
}
