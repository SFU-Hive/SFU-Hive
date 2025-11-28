package com.project362.sfuhive.Calendar

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.Calendar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val googleEventsByDate = mutableMapOf<LocalDate, List<Event>>()
    private var customTasks: List<CustomTaskEntity> = emptyList()
    private var canvasAssignments: List<Assignment> = emptyList()
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
    ): View = inflater.inflate(R.layout.fragment_calendar, container, false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 Setup ViewModels
        dataViewModel = ViewModelProvider(requireActivity(), Util.getViewModelFactory(requireContext()))
            .get(DataViewModel::class.java)

        val customDb = CustomTaskDatabase.getInstance(requireContext())
        val customRepo = CustomTaskRepository(customDb.customDao())
        customVM = ViewModelProvider(this, CustomTaskVMFactory(customRepo))
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
        taskAdapter = TaskAdapter(listOf())
        tasksRecycler.adapter = taskAdapter

        btnPrev.setOnClickListener { selectedDate = selectedDate.minusMonths(1); updateCalendar() }
        btnNext.setOnClickListener { selectedDate = selectedDate.plusMonths(1); updateCalendar() }

        googleHelper = GoogleCalendarHelper(requireActivity()) { handleGoogleEvents(it) }
        googleHelper.setupGoogleSignIn()

        btnOverflow.setOnClickListener { showOverflowMenu() }

        fabAddEvent.setOnClickListener {
            startActivity(Intent(requireContext(), TaskScanActivity::class.java))
        }

        // 🔹 Load saved Google events
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = GoogleEventDatabase.getInstance(requireContext()).googleEventDao()
            val saved = dao.getAllEvents()
            withContext(Dispatchers.Main) {
                googleEventsByDate.clear()
                for (e in saved) {
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

    private fun showOverflowMenu() {
        val popup = PopupMenu(requireContext(), btnOverflow)
        popup.menuInflater.inflate(R.menu.calendar_menu, popup.menu)
        val acc = GoogleSignIn.getLastSignedInAccount(requireContext())
        popup.menu.findItem(R.id.menu_sign_in).isVisible = (acc == null)
        popup.menu.findItem(R.id.menu_sign_out).isVisible = (acc != null)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_sign_in -> signInLauncher.launch(googleHelper.getSignInIntent())
                R.id.menu_refresh -> {
                    val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                    if (account != null) googleHelper.fetchCalendarEvents(account)
                    else Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show()
                }
                R.id.menu_sign_out -> {
                    googleHelper.signOut()
                    googleEventsByDate.clear()
                    lifecycleScope.launch(Dispatchers.IO) {
                        GoogleEventDatabase.getInstance(requireContext()).googleEventDao().deleteAllEvents()
                    }
                    updateCalendar()
                    Toast.makeText(requireContext(), "Signed out.", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar() {
        renderCalendar(canvasAssignments)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun renderCalendar(assignments: List<Assignment>) {
        val map = mutableMapOf<LocalDate, List<String>>()

        fun add(date: LocalDate, priority: String) {
            map[date] = (map[date] ?: listOf()) + priority
        }

        // ✅ Canvas Assignments (blue = low)
        for (a in assignments) {
            if (a.dueAt.isNullOrEmpty() || a.dueAt.length < 10) continue
            val dateStr = a.dueAt.substring(0, 10)
            val date = try { LocalDate.parse(dateStr) } catch (_: Exception) { null }
            date?.let { add(it, "low") }
        }

        // ✅ Google Events (orange = medium)
        for ((d, _) in googleEventsByDate) add(d, "medium")

        // ✅ Custom Tasks (red = high)
        for (t in customTasks) {
            val d = try { LocalDate.parse(t.date) } catch (_: Exception) { null }
            d?.let { add(it, "high") }
        }

        // ✅ Build Calendar Grid
        val ym = YearMonth.from(selectedDate)
        val first = selectedDate.withDayOfMonth(1)
        val shift = first.dayOfWeek.value % 7
        val days = MutableList(shift) { null } + (1..ym.lengthOfMonth()).map { selectedDate.withDayOfMonth(it) }

        monthYearText.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        calendarRecycler.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecycler.adapter = CalendarAdapter(days, map, selectedDate) { date ->
            selectedDate = date
            (calendarRecycler.adapter as CalendarAdapter).updateSelectedDate(date)
            showAssignmentsForDay(date)
        }

        showAssignmentsForDay(selectedDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAssignmentsForDay(date: LocalDate) {
        selectedDateText.text = date.format(DateTimeFormatter.ofPattern("MMM dd"))


        val canvas = canvasAssignments.filter {
            it.dueAt.startsWith(date.toString())
        }

        val google = googleEventsByDate[date]?.map {
            Assignment(0L, "Google Calendar", it.summary ?: "Untitled Event", date.toString(), 0.0)
        }.orEmpty()

        val custom = customTasks.filter { it.date == date.toString() }.map {
            Assignment(0L, "Custom", it.title, it.date, 0.0)
        }

        taskAdapter.update(canvas + google + custom)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleGoogleEvents(events: List<Event>) {
        googleEventsByDate.clear()
        for (event in events) {
            val start = event.start?.dateTime ?: event.start?.date ?: continue
            val d = LocalDate.parse(start.toString().substring(0, 10))
            googleEventsByDate[d] = googleEventsByDate.getOrDefault(d, listOf()) + event
        }
        updateCalendar()
        Toast.makeText(requireContext(), "Google Calendar updated!", Toast.LENGTH_SHORT).show()
    }
}
