package com.project362.sfuhive.Progress.Badges

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import okhttp3.internal.notify

// The Adapter to be used with a RecyclerView displaying badge objects
//
class BadgeAdapter(
    private val context: Context, // Context of the activity the RecyclerView will be displayed
    private val badges : List<Badge>, // I would delete this but it would break the rest of the instances that use it; so I will leave it as is.
    private val viewModel: BadgeActivityViewModel

):RecyclerView.Adapter<BadgeAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        //Inflate ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.badge_view,
            parent,
            false
        )
        val viewHolder=ViewHolder(view) // Get an instance of our ViewHolder (see below for ViewHolder Object)

        return viewHolder
    }

    // This function binds all of the data to the holder's view fields
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        var badge = viewModel.getBadgeList().get(position) // get the badge at requested position

        // Set Views
        holder.badgeIconView.setImageResource(badge.getIconId()) // Set the badge icon (changes depending on locked/unlocked status)
        holder.badgeTitleView.text = badge.getTitle() // Set the title of our badge to appropriate TextView holder

        //Set the current feature badge to be the clicked-badge
        holder.view.setOnClickListener {
            println("setting featured badge to {${badge.getTitle()}}")
            viewModel.setFeaturedBadge(badge)
        }

        // When the BadgeEntity is changed (i.e the badge is locked/unlocked)
        // --> Update the icon id
        // reset the badge info to match the locked/unlocked version
        val lifeCycleOwner=context as LifecycleOwner
        badge.badgeEntity.observe(lifeCycleOwner, Observer {
            holder.badgeIconView.setImageResource(badge.getIconId()) // reset this view image
            viewModel.setFeaturedBadge(badge) // reset the featured badge info
        })
    }

    override fun getItemCount(): Int {
        return viewModel.getBadgeList().size
    }


    // Our Badge ViewHolder contains:
    // - The badge image
    // - The badge title
    class ViewHolder(val view :View, ) : RecyclerView.ViewHolder(
        view
    ) {
        val badgeIconView : ImageView
        val badgeTitleView : TextView

        init {
            // Capture the view for each view holder component at instantiation time
            badgeIconView = view.findViewById<ImageView>(R.id.icon_view)
            badgeTitleView= view.findViewById<TextView>(R.id.badge_title_view)
        }

    }
}