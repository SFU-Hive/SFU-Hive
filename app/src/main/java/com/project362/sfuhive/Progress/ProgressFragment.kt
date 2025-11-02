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
import com.project362.sfuhive.Progress.Badges.BadgeActivity
import com.project362.sfuhive.Progress.Rewards.RewardActivity
import com.project362.sfuhive.Progress.Streaks.StreakActivity

class ProgressFragment : Fragment() {
    private lateinit var progressViewModel : ProgressViewModel

    private lateinit var badgeActivity: BadgeActivity
    private lateinit var rewardsActivity: RewardActivity
    private lateinit var streaksActivity: StreakActivity

    private lateinit var badgeResult: ActivityResultLauncher<Intent>
    private lateinit var rewardResult: ActivityResultLauncher<Intent>
    private lateinit var streakResult: ActivityResultLauncher<Intent>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_progress, container, false)
        progressViewModel=ProgressViewModel()

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


        val pinnedBadge1View: ImageView=view.findViewById<ImageView>(R.id.pinned_badge1)
        val pinnedBadge2View: ImageView = view.findViewById<ImageView>(R.id.pinned_badge2)
        val pinnedBadge3View: ImageView=view.findViewById<ImageView>(R.id.pinned_badge3)

        val pinnedReward1View: ImageView=view.findViewById<ImageView>(R.id.pinned_reward1)
        val pinnedReward2View: ImageView = view.findViewById<ImageView>(R.id.pinned_reward2)
        val pinnedReward3View: ImageView=view.findViewById<ImageView>(R.id.pinned_reward3)

        pinnedBadge1View.setImageResource(progressViewModel.getPinnedBadge(0).getIconId())
        pinnedBadge2View.setImageResource(progressViewModel.getPinnedBadge(1).getIconId())
        pinnedBadge3View.setImageResource(progressViewModel.getPinnedBadge(2).getIconId())

        pinnedReward1View.setImageResource(progressViewModel.getPinnedReward(0).getIconId())
        pinnedReward2View.setImageResource(progressViewModel.getPinnedReward(1).getIconId())
        pinnedReward3View.setImageResource(progressViewModel.getPinnedReward(2).getIconId())
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
}