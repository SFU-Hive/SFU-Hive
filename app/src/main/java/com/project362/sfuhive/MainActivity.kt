package com.project362.sfuhive

import android.content.Intent //added for calendar
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.project362.sfuhive.database.AssignmentViewModel
import com.project362.sfuhive.database.AssignmentViewModelFactory
import com.project362.sfuhive.ui.calendar.CalendarActivity //added for calendar

class MainActivity : ComponentActivity() {

    private lateinit var viewModelFactory: AssignmentViewModelFactory
    private lateinit var assignmentViewModel: AssignmentViewModel
    private lateinit var loadButton: Button

    //added for calendar
    private lateinit var calendarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadButton = findViewById(R.id.load)


//        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        val loaded = prefs.getBoolean("assignments_loaded", false)


        viewModelFactory = Util.getViewModelFactory(this)
        assignmentViewModel =
            ViewModelProvider(this, viewModelFactory).get(AssignmentViewModel::class.java)


        assignmentViewModel.allAssignmentsLiveData.observe(this, Observer { it ->
            Log.d("DatabaseCheck", "Assignments count: ${it.size}")
        })


        loadButton.setOnClickListener {
            // put all assignments into database on start if not there
            Thread {
                Util.getCanvasAssignments(this, this)
            }.start()
        }

        //added for calendar
        calendarButton = findViewById(R.id.btnCalendar)

        calendarButton.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }
    }
}