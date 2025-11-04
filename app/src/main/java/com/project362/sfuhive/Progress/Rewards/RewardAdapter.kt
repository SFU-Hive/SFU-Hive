package com.project362.sfuhive.Progress.Rewards

import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.Progress.Badges.BadgeActivityViewModel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R

class RewardAdapter(
    context: Context,
    private val rewards : List<Reward>,
    private val viewModel: RewardActivityViewModel
):RecyclerView.Adapter<RewardAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reward_view, parent, false)
        val viewHolder=ViewHolder(view)

        return viewHolder
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.rewardIconView.setImageResource(rewards.get(position).getIconId())
        holder.rewardTitleView.text = rewards.get(position).getTitle()
        holder.view.setOnClickListener {
            println("setting featured badge to {${rewards.get(position).getTitle()}}")
            viewModel.setFeaturedBadge(rewards.get(position))
        }
    }

    override fun getItemCount(): Int {
        return rewards.size
    }



    class ViewHolder(val view :View, ) : RecyclerView.ViewHolder(
        view
    ) {
        val rewardIconView : ImageView
        val rewardTitleView : TextView

        init {
            rewardIconView = view.findViewById<ImageView>(R.id.reward_icon_view)
            rewardTitleView= view.findViewById<TextView>(R.id.reward_title_view)
        }

    }
}