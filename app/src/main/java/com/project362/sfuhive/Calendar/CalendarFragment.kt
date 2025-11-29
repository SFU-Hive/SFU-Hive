package com.project362.sfuhive.Calendar

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Suppress("NewApi") // suppress LocalDate API 26 warnings for minSdk 24
class CalendarFragment : Fragment() {

    private lateinit var monthYearText: TextView
    private lateinit var selectedDateText: TextView
    private lateinit var calendarRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var tasksRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnOverflow: ImageButton
    private lateinit var fabAddEvent: FloatingActionButton

    private lateinit var dataViewModel: DataViewModel
    private lateinit var googleHelper: GoogleCalendarHelper

    private val googleEventsByDate = mutableMapOf<LocalDate, List<Event>>()

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

        /** ViewModel **/
        dataViewModel =
            ViewModelProvider(requireActivity(), Util.getViewModelFactory(requireContext()))
                .get(DataViewModel::class.java)

        /** UI refs **/
        monthYearText = view.findViewById(R.id.tvMonthYear)
        selectedDateText = view.findViewById(R.id.tvSelectedDate)
        calendarRecycler = view.findViewById(R.id.calendarRecycler)
        tasksRecycler = view.findViewById(R.id.tasksRecycler)

        btnPrev = view.findViewById(R.id.btnPrevMonth)
        btnNext = view.findViewById(R.id.btnNextMonth)
        btnOverflow = view.findViewById(R.id.btnOverflow)
        fabAddEvent = view.findViewById(R.id.fabAddEvent)

        /** Recycler setup **/
        tasksRecycler.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(listOf())
        tasksRecycler.adapter = taskAdapter

        /** Month Navigation **/
        btnPrev.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            updateCalendar()
        }

        btnNext.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            updateCalendar()
        }

        /** GOOGLE SIGN-IN + REFRESH **/
        googleHelper = GoogleCalendarHelper(requireActivity()) { events ->
            handleGoogleEvents(events)
        }
        googleHelper.setupGoogleSignIn()

        /** OVERFLOW MENU (Sign-in, Refresh, Sign-out) **/
        btnOverflow.setOnClickListener {
            val popup = PopupMenu(requireContext(), btnOverflow)
            popup.menuInflater.inflate(R.menu.calendar_menu, popup.menu)

            // NEW: Dynamic visibility
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
            popup.menu.findItem(R.id.menu_sign_in).isVisible = (account == null)
            popup.menu.findItem(R.id.menu_sign_out).isVisible = (account != null)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_sign_in -> {
                        signInLauncher.launch(googleHelper.getSignInIntent())
                    }

                    R.id.menu_refresh -> {
                        val acc = GoogleSignIn.getLastSignedInAccount(requireContext())
                        if (acc != null) {
                            googleHelper.fetchCalendarEvents(acc)
                        } else {
                            Toast.makeText(requireContext(), "Please sign in first.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    R.id.menu_sign_out -> {
                        googleHelper.signOut()
                        googleEventsByDate.clear()
                        updateCalendar()
                        Toast.makeText(requireContext(), "Signed out.", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            popup.show()
        }

        /** FAB: Add Canvas Task **/
        fabAddEvent.setOnClickListener {
            startActivity(Intent(requireContext(), TaskScanActivity::class.java))
        }

        /** Draw initial calendar **/
        updateCalendar()
    }

    /** ---------- UPDATE CALENDAR ---------- **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {
        dataViewModel.allMyAssignmentsLiveData.observe(viewLifecycleOwner) { assignments ->
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

        /** Canvas tasks **/
        for (assignment in assignments) {
            val date = parseDateFlexible(assignment.dueAt)
            if (date != null) {
                val diff = date.toEpochDay() - selectedDate.toEpochDay()
                val priority = when (diff) {
                    0L -> "high"
                    -1L, 1L -> "medium"
                    else -> "low"
                }
                val current = prioritizedMap[date] ?: listOf()
                prioritizedMap[date] = current + priority
            }
        }

        /** Google Events **/
        for ((date, events) in googleEventsByDate) {
            val diff = date.toEpochDay() - selectedDate.toEpochDay()
            val priority = when (diff) {
                0L -> "high"
                -1L, 1L -> "medium"
                else -> "low"
            }
            val current = prioritizedMap[date] ?: listOf()
            prioritizedMap[date] = current + List(events.size) { priority }
        }

        /** Build Month Grid **/
        val yearMonth = YearMonth.from(selectedDate)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDay = selectedDate.withDayOfMonth(1)
        val shift = firstDay.dayOfWeek.value % 7

        val days = MutableList(shift) { null } +
                (1..daysInMonth).map { selectedDate.withDayOfMonth(it) }

        monthYearText.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

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

        val canvasAssignments = dataViewModel.allMyAssignmentsLiveData.value?.filter {
            it.dueAt.startsWith(date.toString())
        }.orEmpty()

        val googleAssignments = googleEventsByDate[date]?.map {
            Assignment(
                assignmentId = 0L,
                courseName = "Google Calendar",
                assignmentName = it.summary ?: "Untitled Event",
                dueAt = date.toString(),
                pointsPossible = 0.0
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
