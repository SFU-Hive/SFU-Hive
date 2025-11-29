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

class ProgressFragment : Fragment() {
    private lateinit var progressViewModel : ProgressViewModel

    private lateinit var repoVM : DataViewModel
    private lateinit var badgeActivity: BadgeActivity
    private lateinit var rewardsActivity: RewardActivity
    private lateinit var streaksActivity: StreakActivity

    private lateinit var badgeResult: ActivityResultLauncher<Intent>
    private lateinit var rewardResult: ActivityResultLauncher<Intent>
    private lateinit var streakResult: ActivityResultLauncher<Intent>

    private var rewards = RewardFactory()
    private var badges = BadgeFactory()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_progress, container, false)
        progressViewModel=ProgressViewModel()
        var vmFactory =  Util.getViewModelFactory(requireActivity())
        repoVM=ViewModelProvider(this, vmFactory).get(DataViewModel::class.java)
        initBadgeStatus()

        val badgesTitleView: TextView =view.findViewById<TextView>(R.id.badges_title)
        val rewardsTitleView: TextView =view.findViewById<TextView>(R.id.rewards_title)
        val streaksTitleView: TextView =view.findViewById<TextView>(R.id.streaks_title)
        badgeActivity = BadgeActivity()
        rewardsActivity= RewardActivity()
        streaksActivity= StreakActivity()

        val badgeIntent = Intent(activity, BadgeActivity::class.java)
        val rewardIntent = Intent(activity, RewardActivity::class.java)
        val streakIntent = Intent(activity, StreakActivity::class.java)

        badgeResult=registerBadgeActivity()
        rewardResult=registerRewardActivity()
        streakResult=registerStreakActivity()

        // Set onClickListener on all titles to change the fragment to the fragment associated with it

        badgesTitleView.setOnClickListener {
            // on click take user to all the badges
            badgeResult.launch(badgeIntent)

        }

        rewardsTitleView.setOnClickListener {
            // on click take user to all the rewards
            rewardResult.launch(rewardIntent)
        }

        streaksTitleView.setOnClickListener {
            // on click take user to all the streaks
            streakResult.launch(streakIntent)
        }


        val pinnedBadgesView: RecyclerView=view.findViewById<RecyclerView>(R.id.pinned_badges)


        val pinnedRewardsView: RecyclerView=view.findViewById<RecyclerView>(R.id.pinned_rewards)

        val rewardActivityVM=RewardActivityViewModel(progressViewModel.getAllPinnedRewards())

        val badgeActivityVM = BadgeActivityViewModel(badges.mutableBadges)

        // Note: RewardsVM isn't needed here I'm just using it to reuse other objects I've written already
        // sorry 'bout it -Miro
        val pinnedRewardsAdapter = context?.let {
            RewardAdapter(it,
                rewards.getAllRewards(),
                rewardActivityVM)
        }


        val pinnedBadgesAdapter = context?.let {
            BadgeAdapter(
                it,
                badges.getAllBadges(),
                badgeActivityVM
            )
        }

        pinnedRewardsView.layoutManager= GridLayoutManager(context,3)
        pinnedRewardsView.adapter=pinnedRewardsAdapter

        pinnedBadgesView.layoutManager = GridLayoutManager(context, 3)
        pinnedBadgesView.adapter=pinnedBadgesAdapter




        return view
    }

    override fun onResume() {
        super.onResume()
        initBadgeStatus()
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

    private fun registerStreakActivity(): ActivityResultLauncher<Intent> {
        val result = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                println("Streak result was ok!")
                println("Activity was ${result.data}")

            } else {
                println("Streak result NOT OK!")
            }
        }
        return result
    }
    private fun initBadgeStatus() {
        println("Loading badges...")
        for (badge in badges.getAllBadges()) {
            val savedState = repoVM.isBadgeLocked(badge.getId())
            badge.setIsLocked(savedState)
        }
    }
}