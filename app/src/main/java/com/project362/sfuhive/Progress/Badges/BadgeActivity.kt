package com.project362.sfuhive.Progress.Badges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.DataViewModel

// This activity displays all the possible badges
// Allows the user to click on a badge to see more detailed information
// The current badge with details being displayed is called the "featured badge"
class BadgeActivity : AppCompatActivity(){

    // View Models
    private lateinit var badgeActivityVM : BadgeActivityViewModel // Holds all the detailed info for the selected "featured" badge
    private lateinit var repoVM : DataViewModel // provides access between the view layer and the data layer
    private var badgeFactory = BadgeFactory() // Describes all instances of the app's badges. Contains all the badge details, icon ids, etc.
    private var tmpBadgesList = badgeFactory.getAllBadges() // Describes all instances of the app's badge. Contains all badge details

    // Badge Views
    private lateinit var featuredBadgeView : CardView // Provides access to the "featured badge"/ details of selected badge
    private lateinit var featuredImageView : ImageView // View to display Locked or unlocked icon view of "featured badge" selected
    private lateinit var featuredTitleView : TextView // View to display title of "featured badge" selected
    private lateinit var featuredSubheadView : TextView // to display description of "featured badge" selected
    private lateinit var featuredBodyView : TextView // to display locked/complete status of badge

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        // get access to the view model holding database info
        var vmFactory =  Util.getViewModelFactory(this)
        repoVM=ViewModelProvider(this, vmFactory).get(DataViewModel::class.java)

        // inflate view of activity
        setContentView(R.layout.fragment_badges)

        // get view model for badge-specific activity information
        badgeActivityVM=BadgeActivityViewModel(badgeFactory.getAllBadges(),repoVM)

        //Get and set adapter & layout manager for the RecyclerView grid
        val badgeSelectView = findViewById<RecyclerView>(R.id.badge_selection)
        val badgeAdapter = BadgeAdapter(this, tmpBadgesList,badgeActivityVM )
        badgeSelectView.adapter= badgeAdapter
        badgeSelectView.layoutManager = GridLayoutManager(this, 3)

        // Get instances of all views holding the "featured badge" detailed information
        featuredBadgeView =findViewById<CardView>(R.id.featured_badge)
        featuredImageView= featuredBadgeView.findViewById<ImageView>(R.id.featured_image)
        featuredTitleView=featuredBadgeView.findViewById<TextView>(R.id.title)
        featuredSubheadView=featuredBadgeView.findViewById<TextView>(R.id.subhead)
        featuredBodyView=featuredBadgeView.findViewById<TextView>(R.id.body)

        // Get the "reset badge" button instance
        val resetBadgeButtonView = featuredBadgeView.findViewById<Button>(R.id.reset_badge_button)

        //set on "reset button" click to change the "lock" status of the current selected badge (i.e current featured badge)
        resetBadgeButtonView.setOnClickListener {
            val badgeid=badgeActivityVM.featuredBadge.value.getId()
            repoVM.lockBadge(badgeid)
        }
    }

    override fun onResume() {
        super.onResume()

        //Observe when the featured badge data changes in our BadgeActivity ViewModel
        badgeActivityVM.featuredBadge.observe(
            this,
            Observer {
                // update featured badge card
                updateFeaturedBadgeView(badgeActivityVM.getFeaturedBadge())
            }
        )
    }

    // Assigns view model data of the featured badge to the corresponding view
    private fun updateFeaturedBadgeView(newBadge: Badge){
        featuredImageView.setImageResource(newBadge.getIconId())
        featuredTitleView.text = newBadge.getTitle()
        featuredSubheadView.text = newBadge.getDescription()
        featuredBodyView.text = newBadge.getTextStatus()
    }
}