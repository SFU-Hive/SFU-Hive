package com.project362.sfuhive.Assignments

import android.content.Intent
import android.os.Build
import android.os.Bundle
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

        // get assignments and then get distinct assignments (since there can be more than one entry)
        val assignments = intent.getParcelableArrayListExtra("assignments", RatedAssignment::class.java) ?: emptyList()
        // map of assignment id to assignment name
        val assignmentsMap = mutableMapOf<Long, String>()

        for (assignment in assignments) {
            assignmentsMap[assignment.assignmentId] = assignment.assignmentName
        }

        val distinctAssignments = assignments.distinctBy { it.assignmentId }



        recyclerView = findViewById(R.id.assignment_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RatedAssignmentAdapter(distinctAssignments) { selectedAssignment ->
//            Log.d("ListAssignmentsActivity", "Selected assignment: ${selectedAssignment.assignmentName}, Difficulty: ${selectedAssignment.difficulty} ")
            displayRatedAssignment(selectedAssignment, assignments)
        }
        recyclerView.adapter = adapter
    }

    private fun displayRatedAssignment(assignmentList: RatedAssignment, assignments: List<RatedAssignment>) {

        val filteredAssignments = filterAssignmentsById(assignmentList.assignmentId, assignments)

        val intent = Intent(this, DisplayRatedAssignmentActivity::class.java)
        intent.putParcelableArrayListExtra("assignments", ArrayList(filteredAssignments) )
        startActivity(intent)
    }

    private fun filterAssignmentsById(assignmentId: Long, assignments: List<RatedAssignment>) : List<RatedAssignment> {
        return assignments.filter { it.assignmentId == assignmentId }
    }

}