package com.project362.sfuhive.Assignments

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.Assignments.RateSubmissionDialog.RatedAssignment

class ListAssignmentsActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RatedAssignmentAdapter


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignments)

        val assignments = intent.getParcelableArrayListExtra("assignments", RatedAssignment::class.java) ?: emptyList()

        recyclerView = findViewById(R.id.assignment_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RatedAssignmentAdapter(assignments) { selectedAssignment ->
            Log.d("ListAssignmentsActivity", "Selected assignment: ${selectedAssignment.assignmentName}, Difficulty: ${selectedAssignment.difficulty} ")
        }
        recyclerView.adapter = adapter
    }

}