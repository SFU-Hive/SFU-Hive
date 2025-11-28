package com.project362.sfuhive.Calendar

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.Calendar.GoogleEventDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayViewActivity : ComponentActivity() {

    private lateinit var dateTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var dataViewModel: DataViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_view)

        dateTitle = findViewById(R.id.dayViewDateTitle)
        recyclerView = findViewById(R.id.dayViewRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(listOf())
        recyclerView.adapter = adapter

        dataViewModel = Util.getViewModelFactory(this)
            .let { ViewModelProvider(this, it) }
            .get(DataViewModel::class.java)

        val dateStr = intent.getStringExtra("selected_date") ?: return
        val date = LocalDate.parse(dateStr)

        dateTitle.text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd"))

        dataViewModel.allAssignmentsLiveData.observe(this, Observer { allAssignments ->

            // Canvas + custom tasks for this day
            val roomTasksForDate = allAssignments.filter {
                it.dueAt.startsWith(dateStr)
            }

            // Load Google events for this day from DB
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = GoogleEventDatabase.getInstance(this@DayViewActivity).googleEventDao()
                val dbEvents = dao.getEventsForDate(dateStr)

                val googleAsAssignments = dbEvents.map {
                    Assignment(
                        assignmentId = 0L,
                        courseName = "Google Calendar",
                        assignmentName = it.title,
                        dueAt = it.date,
                        pointsPossible = 0.0
                    )
                }

                val combined = roomTasksForDate + googleAsAssignments

                withContext(Dispatchers.Main) {
                    adapter.update(combined)
                }
            }
        })
    }
}
