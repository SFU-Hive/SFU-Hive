package com.project362.sfuhive.Progress.Rewards


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R

// The Adapter to be used with a RecyclerView displaying reward objects
class RewardAdapter(
    context: Context,
    private val rewards : List<Reward>, // List of all rewards to display in RecyclerView
    private val viewModel: RewardActivityViewModel // ViewModel associated with the RecyclerView holding rewards

):RecyclerView.Adapter<RewardAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        //Inflate ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.reward_view,
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
        // Set Views
        holder.rewardIconView.setImageResource(rewards.get(position).getIconId())
        holder.rewardTitleView.text = rewards.get(position).getTitle()

        //Set the current feature reward to be the clicked-reward
        holder.view.setOnClickListener {
            println("setting featured reward to {${rewards.get(position).getTitle()}}")
            viewModel.setFeaturedReward(rewards.get(position))
        }
    }

    override fun getItemCount(): Int {
        return rewards.size
    }

    // Our Badge ViewHolder contains:
    // - The reward image
    // - The reward title
    class ViewHolder(val view :View, ) : RecyclerView.ViewHolder(
        view
    ) {
        val rewardIconView : ImageView
        val rewardTitleView : TextView

        init {
            // Capture the view for each view holder component at instantiation time
            rewardIconView = view.findViewById<ImageView>(R.id.reward_icon_view)
            rewardTitleView= view.findViewById<TextView>(R.id.reward_title_view)
        }

    }
}