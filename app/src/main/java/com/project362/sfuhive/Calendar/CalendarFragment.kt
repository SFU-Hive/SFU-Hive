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

    private val googleEventsByDate = mutableMapOf<LocalDate, List<Event>>()
    private var canvasAssignments: List<Assignment> = emptyList()
    private var customTasks: List<CustomTaskEntity> = emptyList()

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
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataViewModel =
            ViewModelProvider(requireActivity(), Util.getViewModelFactory(requireContext()))
                .get(DataViewModel::class.java)

        val customDb = CustomTaskDatabase.getInstance(requireContext())
        val customRepo = CustomTaskRepository(customDb.customDao())
        customVM = ViewModelProvider(requireActivity(), CustomTaskVMFactory(customRepo))
            .get(CustomTaskViewModel::class.java)


        // 🔹 Observe Canvas assignments
        dataViewModel.allMyAssignmentsLiveData.observe(viewLifecycleOwner) { list ->
            canvasAssignments = list
            updateCalendar()
        }

        // 🔹 Observe Custom tasks
        customVM.allTasks.observe(viewLifecycleOwner) { list ->
            customTasks = list
            updateCalendar()
        }

        // 🔹 UI setup
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
            // 🔥 Auto-refresh calendar dots when priority changes
            updateCalendar()
        }
        tasksRecycler.adapter = taskAdapter

        calendarAdapter = CalendarAdapter(
            days = mutableListOf(),
            assignmentsByDate = mutableMapOf(),
            selectedDate = selectedDate
        ) { date ->
            if (date == selectedDate) {
                // 👉 Open Day View on second click
                openDayView(date)
            } else {
                // 👉 First click just selects
                selectedDate = date
                calendarAdapter.updateSelectedDate(date)
                showAssignmentsForDay(date)
            }
        }

        calendarRecycler.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecycler.adapter = calendarAdapter

        btnPrev.setOnClickListener { selectedDate = selectedDate.minusMonths(1); updateCalendar() }
        btnNext.setOnClickListener { selectedDate = selectedDate.plusMonths(1); updateCalendar() }

        googleHelper = GoogleCalendarHelper(requireActivity()) { handleGoogleEvents(it) }
        googleHelper.setupGoogleSignIn()

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

        fabAddEvent.setOnClickListener {
            startActivity(Intent(requireContext(), TaskScanActivity::class.java))
        }

        // Canvas
        dataViewModel.allMyAssignmentsLiveData.observe(viewLifecycleOwner) {

            canvasAssignments = it
            updateCalendar()
        }

        customVM.allTasks.observe(viewLifecycleOwner) {
            customTasks = it
            updateCalendar()
        }

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

        val ym = YearMonth.from(selectedDate)
        val first = selectedDate.withDayOfMonth(1)
        val shift = first.dayOfWeek.value % 7

        val days = MutableList(shift) { null } +
                (1..ym.lengthOfMonth()).map { selectedDate.withDayOfMonth(it) }

        monthYearText.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        calendarAdapter.setDays(days)
        calendarAdapter.updateAssignments(merged)
        calendarAdapter.updateSelectedDate(selectedDate)

        showAssignmentsForDay(selectedDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAssignmentsForDay(date: LocalDate) {

        selectedDateText.text = date.format(DateTimeFormatter.ofPattern("MMM dd"))


        val assignmentsForDay = mutableListOf<Assignment>()
        val priorityIds = mutableListOf<String>()

        // Canvas
        canvasAssignments.filter { it.dueAt.startsWith(date.toString()) }.forEach {
            assignmentsForDay.add(it)
            priorityIds.add(it.assignmentId.toString())
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
                    pointsPossible = 0.0
                )
            )
            priorityIds.add(id)
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
                    pointsPossible = 0.0
                )
            )
            priorityIds.add(id)
        }

        taskAdapter.update(assignmentsForDay, priorityIds)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleGoogleEvents(events: List<Event>) {
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
}
