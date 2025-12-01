package com.project362.sfuhive.Calendar

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.Calendar.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


// Hosts the main month view of the app.

@Suppress("NewApi")
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
    private lateinit var customVM: CustomTaskViewModel

    private lateinit var calendarAdapter: CalendarAdapter

    // Local caches used when building the calendar UI
    private val googleEventsByDate = mutableMapOf<LocalDate, List<Event>>()
    private var canvasAssignments: List<Assignment> = emptyList()
    private var customTasks: List<CustomTaskEntity> = emptyList()

    private var selectedDate: LocalDate = LocalDate.now()

    // Launcher to handle Google sign-in flow; result forwarded to GoogleCalendarHelper
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
        // Let this fragment contribute to the options menu
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtain shared ViewModel which supplies Canvas assignments
        dataViewModel =
            ViewModelProvider(requireActivity(), Util.getViewModelFactory(requireContext()))
                .get(DataViewModel::class.java)

        // Setup local custom tasks ViewModel and repository used for user-created tasks
        val customDb = CustomTaskDatabase.getInstance(requireContext())
        val customRepo = CustomTaskRepository(customDb.customDao())
        customVM = ViewModelProvider(requireActivity(), CustomTaskVMFactory(customRepo))
            .get(CustomTaskViewModel::class.java)


        // Observe Canvas assignments; update local cache and refresh calendar
        dataViewModel.allMyAssignmentsLiveData.observe(viewLifecycleOwner) { list ->
            canvasAssignments = list
            updateCalendar()
        }

        // Observe Custom tasks; update local cache and refresh calendar
        customVM.allTasks.observe(viewLifecycleOwner) { list ->
            customTasks = list
            updateCalendar()
        }

        // UI wiring; find views and configure RecyclerViews
        monthYearText = view.findViewById(R.id.tvMonthYear)
        selectedDateText = view.findViewById(R.id.tvSelectedDate)
        calendarRecycler = view.findViewById(R.id.calendarRecycler)
        tasksRecycler = view.findViewById(R.id.tasksRecycler)
        btnPrev = view.findViewById(R.id.btnPrevMonth)
        btnNext = view.findViewById(R.id.btnNextMonth)
        btnOverflow = view.findViewById(R.id.btnOverflow)
        fabAddEvent = view.findViewById(R.id.fabAddEvent)

        tasksRecycler.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(
            emptyList(),
            emptyList()
        ) {
            // Auto-refresh calendar dots when priority changes
            updateCalendar()
        }
        tasksRecycler.adapter = taskAdapter

        calendarAdapter = CalendarAdapter(
            days = mutableListOf(),
            assignmentsByDate = mutableMapOf(),
            selectedDate = selectedDate
        ) { date ->
            if (date == selectedDate) {
                // Open Day View on second click
                openDayView(date)
            } else {
                // First click just selects and updates the assignment list for that day
                selectedDate = date
                calendarAdapter.updateSelectedDate(date)
                showAssignmentsForDay(date)
            }
        }

        calendarRecycler.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecycler.adapter = calendarAdapter

        // Month navigation
        btnPrev.setOnClickListener { selectedDate = selectedDate.minusMonths(1); updateCalendar() }
        btnNext.setOnClickListener { selectedDate = selectedDate.plusMonths(1); updateCalendar() }

        // Google calendar helper handles sign-in and fetching events
        googleHelper = GoogleCalendarHelper(requireActivity()) { handleGoogleEvents(it) }
        googleHelper.setupGoogleSignIn()

        // Overflow menu (sign-in / refresh / sign-out)
        btnOverflow.setOnClickListener { v ->
            val popup = PopupMenu(requireContext(), v)
            popup.menuInflater.inflate(R.menu.calendar_menu, popup.menu)

            lifecycleScope.launch {
                val dao = GoogleEventDatabase.getInstance(requireContext()).googleEventDao()
                val hasCached = withContext(Dispatchers.IO) { dao.getEventCount() > 0 }

                val signedIn = googleHelper.isSignedIn() || hasCached

                popup.menu.findItem(R.id.menu_sign_in).isVisible = !signedIn
                popup.menu.findItem(R.id.menu_refresh).isVisible = true
                popup.menu.findItem(R.id.menu_sign_out).isVisible = signedIn
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_sign_in -> {
                        signInLauncher.launch(googleHelper.getSignInIntent())
                        true
                    }

                    R.id.menu_refresh -> {
                        googleHelper.refreshEvents()
                        true
                    }

                    R.id.menu_sign_out -> {
                        googleHelper.signOut()
                        lifecycleScope.launch(Dispatchers.IO) {
                            GoogleEventDatabase.getInstance(requireContext()).googleEventDao()
                                .deleteAllEvents()
                        }
                        googleEventsByDate.clear()
                        updateCalendar()
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }

        // Floating action to create a scanned task
        fabAddEvent.setOnClickListener {
            startActivity(Intent(requireContext(), TaskScanActivity::class.java))
        }

        // Re-observe in case other lifecycle owners changed data while fragment was inactive
        dataViewModel.allMyAssignmentsLiveData.observe(viewLifecycleOwner) {

            canvasAssignments = it
            updateCalendar()
        }

        customVM.allTasks.observe(viewLifecycleOwner) {
            customTasks = it
            updateCalendar()
        }

        // Load any cached Google events from local DB on a background thread and merge them
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = GoogleEventDatabase.getInstance(requireContext()).googleEventDao()
            val saved = dao.getAllEvents()

            withContext(Dispatchers.Main) {
                googleEventsByDate.clear()

                saved.forEach { e ->
                    val date = LocalDate.parse(e.date)
                    val event = Event().apply {
                        id = e.eventId
                        summary = e.title
                        start = EventDateTime().setDate(DateTime("${e.date}T00:00:00Z"))
                    }
                    googleEventsByDate[date] =
                        googleEventsByDate.getOrDefault(date, emptyList()) + event
                }
                updateCalendar()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {

        // Build a unified map that assigns a priority ID to each date
        val merged = mutableMapOf<LocalDate, MutableList<String>>()

        fun put(date: LocalDate, id: String) {
            merged.getOrPut(date) { mutableListOf() }.add(id)
        }

        // CANVAS — store REAL assignmentId
        canvasAssignments.forEach {
            if (it.dueAt.length >= 10) {
                val d = LocalDate.parse(it.dueAt.substring(0, 10))
                put(d, it.assignmentId.toString())
            }
        }

        // GOOGLE — store REAL event priority ID
        googleEventsByDate.forEach { (d, events) ->
            events.forEach { e ->
                val id = "google_${e.id}"
                put(d, id)
            }
        }

        // CUSTOM — store REAL task priority ID
        customTasks.forEach {
            if (it.date.length == 10) {
                val d = LocalDate.parse(it.date)
                val id = "custom_${it.id}"
                put(d, id)
            }
        }

        // Month and day grid calculation
        val ym = YearMonth.from(selectedDate)
        val first = selectedDate.withDayOfMonth(1)
        val shift = first.dayOfWeek.value % 7

        val days = MutableList(shift) { null } +
                (1..ym.lengthOfMonth()).map { selectedDate.withDayOfMonth(it) }

        monthYearText.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        calendarAdapter.setDays(days)
        calendarAdapter.updateAssignments(merged)
        calendarAdapter.updateSelectedDate(selectedDate)

        // Update the list of assignments for the currently selected day
        showAssignmentsForDay(selectedDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAssignmentsForDay(date: LocalDate) {

        // Update the header showing the selected date
        selectedDateText.text = date.format(DateTimeFormatter.ofPattern("MMM dd"))


        val assignmentsForDay = mutableListOf<Assignment>()
        val priorityIds = mutableListOf<String>()

        // Canvas assignments for this day
        canvasAssignments.filter { it.dueAt.startsWith(date.toString()) }.forEach {
            assignmentsForDay.add(it)
            priorityIds.add(it.assignmentId.toString())
        }

        // Google events for this day — map to Assignment model for display only
        googleEventsByDate[date]?.forEach { e ->
            val id = "google_${e.id}"
            assignmentsForDay.add(
                Assignment(
                    assignmentId = id.hashCode().toLong(),
                    courseName = "Google Calendar",
                    assignmentName = e.summary ?: "Untitled Event",
                    dueAt = date.toString(),
                    pointsPossible = 0.0
                )
            )
            priorityIds.add(id)
        }

        // Custom user tasks — convert to Assignment for display
        customTasks.filter { it.date == date.toString() }.forEach {
            val id = "custom_${it.id}"
            assignmentsForDay.add(
                Assignment(
                    assignmentId = id.hashCode().toLong(),
                    courseName = "Task",
                    assignmentName = it.title,
                    dueAt = it.date,
                    pointsPossible = 0.0
                )
            )
            priorityIds.add(id)
        }

        // Update the task adapter with items and their priority IDs used by EventPriority DB
        taskAdapter.update(assignmentsForDay, priorityIds)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleGoogleEvents(events: List<Event>) {
        // Called after Google events are fetched; merge them into local cache and refresh
        googleEventsByDate.clear()

        events.forEach { e ->
            val start = e.start?.dateTime ?: e.start?.date ?: return@forEach
            val d = LocalDate.parse(start.toString().substring(0, 10))
            googleEventsByDate[d] =
                googleEventsByDate.getOrDefault(d, emptyList()) + e
        }

        updateCalendar()
        Toast.makeText(requireContext(), "Google Calendar updated!", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.search_menu, menu)

        return
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun openDayView(date: LocalDate) {

        val assignmentsForDay = mutableListOf<Assignment>()
        val priorityIds = mutableListOf<String>()
        val pointsList = mutableListOf<Double>()
        val groupList = mutableListOf<Double>()

        // Build payload for DayViewActivity
        // Canvas
        canvasAssignments.filter { it.dueAt.startsWith(date.toString()) }.forEach {
            assignmentsForDay.add(it)
            priorityIds.add(it.assignmentId.toString())
            pointsList.add(it.pointsPossible)
            groupList.add(it.groupWeight)
        }

        // Google
        googleEventsByDate[date]?.forEach { e ->
            val id = "google_${e.id}"
            assignmentsForDay.add(
                Assignment(
                    assignmentId = id.hashCode().toLong(),
                    courseName = "Google Calendar",
                    assignmentName = e.summary ?: "Untitled Event",
                    dueAt = date.toString(),
                    pointsPossible = 0.0,
                    groupWeight = 0.0
                )
            )
            priorityIds.add(id)
            pointsList.add(0.0)
            groupList.add(0.0)
        }

        // Custom
        customTasks.filter { it.date == date.toString() }.forEach {
            val id = "custom_${it.id}"
            assignmentsForDay.add(
                Assignment(
                    assignmentId = id.hashCode().toLong(),
                    courseName = "Task",
                    assignmentName = it.title,
                    dueAt = it.date,
                    pointsPossible = 0.0,
                    groupWeight = 0.0
                )
            )
            priorityIds.add(id)
            pointsList.add(0.0)
            groupList.add(0.0)
        }

        // Send end to day view activty
        val intent = Intent(requireContext(), DayViewActivity::class.java)
        intent.putExtra("selected_date", date.toString())

        intent.putStringArrayListExtra("task_titles", ArrayList(assignmentsForDay.map { it.assignmentName }))
        intent.putStringArrayListExtra("task_courses", ArrayList(assignmentsForDay.map { it.courseName }))
        intent.putStringArrayListExtra("task_dates", ArrayList(assignmentsForDay.map { it.dueAt }))
        intent.putStringArrayListExtra("task_priority_ids", ArrayList(priorityIds))
        intent.putExtra("task_points", pointsList.toDoubleArray())
        intent.putExtra("task_groups", groupList.toDoubleArray())

        startActivity(intent)
    }

}
