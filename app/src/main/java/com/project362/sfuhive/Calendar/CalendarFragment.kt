package com.project362.sfuhive.Calendar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.calendar.model.Event
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import android.app.Activity

class CalendarFragment : Fragment() {

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

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                googleHelper.handleSignInResult(
                    GoogleCalendarHelper.RC_SIGN_IN,
                    result.data!!
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataViewModel =
            ViewModelProvider(requireActivity(), Util.getViewModelFactory(requireContext()))
                .get(DataViewModel::class.java)

        monthYearText = view.findViewById(R.id.tvMonthYear)
        selectedDateText = view.findViewById(R.id.tvSelectedDate)
        calendarRecycler = view.findViewById(R.id.calendarRecycler)
        tasksRecycler = view.findViewById(R.id.tasksRecycler)
        btnPrev = view.findViewById(R.id.btnPrevMonth)
        btnNext = view.findViewById(R.id.btnNextMonth)

        tasksRecycler.layoutManager =
            LinearLayoutManager(requireContext())
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

        /** GOOGLE SIGN IN + REFRESH **/
        googleHelper = GoogleCalendarHelper(requireActivity()) { events ->
            handleGoogleEvents(events)
        }
        googleHelper.setupGoogleSignIn()

        view.findViewById<Button>(R.id.signInButton).setOnClickListener {
            signInLauncher.launch(googleHelper.getSignInIntent())
        }

        view.findViewById<Button>(R.id.signOutButton).setOnClickListener {
            googleHelper.signOut()
            googleEventsByDate.clear()
            updateCalendar()
        }

        view.findViewById<Button>(R.id.refreshButton)?.setOnClickListener {
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
            if (account != null) {
                Toast.makeText(requireContext(), "Refreshing Google Calendar...", Toast.LENGTH_SHORT).show()
                googleHelper.fetchCalendarEvents(account)
            } else {
                Toast.makeText(requireContext(), "Please sign in with Google first.", Toast.LENGTH_SHORT).show()
            }
        }

        /** ADD TASK **/
        view.findViewById<Button>(R.id.addTaskButton).setOnClickListener {
            startActivity(Intent(requireContext(), TaskScanActivity::class.java))
        }

        updateCalendar()
    }

    /** ---------- UPDATE CALENDAR ---------- **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {
        dataViewModel.allAssignmentsLiveData.observe(viewLifecycleOwner) { assignments ->
            renderCalendar(assignments)
        }
    }

    /** ---------- RENDER CALENDAR UI ---------- **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun renderCalendar(assignments: List<Assignment>) {

        val prioritizedMap = mutableMapOf<LocalDate, List<String>>()

        fun parseDateFlexible(raw: String): LocalDate? {
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
                val intent = Intent(requireContext(), DayViewActivity::class.java)
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

        calendarRecycler.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecycler.adapter = adapter

        showAssignmentsForDay(selectedDate)
    }

    /** ---------- TASKS BELOW CALENDAR ---------- **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAssignmentsForDay(date: LocalDate) {

        selectedDateText.text = date.format(DateTimeFormatter.ofPattern("MMM dd"))

        val canvasAssignments = dataViewModel.allAssignmentsLiveData.value?.filter {
            it.dueAt.startsWith(date.toString())
        }.orEmpty()

        val googleAssignments = googleEventsByDate[date]?.map {
            Assignment(
                0L,
                "Google Calendar",
                it.summary ?: "Untitled Event",
                date.toString(),
                0.0
            )
        }.orEmpty()

        taskAdapter.update(canvasAssignments + googleAssignments)
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
        Toast.makeText(requireContext(), "Google Calendar updated!", Toast.LENGTH_SHORT).show()
    }
}
