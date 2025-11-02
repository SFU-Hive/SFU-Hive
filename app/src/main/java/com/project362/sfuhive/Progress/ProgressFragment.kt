package com.project362.sfuhive.Progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.R
import android.widget.ImageView
import android.widget.TextView
import com.project362.sfuhive.Calendar.CalendarFragment
import com.project362.sfuhive.Dashboard.DashboardFragment
import com.project362.sfuhive.Progress.Badges.BadgeFragment
import com.project362.sfuhive.Progress.Rewards.RewardFragment
import com.project362.sfuhive.Progress.Streaks.StreakFragment

class ProgressFragment : Fragment() {
    private lateinit var progressViewModel : ProgressViewModel

    private lateinit var badgeFrag: BadgeFragment
    private lateinit var rewardsFrag: RewardFragment
    private lateinit var streaksFrag: StreakFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_progress, container, false)
        progressViewModel=ProgressViewModel()

        val badgesTitleView: TextView =view.findViewById<TextView>(R.id.badges_title)
        val rewardsTitleView: TextView =view.findViewById<TextView>(R.id.rewards_title)
        badgeFrag = BadgeFragment()
        rewardsFrag= RewardFragment()
        streaksFrag= StreakFragment()


        // Set onClickListener on all titles to change the fragment to the fragment associated with it

        badgesTitleView.setOnClickListener {
            // on click take user to all the badges
            //fragTransaction.show()
            val fragTransaction=parentFragmentManager.beginTransaction()
            fragTransaction.setReorderingAllowed(true)
            fragTransaction.replace(context.c badgeFrag)
            fragTransaction.commit()


        }

        rewardsTitleView.setOnClickListener {
            // on click take user to all the badges

        }

        rewardsTitleView.setOnClickListener {
            // on click take user to all the badges

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
}