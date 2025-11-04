package com.project362.sfuhive.Progress.Badges

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R

class BadgeAdapter(
    context: Context,
    private val badges : List<Badge>,
    private val viewModel: BadgeActivityViewModel
):RecyclerView.Adapter<BadgeAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.badge_view, parent, false)
        val viewHolder=ViewHolder(view)

        return viewHolder
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.badgeIconView.setImageResource(badges.get(position).getIconId())
        holder.badgeTitleView.text = badges.get(position).getTitle()
        holder.view.setOnClickListener {
            println("setting featured badge to {${badges.get(position).getTitle()}}")
            viewModel.setFeaturedBadge(badges.get(position))
        }
    }

    override fun getItemCount(): Int {
        return badges.size
    }



    class ViewHolder(val view :View, ) : RecyclerView.ViewHolder(
        view
    ) {
        val badgeIconView : ImageView
        val badgeTitleView : TextView

        init {
            badgeIconView = view.findViewById<ImageView>(R.id.icon_view)
            badgeTitleView= view.findViewById<TextView>(R.id.badge_title_view)
        }

    }
}