package com.project362.sfuhive.Wellness

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.project362.sfuhive.R
import com.project362.sfuhive.Util.getViewModelFactory
import com.project362.sfuhive.database.DataViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class GoalDialog (): DialogFragment() {
    companion object {
        private const val ARG_GOAL_ID = "arg_goal_id"

        fun newInstance(goalId: Long): GoalDialog {
            val d = GoalDialog()
            val args = Bundle()
            args.putLong(ARG_GOAL_ID, goalId)
            d.arguments = args
            return d
        }
    }

    private val goalId: Long by lazy { arguments?.getLong(ARG_GOAL_ID) ?: -1L }
    private lateinit var viewModel: DataViewModel
    private lateinit var editName: EditText
    private lateinit var assignBtn: Button
    private lateinit var clearBtn: Button
    private lateinit var nfcStatusText: TextView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val factory = getViewModelFactory(requireContext())
        viewModel = ViewModelProvider(requireActivity(), factory).get(DataViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_goal, null)

        editName = view.findViewById(R.id.edit_goal_name)
        nfcStatusText = view.findViewById(R.id.nfc_status)
        assignBtn = view.findViewById(R.id.assign_nfc_btn)
        clearBtn = view.findViewById(R.id.clear_nfc_btn)

        // preload existing goal name and details
        lifecycleScope.launch {
            val goal = viewModel.getGoalById(goalId).firstOrNull()
            val tagId = viewModel.getNfcById(goalId).firstOrNull()


            editName.setText(goal?.goalName ?: "")

            if (!tagId.isNullOrEmpty()) {
                nfcStatusText.text = "Assigned: $tagId"
                clearBtn.visibility = View.VISIBLE
            } else {
                nfcStatusText.text = "No tag assigned"
            }
        }


        builder.setView(view)
        builder.setTitle("Configure Goal:")
        builder.setNegativeButton("Cancel", null) // override later
        builder.setPositiveButton("Save", null) // override later

        val dialog = builder.create()


        assignBtn.setOnClickListener {

            val name = editName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a goal name first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            (activity as? GoalsActivity)?.pendingGoalAssignId = goalId
            nfcStatusText.text = "Waiting for NFC scan..."
        }

        clearBtn.setOnClickListener {
            lifecycleScope.launch {
                viewModel.updateNfcTag(goalId, null)
                nfcStatusText.text = "No tag assigned"
                clearBtn.visibility = View.GONE
                Toast.makeText(requireContext(), "Tag cleared", Toast.LENGTH_SHORT).show()
            }
        }


        // override button clicks after dialog shows so we can control dismissal
        dialog.setOnShowListener {
            val btnSave: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnCancel: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnSave.setOnClickListener {
                val name = editName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a goal name", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                // persist via viewModel
                viewModel.updateGoalName(goalId, name)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                Log.d("GoalDialog", "Saved goalId=$goalId name='$name'")
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        // to avoid stray scans from registering
        dialog.setOnDismissListener {
            (activity as? GoalsActivity)?.pendingGoalAssignId = null
        }

        return dialog
    }

    fun updateNfcStatus(tagId: String) {
        nfcStatusText.text = "Assigned: $tagId"
        clearBtn.visibility = View.VISIBLE
    }

}
