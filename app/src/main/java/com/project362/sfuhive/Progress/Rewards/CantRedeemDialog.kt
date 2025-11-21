package com.project362.sfuhive.Progress.Rewards

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import android.widget.TextView
import com.project362.sfuhive.R

class CantRedeemDialog(theReward:Reward): DialogFragment(), DialogInterface.OnClickListener {
    private var bundle_result : Bundle = Bundle()
    private var reward :Reward = theReward


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireActivity())
        val view: View = populateView()
        builder.setPositiveButton("Ok", this)
        builder.setView(view)
        val dialog : Dialog =builder.create()

        return dialog
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {

    }
    private fun populateView():View{
        var view : View = requireActivity().layoutInflater.inflate(R.layout.dialog_cant_redeem, null)
        var textView=view.findViewById<TextView>(R.id.main_text_cant_redeem)
        var detailsTextView = view.findViewById<TextView>(R.id.details_text_cant_redeem)
        textView.text = "Oh No! ${reward.getTitle()} costs ${reward.getCost()}."
        detailsTextView.text = "You don't have enough coins to redeem ${reward.getTitle()} :("

        return view
    }

}