package com.project362.sfuhive.Calendar

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Assignment

class DayViewActivity : AppCompatActivity() {

    private lateinit var tvDate: TextView
    private lateinit var recycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_view)

        // FIXED ID HERE
        tvDate = findViewById(R.id.dayViewDateTitle)
        recycler = findViewById(R.id.dayViewRecycler)

        recycler.layoutManager = LinearLayoutManager(this)

        // Retrieve data passed from CalendarFragment
        val date = intent.getStringExtra("selected_date") ?: ""
        tvDate.text = date

        val titles = intent.getStringArrayListExtra("task_titles") ?: arrayListOf()
        val courses = intent.getStringArrayListExtra("task_courses") ?: arrayListOf()
        val dates = intent.getStringArrayListExtra("task_dates") ?: arrayListOf()
        val ids = intent.getStringArrayListExtra("task_priority_ids") ?: arrayListOf()
        val points = intent.getDoubleArrayExtra("task_points") ?: DoubleArray(titles.size)
        val groups = intent.getDoubleArrayExtra("task_groups") ?: DoubleArray(titles.size)

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

        taskAdapter = TaskAdapter(tasks, ids)
        recycler.adapter = taskAdapter
    }
}
