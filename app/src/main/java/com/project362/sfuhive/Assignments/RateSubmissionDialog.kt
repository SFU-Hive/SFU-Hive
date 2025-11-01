package com.project362.sfuhive.Assignments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.project362.sfuhive.R
import com.project362.sfuhive.RatedAssignment
import com.project362.sfuhive.Util

class RateSubmissionDialog : DialogFragment(), DialogInterface.OnClickListener {

    private var assignmentID: Long? = 0L
    private var assignmentName: String? = ""
    private var courseID: Long? = 0L
    private var courseName: String? = ""
    private var grade: Double? = 0.0
    lateinit var difficultySpinner: Spinner
    lateinit var timeEditText: EditText
    lateinit var courseNameView: TextView
    lateinit var assignmentNameView: TextView
    lateinit var ratedAssignment: RatedAssignment
    var userUid: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog

        // create rated assignment object
        ratedAssignment = RatedAssignment()

        //get arguments
        val bundle = arguments
        assignmentID = bundle?.getLong(Util.ASSIGNMENT_ID_KEY)
        assignmentName = bundle?.getString(Util.ASSIGNMENT_NAME_KEY)
        courseID = bundle?.getLong(Util.COURSE_ID_KEY)
        courseName = bundle?.getString(Util.COURSE_NAME_KEY)
        grade = bundle?.getDouble(Util.GRADE_KEY)
        userUid = arguments?.getString("USER_UID")
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(
            R.layout.rate_submission_dialog,
            null
        )

        // set rated assignment data
        ratedAssignment.courseId = courseID!!
        ratedAssignment.courseName = courseName!!
        ratedAssignment.assignmentId = assignmentID!!
        ratedAssignment.assignmentName = assignmentName!!
        ratedAssignment.hoursSpent = grade!!


        // get views
        difficultySpinner = view.findViewById(R.id.difficulty_spinner)
        timeEditText = view.findViewById(R.id.time_spent)
        courseNameView = view.findViewById(R.id.course_name)
        assignmentNameView = view.findViewById(R.id.assignment_name)

        // set input type
        timeEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        // set view accordingly
        courseNameView.text = courseName
        assignmentNameView.text = assignmentName

        builder.setView(view)
        builder.setTitle("Rate Assignment")
        builder.setPositiveButton("Submit", this)
        ret = builder.create()

        return ret
    }

    override fun onClick(p0: DialogInterface, p1: Int) {
        when (p1) {
            DialogInterface.BUTTON_POSITIVE -> {
                val difficulty = difficultySpinner.selectedItem.toString().toInt()
                val timeSpent = timeEditText.text.toString().toDoubleOrNull() ?: 0.0

                ratedAssignment.difficulty = difficulty
                ratedAssignment.hoursSpent = timeSpent

                // Write a message to the database
                val database = Firebase.database
                val myRef = database.getReference("rated_assignments").child(userUid ?: return)

                myRef.push().setValue(ratedAssignment)
                    .addOnSuccessListener {
                        Log.d("FirebaseDB", "Rated assignment saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.d("FirebaseDB", "Failed to save rated assignment")
                    }

                // log result
                Log.d(
                    "RateSubmission",
                    "Rated $assignmentName with difficulty $difficulty, time $timeSpent min"
                )

                // notify parent from ChatGPT
                parentFragmentManager.setFragmentResult("RateSubmission", Bundle())
            }
        }
    }
}