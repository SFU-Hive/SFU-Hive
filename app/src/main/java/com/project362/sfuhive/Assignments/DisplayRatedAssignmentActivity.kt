package com.project362.sfuhive.Assignments

import android.os.Build
import android.os.Bundle
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.project362.sfuhive.Assignments.RateSubmissionDialog.RatedAssignment
import com.project362.sfuhive.R

class DisplayRatedAssignmentActivity: AppCompatActivity() {

    private lateinit var courseNameView: TextView
    private lateinit var assignmentNameView: TextView
    private lateinit var difficultyBar: RatingBar
    private lateinit var hoursView: TextView


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_rated_assignment)



        // get assignments (multiple assignments for multiple people submitting ratings)
        val assignments = intent.getParcelableArrayListExtra("assignments", RatedAssignment::class.java) ?: emptyList()

        // exit out if something goes wrong with loading assignment
        if (assignments.isEmpty()) {
            Toast.makeText(this, "No assignments found", Toast.LENGTH_SHORT).show()
            finish()
        }

        courseNameView = findViewById(R.id.course_title)
        assignmentNameView = findViewById(R.id.assignment_title)
        difficultyBar = findViewById(R.id.difficulty)
        hoursView = findViewById(R.id.hours)

        // get course and assignment name
        val courseName = assignments[0].courseName
        val assignmentName = assignments[0].assignmentName

        // calculate averages for difficulty and time spent
        val averageDifficulty = assignments.map { it.difficulty }.average()
        val averageHours = assignments.map { it.hoursSpent }.average()

        // set values to views
        courseNameView.text = "Course: $courseName"
        assignmentNameView.text = "Assignment: $assignmentName"
        difficultyBar.setRating(averageDifficulty.toFloat())
        hoursView.text = averageHours.toString()
    }
}