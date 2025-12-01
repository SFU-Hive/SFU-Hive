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

// Build the dialog that displays when a user has enough coins to purchase a reward
// Confirmation of reward purchase
// Returns an AlertDialog instance
class PurchaseDialog(theReward:Reward): DialogFragment(), DialogInterface.OnClickListener {

    private var bundle_result : Bundle = Bundle() // Bundle to hold the "purchased/not purchased" dialog result

    // Views to populate with reward info
    private lateinit var imageView : ImageView
    private lateinit var titleView : TextView
    private lateinit var subheadView : TextView
    private lateinit var bodyView : TextView

    // The reward the user selected to redeem
    private var reward :Reward = theReward

    //Keys for accessing the bundle in RewardActivity
    companion object{
        const val DIALOG_RESULT = "RESULT"
        const val STRING_RESULT = "DATA"
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{

        // instantiate and set all corresponding fields of the view with the appropriate reward data
        val view: View = populateView()

        // get a dialog builder
        val builder = AlertDialog.Builder(requireActivity())

        //Set builder fields
        builder.setView(view)
        builder.setTitle("Buy this reward?")
        builder.setPositiveButton("Redeem", this)
        builder.setNegativeButton("Cancel", this)

        //Create dialog object from the build
        val dialog : Dialog =builder.create()

        return dialog
    }

    // Define the behaviour for "Redeem" button click or "Cancel" click
    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE){

            // Notify RewardActivity that user has selected to redeem this reward
            bundle_result.putString(STRING_RESULT,"redeemed")
            requireActivity().supportFragmentManager.setFragmentResult(RewardActivity.REDEEM, bundle_result)
            println("positive button clicked")
        }
        else if (which == DialogInterface.BUTTON_NEGATIVE){

            // Notify RewardActivity that user has selected to NOT redeem this reward
            bundle_result.putString(STRING_RESULT,"canceled")
            requireActivity().supportFragmentManager.setFragmentResult(RewardActivity.REDEEM,bundle_result)
            println("negative button clicked")
        }
    }

    // Gets the instances of view fields and sets their data accordingly
    private fun populateView():View{

        // Inflate the badge dialog with corresponding layout "dialog_purchase"
        var view:View = requireActivity().layoutInflater.inflate(R.layout.dialog_purchase, null)

        // get all views
        imageView=view.findViewById<ImageView>(R.id.featured_image)
        titleView = view.findViewById<TextView>(R.id.title)
        subheadView = view.findViewById<TextView>(R.id.subhead)
        bodyView = view.findViewById<TextView>(R.id.body)

        // set all view fields
        imageView.setImageResource(reward.getIconId())
        titleView.text = reward.getTitle()
        subheadView.text = "Cost: $${reward.getCost().toString()}"
        bodyView.text = reward.getDescription()

        return view
    }
}