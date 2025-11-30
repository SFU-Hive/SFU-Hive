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


class BadgeAdapter(
    private val context: Context,
    private val badges : List<Badge>,
    private val viewModel: BadgeActivityViewModel

):RecyclerView.Adapter<BadgeAdapter.ViewHolder>(){
    private lateinit var parentContext : Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.badge_view, parent, false)
        parentContext=parent.context
        val viewHolder=ViewHolder(view)


        return viewHolder
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        var badge = viewModel.getBadgeList().get(position)

        holder.badgeIconView.setImageResource(badge.getIconId()) // set observer for this
        holder.badgeTitleView.text = badge.getTitle()
        holder.view.setOnClickListener {
            println("setting featured badge to {${badge.getTitle()}}")
            viewModel.setFeaturedBadge(badge)
            //Change theme here
        }
        val lifeCycleOwner=context as LifecycleOwner
        badge.badgeEntity.observe(lifeCycleOwner, Observer {
            holder.badgeIconView.setImageResource(badge.getIconId())
            viewModel.setFeaturedBadge(badge)
        })
    }

    override fun getItemCount(): Int {
        return viewModel.getBadgeList().size
    }



    class ViewHolder(val view :View, ) : RecyclerView.ViewHolder(
        view
    ) {
        val badgeIconView : ImageView
        val badgeTitleView : TextView

        init {
            badgeIconView = view.findViewById<ImageView>(R.id.icon_view)
            badgeTitleView= view.findViewById<TextView>(R.id.badge_title_view)

            // observe live badge data here

        }

    }
}