package com.project362.sfuhive.Calendar

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Assignment


 // Shows a simple list of assignments/tasks for a single selected date.
class DayViewActivity : AppCompatActivity() {

    private lateinit var tvDate: TextView
    private lateinit var recycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_view)

        // Find and wire up views
        tvDate = findViewById(R.id.dayViewDateTitle)
        recycler = findViewById(R.id.dayViewRecycler)

        recycler.layoutManager = LinearLayoutManager(this)

        // Retrieve data passed from CalendarFragment via intent extras
        val date = intent.getStringExtra("selected_date") ?: ""
        tvDate.text = date

        val titles = intent.getStringArrayListExtra("task_titles") ?: arrayListOf()
        val courses = intent.getStringArrayListExtra("task_courses") ?: arrayListOf()
        val dates = intent.getStringArrayListExtra("task_dates") ?: arrayListOf()
        val ids = intent.getStringArrayListExtra("task_priority_ids") ?: arrayListOf()
        val points = intent.getDoubleArrayExtra("task_points") ?: DoubleArray(titles.size)
        val groups = intent.getDoubleArrayExtra("task_groups") ?: DoubleArray(titles.size)

        // Build Assignment list to feed TaskAdapter — use temporary IDs for display only
        val tasks = mutableListOf<Assignment>()

            for (i in titles.indices) {
            tasks.add(
                Assignment(
                    assignmentId = i.toLong(), // temp ID; not used for priority
                    courseName = courses[i],
                    assignmentName = titles[i],
                    dueAt = dates[i],
                    pointsPossible = points[i],
                    groupWeight = groups[i]
                )
            )
        }

        // Create adapter and finish the activity when it signals an update so the month view can refresh
        taskAdapter = TaskAdapter(tasks, ids) {
            // Refresh month calendar when returning from Day View
            finish()  // closes day view
        }
        recycler.adapter = taskAdapter
    }
}
