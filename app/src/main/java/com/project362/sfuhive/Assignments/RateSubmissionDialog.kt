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
import com.project362.sfuhive.R
import com.project362.sfuhive.Util

class RateSubmissionDialog : DialogFragment(), DialogInterface.OnClickListener {

    private var assignmentID: Long? = 0L
    private var assignmentName: String? = ""
    private var courseName: String? = ""
    private var grade: Double? = 0.0
    lateinit var difficultySpinner: Spinner
    lateinit var timeEditText: EditText
    lateinit var courseNameView: TextView
    lateinit var assignmentNameView: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog

        //get arguments
        val bundle = arguments
        assignmentID = bundle?.getLong(Util.ASSIGNMENT_ID_KEY)
        assignmentName = bundle?.getString(Util.ASSIGNMENT_NAME_KEY)
        courseName = bundle?.getString(Util.COURSE_NAME_KEY)
        grade = bundle?.getDouble(Util.GRADE_KEY)
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(
            R.layout.rate_submission_dialog,
            null
        )

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

    override fun onClick(p0: DialogInterface?, p1: Int) {
        when (p1) {
            DialogInterface.BUTTON_POSITIVE -> {
                val difficulty = difficultySpinner.selectedItem.toString()
                val timeSpent = timeEditText.text.toString().toDoubleOrNull() ?: 0.0

                // TODO: save to online database

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