package com.project362.sfuhive.Assignments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.Assignments.RateSubmissionDialog.RatedAssignment
import java.util.ArrayList
import java.util.Locale

class ListAssignmentsActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RatedAssignmentAdapter
    private lateinit var assignments: List<RatedAssignment>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_assignments)

        // get assignments and then get distinct assignments (since there can be more than one entry)
        assignments = intent.getParcelableArrayListExtra("assignments", RatedAssignment::class.java) ?: emptyList()
        // map of assignment id to assignment name
        val assignmentsMap = mutableMapOf<Long, String>()

        for (assignment in assignments) {
            assignmentsMap[assignment.assignmentId] = assignment.assignmentName
        }

        val distinctAssignments = assignments.distinctBy { it.assignmentId }

        recyclerView = findViewById(R.id.course_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RatedAssignmentAdapter(distinctAssignments) { selectedAssignment ->
//            Log.d("ListAssignmentsActivity", "Selected assignment: ${selectedAssignment.assignmentName}, Difficulty: ${selectedAssignment.difficulty} ")
            displayRatedAssignment(selectedAssignment, assignments)
        }
        recyclerView.adapter = adapter

        // setup tool bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Assignments"
        setSupportActionBar(toolbar)
    }

    // implementation adapted from https://www.geeksforgeeks.org/android/searchview-in-android-with-recyclerview/
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater

        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.actionSearch)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
        return true
    }

    private fun filter(text: String) {
        val filtered = ArrayList<RatedAssignment>()

        for (item in assignments) {
            // perform search
            if (item.assignmentName.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                filtered.add(item)
            }
            adapter.filterList(filtered)
        }
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