package com.project362.sfuhive.Progress.Rewards

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.project362.sfuhive.R

class PurchaseDialog: DialogFragment(), DialogInterface.OnClickListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(R.layout.dialog_purchase, null)
        builder.setView(view)
        ///builder.setTitle("")
        builder.setPositiveButton("Redeem", this)
        builder.setNegativeButton("Cancel", this)
        val dialog : Dialog =builder.create()
        return dialog
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        println("Dialog Clicked!")
    }
}