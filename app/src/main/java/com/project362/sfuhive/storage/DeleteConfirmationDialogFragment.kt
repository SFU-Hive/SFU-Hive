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

        val currentActivity = activity?: throw IllegalStateException("Activity cannot be null")

        val builder = AlertDialog.Builder(currentActivity)
        builder.setTitle("Delete Confirmation")
        builder.setMessage(R.string.warning_msg)
        builder.setPositiveButton("Delete") { _, _ ->
            listener?.onDeleteConfirmed()
        }
        builder.setNegativeButton("Cancel"){ dialog, _ ->
            dialog.dismiss()
        }

        return builder.create()
    }
}