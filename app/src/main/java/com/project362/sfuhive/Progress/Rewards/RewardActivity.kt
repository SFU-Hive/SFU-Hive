package com.project362.sfuhive.Progress.Rewards

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.Progress.Badges.BadgeActivityViewModel
import com.project362.sfuhive.Progress.Badges.BadgeAdapter
import com.project362.sfuhive.Progress.Badges.BadgeFactory
import com.project362.sfuhive.R

class RewardActivity : AppCompatActivity() {
    private lateinit var rewardActivityVM : RewardActivityViewModel
    private var rewardFactory = RewardFactory()
    private var tmpRewardList =rewardFactory.getAllRewards()

    private lateinit var featuredRewardView : CardView

    private lateinit var featuredImageView : ImageView
    private lateinit var featuredTitleView : TextView
    private lateinit var featuredSubheadView : TextView
    private lateinit var featuredCostView : TextView

    private lateinit var featuredButtonView: Button

    private lateinit var currencyTextView : TextView
    private lateinit var currencyImageView : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_rewards)

        rewardActivityVM=RewardActivityViewModel(tmpRewardList)
        val rewardSelectView = findViewById<RecyclerView>(R.id.badge_selection)
        val rewardAdapter = RewardAdapter(this, tmpRewardList,rewardActivityVM )
        rewardSelectView.adapter= rewardAdapter
        rewardSelectView.layoutManager = GridLayoutManager(this, 3)

        featuredRewardView =findViewById<CardView>(R.id.featured_reward)

        featuredImageView= featuredRewardView.findViewById<ImageView>(R.id.featured_image)
        featuredTitleView=featuredRewardView.findViewById<TextView>(R.id.title)
        featuredSubheadView=featuredRewardView.findViewById<TextView>(R.id.subhead)
        featuredCostView=featuredRewardView.findViewById<TextView>(R.id.cost)
        featuredButtonView=featuredRewardView.findViewById<Button>(R.id.buy_button)
        currencyTextView = findViewById<TextView>(R.id.currency_text)
        currencyImageView = findViewById<ImageView>(R.id.currency_image)

        currencyTextView.text = rewardActivityVM.getCurrencyCount().toString()

        rewardActivityVM.currencyCount.observe(this,Observer{ it ->
            currencyTextView.text = rewardActivityVM.getCurrencyCount().toString()

        })

    }

    override fun onResume() {
        super.onResume()

        rewardActivityVM.featuredReward.observe(
            this,
            Observer {
                // update featured badge card
                println("observer triggered")
                updateFeaturedRewardView(rewardActivityVM.getFeaturedBadge())

            }
        )
    }

    private fun updateFeaturedRewardView(newReward : Reward){
        featuredImageView.setImageResource(newReward.getIconId())
        featuredTitleView.text = newReward.getTitle()
        featuredCostView.text = newReward.getCost().toString()
        featuredSubheadView.text = newReward.getDescription()

    }


}