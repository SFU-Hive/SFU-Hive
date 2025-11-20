package com.project362.sfuhive.Progress.Rewards

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.project362.sfuhive.R

class PurchaseDialog(theReward:Reward): DialogFragment(), DialogInterface.OnClickListener {

    private var bundle_result : Bundle = Bundle()

    private lateinit var imageView : ImageView
    private lateinit var titleView : TextView
    private lateinit var subheadView : TextView
    private lateinit var bodyView : TextView
    private var reward :Reward = theReward

    companion object{
        const val DIALOG_RESULT = "RESULT"
        const val STRING_RESULT = "DATA"

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        val builder = AlertDialog.Builder(requireActivity())

        val view: View = populateView()

        builder.setView(view)
        ///builder.setTitle("")
        builder.setPositiveButton("Redeem", this)
        builder.setNegativeButton("Cancel", this)
        val dialog : Dialog =builder.create()
        return dialog
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE){
            bundle_result.putString(STRING_RESULT,"redeemed")
            requireActivity().supportFragmentManager.setFragmentResult(RewardActivity.REDEEM, bundle_result)
            println("positive button clicked")
        }
        else if (which == DialogInterface.BUTTON_NEGATIVE){
            bundle_result.putString(STRING_RESULT,"canceled")
            requireActivity().supportFragmentManager.setFragmentResult(RewardActivity.REDEEM,bundle_result)
            println("negative button clicked")
        }
    }
    private fun populateView():View{
        var view:View = requireActivity().layoutInflater.inflate(R.layout.dialog_purchase, null)
        // get all views
        imageView=view.findViewById<ImageView>(R.id.featured_image)
        titleView = view.findViewById<TextView>(R.id.title)
        subheadView = view.findViewById<TextView>(R.id.subhead)
        bodyView = view.findViewById<TextView>(R.id.body)

        // set all view fields
        imageView.setImageResource(reward.getIconId())
        titleView.text = reward.getTitle()
        subheadView.text = reward.getCost().toString()
        bodyView.text = reward.getDescription()

        return view
    }
}