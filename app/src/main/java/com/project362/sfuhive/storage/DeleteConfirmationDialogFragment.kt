package com.project362.sfuhive.storage

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.project362.sfuhive.R

class DeleteConfirmationDialogFragment : DialogFragment() {
    interface ConfirmationListener {
        fun onDeleteConfirmed()
    }

    private var listener: ConfirmationListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listener = activity as? ConfirmationListener

        if (listener == null) {
            listener = activity as? ConfirmationListener
        }
        // Check if the listener is not null
        val currentActivity = activity?: throw IllegalStateException("Activity cannot be null")

        // Create the confirmation dialog
        val builder = AlertDialog.Builder(currentActivity)
        builder.setTitle("Delete Confirmation")
        builder.setMessage(R.string.warning_msg)
        builder.setPositiveButton("Delete") { _, _ ->
            // Call the delete confirmation
            listener?.onDeleteConfirmed()
        }
        builder.setNegativeButton("Cancel"){ dialog, _ ->
            // Dismiss the dialog
            dialog.dismiss()
        }

        return builder.create()
    }
}