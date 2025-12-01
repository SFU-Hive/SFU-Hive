package com.project362.sfuhive.Progress.Rewards


import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.BANK_BREAKER
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.DataViewModel


// This activity displays all the possible rewards
// Allows the user to click on a reward to see more detailed information & spend coins to redeem it
// The current reward with details being displayed is called the "featured reward"
class RewardActivity : AppCompatActivity() {
    private lateinit var rewardActivityVM : RewardActivityViewModel // Holds all the detailed info for the selected "featured" reward
    private lateinit var repoVM : DataViewModel  // provides access between the view layer and the data layer
    private var rewardFactory = RewardFactory() // Describes all instances of the app's rewards. Contains all the reward details, icon ids, etc.
    private var tmpRewardList =rewardFactory.getAllRewards() // Describes all instances of the app's rewards. Contains all reward details

    private lateinit var featuredRewardView : CardView // Provides access to the "featured reward"/ details of selected reward

    private lateinit var featuredImageView : ImageView // View to display icon view of "featured reward" selected
    private lateinit var featuredTitleView : TextView // View to display title of "featured reward" selected
    private lateinit var featuredSubheadView : TextView // to display description of "featured reward" selected
    private lateinit var featuredCostView : TextView // to display cost of reward

    private lateinit var featuredButtonView: Button // View of button clicked to redeem reward

    // Views of coin display -- icon and quantity
    private lateinit var currencyTextView : TextView
    private lateinit var currencyImageView : ImageView

    // Key to access bundle information regarding the users decision to redeem or cancel a reward purchase
    companion object {
        val REDEEM = "REDEEM"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflate view of activity
        setContentView(R.layout.fragment_rewards)

        // get access to the view model holding database info
        rewardActivityVM=RewardActivityViewModel(tmpRewardList)
        var factory =  Util.getViewModelFactory(this)
        repoVM =ViewModelProvider(this, factory).get(DataViewModel::class.java)

        // set coin total based on shared prefs total
        rewardActivityVM.setCurrencyCount(Util.getCoinTotal(this))

        //Get and set adapter & layout manager for the RecyclerView grid
        val rewardSelectView = findViewById<RecyclerView>(R.id.badge_selection)
        val rewardAdapter = RewardAdapter(this, tmpRewardList,rewardActivityVM )
        rewardSelectView.adapter= rewardAdapter
        rewardSelectView.layoutManager = GridLayoutManager(this, 3)

        // Get instances of all views holding the "featured reward" detailed information
        featuredRewardView =findViewById<CardView>(R.id.featured_reward)
        featuredImageView= featuredRewardView.findViewById<ImageView>(R.id.featured_image)
        featuredTitleView=featuredRewardView.findViewById<TextView>(R.id.title)
        featuredSubheadView=featuredRewardView.findViewById<TextView>(R.id.subhead)
        featuredCostView=featuredRewardView.findViewById<TextView>(R.id.cost)
        featuredButtonView=featuredRewardView.findViewById<Button>(R.id.buy_button)

        // Get instances of the views holding user Coin total and coin icon
        currencyTextView = findViewById<TextView>(R.id.currency_text)
        currencyImageView = findViewById<ImageView>(R.id.currency_image)

        // Set saved currency value from sharedPrefs to Coin TextView displaying "coin quantity"
        currencyTextView.text = rewardActivityVM.getCurrencyCount().toString()

        // Handle whether or not the user wanted to redeem/continue or cancel a reward purchase
        supportFragmentManager.setFragmentResultListener(REDEEM,this){ requestKey, bundle ->
            val text_data = bundle.getBundle(PurchaseDialog.DIALOG_RESULT)
            val user_confirmation = bundle.getString(PurchaseDialog.STRING_RESULT)

            println("inside dialog result data is: $user_confirmation") // Debugging info


            if(user_confirmation == "redeemed"){ // The user wants to buy this reward
                //subtract cost of the featured reward
               rewardActivityVM.subtractCost()
                val newTotal=rewardActivityVM.currencyCount.value
                Util.updateCoinTotal(this,newTotal)


                if(rewardActivityVM.getCurrencyCount()==0L){ // Check to see if the user spent all their coins
                    if(repoVM.isBadgeLocked(BANK_BREAKER)==true){ // User spent all their coins ==> unlock "Bank Breaker" badge if locked
                        repoVM.unlockBadge(BANK_BREAKER)

                        Util.UnlockBadgeDialog( // Display "Badge unlocked" dialog to user after spending all their coins
                            BANK_BREAKER,
                            this.supportFragmentManager
                        )
                    }
                }
            }
        }

        // Update Coin quantity view if quantity has changed
        rewardActivityVM.currencyCount.observe(this,Observer{ it ->
            currencyTextView.text = rewardActivityVM.getCurrencyCount().toString()
        })

        // Define behaviour for "Redeem" reward button
        featuredButtonView.setOnClickListener { it ->
            //check to see if there is enough currency to redeem this reward
            if(rewardActivityVM.isRedeemable()){

                val dialog=PurchaseDialog(rewardActivityVM.getFeaturedReward())
                dialog.show(supportFragmentManager,"Redeem Reward")
                // if response is ok
//                  --> Add reward to database/Inventory
//                  --> Reduce cost
                // if response is canceled
            }else{
                // Notify user they don't have enough coins for this reward
                val dialog= CantRedeemDialog(rewardActivityVM.getFeaturedReward())
                dialog.show(supportFragmentManager,"Can't Redeem Reward")
                println("Cost too high")
            }

        }

    }

    override fun onResume() {
        super.onResume()

        rewardActivityVM.featuredReward.observe( // Update view to display "clicked on" reward
            this,
            Observer {
                // update featured reward card
                println("observer triggered")
                updateFeaturedRewardView(rewardActivityVM.getFeaturedReward())

            }
        )
    }

    // Assigns view model data of the featured reward to the corresponding view
    private fun updateFeaturedRewardView(newReward : Reward){
        featuredImageView.setImageResource(newReward.getIconId())
        featuredTitleView.text = newReward.getTitle()
        featuredCostView.text = "Cost: $${newReward.getCost()}"
        featuredSubheadView.text = newReward.getDescription()

    }


}