package com.project362.sfuhive.Calendar

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.Util
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
            .let { androidx.lifecycle.ViewModelProvider(this, it) }
            .get(DataViewModel::class.java)

        val dateStr = intent.getStringExtra("selected_date") ?: return
        val date = LocalDate.parse(dateStr)

        dateTitle.text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd"))

        val googleEvents = GoogleEventCache.events[dateStr] ?: listOf()

        /** ⭐ FIX: Observe LiveData instead of using .value */
        dataViewModel.allAssignmentsLiveData.observe(this, Observer { allAssignments ->

            // Canvas + custom tasks
            val roomTasksForDate = allAssignments.filter {
                it.dueAt.startsWith(dateStr)
            }

            // Google events mapped to assignments
            val googleAsAssignments = googleEvents.map {
                Assignment(
                    assignmentId = 0L,
                    courseName = "Google Calendar",
                    assignmentName = it.summary ?: "Untitled Event",
                    dueAt = dateStr,
                    pointsPossible = 0.0
                )
            }

            // Combine all 3 sources
            val combined = roomTasksForDate + googleAsAssignments

            adapter.update(combined)
        })
    }
}
