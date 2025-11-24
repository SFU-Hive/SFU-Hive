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

class UnlockedDialog(theBadge : Badge) : DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var imageView : ImageView
    private lateinit var titleView : TextView
    private lateinit var subheadView : TextView
    private lateinit var bodyView : TextView
    private var badge :Badge = theBadge
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        val view = populateView()
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("New Badge Unlocked!")
        ///builder.setTitle("")
        builder.setPositiveButton("Ok", null)
        val dialog : Dialog =builder.create()

        return dialog

    }
    override fun onClick(dialog: DialogInterface?, which: Int) {
        TODO("Not yet implemented")
    }
    private fun populateView():View{

        var view:View = requireActivity().layoutInflater.inflate(R.layout.dialog_unlocked_badge, null)
        // get all views
        val imageView=view.findViewById<ImageView>(R.id.featured_image)
        val titleView = view.findViewById<TextView>(R.id.title)
        val subheadView = view.findViewById<TextView>(R.id.subhead)
        val bodyView = view.findViewById<TextView>(R.id.body)

        // set all view fields
        imageView.setImageResource(badge.getUnlockedIcon())
        titleView.text = badge.getTitle()
        subheadView.text = "Unlocked!"//badge.getTextStatus()
        bodyView.text = badge.getDescription()

        return view
    }


}