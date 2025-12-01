package com.project362.sfuhive.Progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project362.sfuhive.R
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.Progress.Badges.BadgeActivity
import com.project362.sfuhive.Progress.Badges.BadgeActivityViewModel
import com.project362.sfuhive.Progress.Badges.BadgeAdapter
import com.project362.sfuhive.Progress.Badges.BadgeFactory
import com.project362.sfuhive.Progress.Rewards.RewardActivity
import com.project362.sfuhive.Progress.Rewards.RewardActivityViewModel
import com.project362.sfuhive.Progress.Rewards.RewardAdapter
import com.project362.sfuhive.Progress.Rewards.RewardFactory
import com.project362.sfuhive.Progress.Streaks.StreakActivity
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.DataViewModel

// Progress Fragment displays all of the rewards and badges that are possible to redeem
// Note: Streaks was supposed to be part of this but we ran out of time to implement it
class ProgressFragment : Fragment() {
    private lateinit var progressViewModel : ProgressViewModel

    private lateinit var repoVM : DataViewModel // view model to access all of the room databases
    private lateinit var badgeActivity: BadgeActivity // holds an instance of BadgeActivity()
    private lateinit var rewardsActivity: RewardActivity // holds an instance of RewardActivity()
    private lateinit var badgeResult: ActivityResultLauncher<Intent> // holds result for badgeActivity
    private lateinit var rewardResult: ActivityResultLauncher<Intent> // holds result for rewardActivity

    private var rewards = RewardFactory() // The object holding all types of rewards
    private var badges = BadgeFactory() //  The object holding all types of badges

    // Creates the view for Progress Fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // inflate fragment
        val view=inflater.inflate(R.layout.fragment_progress, container, false)

        // initialize the ViewModels needed for ProgressFragment
        progressViewModel=ProgressViewModel()
        var vmFactory =  Util.getViewModelFactory(requireActivity())
        repoVM=ViewModelProvider(this, vmFactory).get(DataViewModel::class.java) // RepoVM is the ViewModel that is used to access data at the data layer


        val badgesTitleView: TextView =view.findViewById<TextView>(R.id.badges_title) // get access to the "Badge" title of our fragment layout
        val rewardsTitleView: TextView =view.findViewById<TextView>(R.id.rewards_title) // get access to the "Reward" title of our fragment layout

        badgeActivity = BadgeActivity() // get an instance of our badge activity for later launch
        rewardsActivity= RewardActivity() // get an instance of our reward activity for later launch

        val badgeIntent = Intent(activity, BadgeActivity::class.java) // Create the intent used to launch our Badge Activity
        val rewardIntent = Intent(activity, RewardActivity::class.java) // Create the intent to launch our Reward Activity

        // Register both activities in case we need to get a result from them
        badgeResult=registerBadgeActivity()
        rewardResult=registerRewardActivity()

        // Set onClickListener on all badge title to launch the "BadgeActivity" on "Badges" title click
        badgesTitleView.setOnClickListener {
            // on click take user to all the badges
            badgeResult.launch(badgeIntent)
        }

        // Set onClickListener on the reward title to launch the "RewardActivity" on "Rewards" title click
        rewardsTitleView.setOnClickListener {
            // on click take user to all the rewards
            rewardResult.launch(rewardIntent)
        }


        // Access the Badge RecyclerView grid to populate with all possible badges
        val pinnedBadgesView: RecyclerView=view.findViewById<RecyclerView>(R.id.pinned_badges)

        // Access the Reward RecyclerView grid to populate with all possible rewards
        val pinnedRewardsView: RecyclerView=view.findViewById<RecyclerView>(R.id.pinned_rewards)

        //Get an instance of the rewardActivity View model to use with the RecyclerView adapter
        val rewardActivityVM=RewardActivityViewModel(progressViewModel.getAllPinnedRewards())

        //Get an instance of the badgeActivity View model to use with the RecyclerView adapter
        val badgeActivityVM = BadgeActivityViewModel(badges.getAllBadges(), repoVM)

        // create the adapter for our Rewards RecyclerView
        val pinnedRewardsAdapter = context?.let {
            RewardAdapter(it,
                rewards.getAllRewards(),
                rewardActivityVM)
        }

        // create the adapter for our Badge RecyclerView
        val pinnedBadgesAdapter = context?.let {
            BadgeAdapter(
                it,
                badges.getAllBadges(),
                badgeActivityVM
            )
        }

        // Create a Grid layout manager for our Rewards RecyclerView
        pinnedRewardsView.layoutManager= GridLayoutManager(context,3)
        pinnedRewardsView.adapter=pinnedRewardsAdapter // Set the rewards RecyclerView adapter

        // Create a Grid layout manager for our Badge RecyclerView
        pinnedBadgesView.layoutManager = GridLayoutManager(context, 3)
        pinnedBadgesView.adapter=pinnedBadgesAdapter // Set the badge RecyclerView adapter
        return view
    }

    private fun registerBadgeActivity(): ActivityResultLauncher<Intent> {
        val result =registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                println("Badge result was ok!")
                println("Activity was ${result.data}")

            }else{
                println("Badge result NOT OK!")
            }
        }
        return result
    }

    private fun registerRewardActivity(): ActivityResultLauncher<Intent> {
        val result =registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                println("Reward result was ok!")
                println("Activity was ${result.data}")

            }else{
                println("Reward result NOT OK!")
            }
        }
        return result
    }

}