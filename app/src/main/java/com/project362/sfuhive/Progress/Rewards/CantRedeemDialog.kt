package com.project362.sfuhive.Progress.Rewards

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import android.widget.TextView
import com.project362.sfuhive.R

// Build the dialog that notifies the user "you can't redeem this - you don't have enough coins"
// Returns an AlertDialog instance
class CantRedeemDialog(theReward:Reward): DialogFragment(), DialogInterface.OnClickListener {
    private var reward :Reward = theReward // The reward the user attempted to redeem


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // instantiate and set all corresponding fields of the view with the appropriate reward data
        val view: View = populateView()

        // get a dialog builder
        val builder = AlertDialog.Builder(requireActivity())

        //Set builder fields
        builder.setPositiveButton("Ok", this)
        builder.setView(view)

        //Create dialog object from the build
        val dialog : Dialog =builder.create()

        return dialog
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        // Not needed. Nothing happens on dialog click except dialog dismissal

    }

    // Gets the instances of view fields and sets their data accordingly
    private fun populateView():View{

        // Inflate the badge dialog with corresponding layout "dialog_unlocked_badge"
        var view : View = requireActivity().layoutInflater.inflate(R.layout.dialog_cant_redeem, null)

        // get all views
        var textView=view.findViewById<TextView>(R.id.main_text_cant_redeem)
        var detailsTextView = view.findViewById<TextView>(R.id.details_text_cant_redeem)

        // set all view fields.
        textView.text = "Oh No! ${reward.getTitle()} costs ${reward.getCost()}."
        detailsTextView.text = "You don't have enough coins to redeem ${reward.getTitle()} :("

        return view
    }

}