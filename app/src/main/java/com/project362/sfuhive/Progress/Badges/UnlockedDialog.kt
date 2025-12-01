package com.project362.sfuhive.Progress.Badges

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.project362.sfuhive.Progress.Rewards.Reward
import com.project362.sfuhive.R

// Build the dialog that displays when a user unlocks a badge
// Returns an AlertDialog instance
class UnlockedDialog(theBadge : Badge) : DialogFragment(), DialogInterface.OnClickListener {
    private var badge :Badge = theBadge

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{

        // instantiate and set all corresponding fields of the view with the appropriate badge data
        val view = populateView()

        // get a dialog builder
        val builder = AlertDialog.Builder(requireActivity())

        //Set builder fields
        builder.setView(view)
        builder.setTitle("New Badge Unlocked!")
        builder.setPositiveButton("Ok", null)

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
        var view:View = requireActivity().layoutInflater.inflate(R.layout.dialog_unlocked_badge, null)

        // get all views
        val imageView=view.findViewById<ImageView>(R.id.featured_image)
        val titleView = view.findViewById<TextView>(R.id.title)
        val subheadView = view.findViewById<TextView>(R.id.subhead)
        val bodyView = view.findViewById<TextView>(R.id.body)

        // set all view fields. Make sure it always displays the "unlocked" version of the badge
        imageView.setImageResource(badge.getUnlockedIcon())
        titleView.text = badge.getTitle()
        subheadView.text = "Unlocked!"
        bodyView.text = badge.getDescription()

        return view
    }


}