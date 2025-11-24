package com.project362.sfuhive.Wellness

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.project362.sfuhive.R


class GoalDialog (): DialogFragment(), DialogInterface.OnClickListener {

    companion object {
        const val GOAL_INDEX_KEY = "goal_index"

        fun newInstance(goalIndex: Int): GoalDialog {
            val dialog = GoalDialog()
            val args = Bundle()
            args.putInt(GOAL_INDEX_KEY, goalIndex)
            dialog.arguments = args
            return dialog
        }
    }

    private val goalIndex: Int by lazy {
        arguments?.getInt(GOAL_INDEX_KEY) ?: -1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        lateinit var dialog: Dialog
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(R.layout.dialog_goal, null)

        builder.setView(view)
        builder.setTitle("Configure Goal $goalIndex:")
        builder.setPositiveButton("Save", this)
        builder.setNegativeButton("Cancel", this)
        dialog = builder.create()

        return dialog
    }

    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            // extract the input and pass it back to the caller
            Toast.makeText(requireActivity(), "Save clicked!", Toast.LENGTH_SHORT).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(requireActivity(), "Cancel clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
