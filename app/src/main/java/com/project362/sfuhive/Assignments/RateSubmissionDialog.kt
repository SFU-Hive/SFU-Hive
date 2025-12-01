package com.project362.sfuhive.Assignments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.DataViewModel
import kotlinx.parcelize.Parcelize

class RateSubmissionDialog : DialogFragment(), DialogInterface.OnClickListener {

    private var assignmentID: Long? = 0L
    private var assignmentName: String? = ""
    private var courseID: Long? = 0L
    private var courseName: String? = ""
    private var grade: Double? = 0.0
    lateinit var ratingBar: RatingBar
    lateinit var timeEditText: EditText
    lateinit var courseNameView: TextView
    lateinit var assignmentNameView: TextView
    lateinit var ratedAssignment: RatedAssignment
    var userUid: String? = null

    // parcelize usage from ChatGPT
    @Parcelize
    data class RatedAssignment (
        var courseId: Long = 0L,
        var courseName: String = "",
        var assignmentId: Long = 0L,
        var assignmentName: String = "",
        var hoursSpent: Double = 0.0,
        var difficulty: Double = 0.0
    ): Parcelable

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
        ratingBar = view.findViewById(R.id.ratingBar)
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

                // get values from view inputs
                val difficulty = ratingBar.rating.toString().toDoubleOrNull() ?: 0.0
                val timeSpent = timeEditText.text.toString().toDoubleOrNull() ?: 0.0

                ratedAssignment.difficulty = difficulty
                ratedAssignment.hoursSpent = timeSpent

                // insert into database
                val viewModelFactory = Util.getViewModelFactory(requireContext())
                val dataViewModel = ViewModelProvider(this, viewModelFactory).get(DataViewModel::class.java)
                dataViewModel.insertRatedAssignment(ratedAssignment, userUid ?: return)

                // log result
                Log.d(
                    "RateSubmission",
                    "Rated $userUid $assignmentName with difficulty $difficulty, time $timeSpent min"
                )

                // notify parent from ChatGPT
                parentFragmentManager.setFragmentResult("RateSubmission", Bundle())
            }
        }
    }
}