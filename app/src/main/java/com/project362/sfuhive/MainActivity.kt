package com.project362.sfuhive

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.project362.sfuhive.Util.SubmittedAssignment
import com.project362.sfuhive.database.AssignmentViewModel
import com.project362.sfuhive.database.AssignmentViewModelFactory


class MainActivity : ComponentActivity() {

    private lateinit var viewModelFactory: AssignmentViewModelFactory
    private lateinit var assignmentViewModel: AssignmentViewModel
    private lateinit var loadButton: Button


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadButton = findViewById(R.id.load)

        viewModelFactory = Util.getViewModelFactory(this)
        assignmentViewModel =
            ViewModelProvider(this, viewModelFactory).get(AssignmentViewModel::class.java)

        // observe and log local database changes
        assignmentViewModel.allAssignmentsLiveData.observe(this, Observer { it ->
            Log.d("DatabaseCheck", "Assignments count: ${it.size}")
        })


        loadButton.setOnClickListener {
            // put all assignments into database on start if not there
            Thread {
                Util.getCanvasAssignments(this, this)
            }.start()

            // open to dashboard
            intent = Intent(this, NavActivity::class.java)
            startActivity(intent)
        }
    }
}