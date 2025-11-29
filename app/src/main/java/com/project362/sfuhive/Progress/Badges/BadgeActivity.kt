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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.DataViewModel

class BadgeActivity : AppCompatActivity(){

    // View Models
    private lateinit var badgeActivityVM : BadgeActivityViewModel
    private lateinit var repoVM : DataViewModel
    private var badgeFactory = BadgeFactory()
    private var tmpBadgesList = badgeFactory.getAllBadges()

    private lateinit var featuredBadgeView : CardView

    private lateinit var featuredImageView : ImageView
    private lateinit var featuredTitleView : TextView
    private lateinit var featuredSubheadView : TextView
    private lateinit var featuredBodyView : TextView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        var vmFactory =  Util.getViewModelFactory(this)
        repoVM=ViewModelProvider(this, vmFactory).get(DataViewModel::class.java)
        initBadgeStatus()
        setContentView(R.layout.fragment_badges)
        badgeActivityVM=BadgeActivityViewModel(badgeFactory.mutableBadges)
        val badgeSelectView = findViewById<RecyclerView>(R.id.badge_selection)
        val badgeAdapter = BadgeAdapter(this, tmpBadgesList,badgeActivityVM )
        badgeSelectView.adapter= badgeAdapter
        badgeSelectView.layoutManager = GridLayoutManager(this, 3)

        badgeActivityVM.setBadgeList(badgeFactory.getAllBadges())

        featuredBadgeView =findViewById<CardView>(R.id.featured_badge)

        featuredImageView= featuredBadgeView.findViewById<ImageView>(R.id.featured_image)
        featuredTitleView=featuredBadgeView.findViewById<TextView>(R.id.title)
        featuredSubheadView=featuredBadgeView.findViewById<TextView>(R.id.subhead)
        featuredBodyView=featuredBadgeView.findViewById<TextView>(R.id.body)

        val resetBadgeButtonView = featuredBadgeView.findViewById<Button>(R.id.reset_badge_button)

        resetBadgeButtonView.setOnClickListener {
            val badgeid=badgeActivityVM.featuredBadge.value.getId()
            repoVM.lockBadge(badgeid)
            initBadgeStatus()
        }
    }

    override fun onResume() {
        super.onResume()

        badgeActivityVM.featuredBadge.observe(
            this,
            Observer {
                // update featured badge card
                updateFeaturedBadgeView(badgeActivityVM.getFeaturedBadge())

            }
        )
        initBadgeStatus()
    }
    private fun updateFeaturedBadgeView(newBadge: Badge){
        featuredImageView.setImageResource(newBadge.getIconId())
        featuredTitleView.text = newBadge.getTitle()
        featuredSubheadView.text = newBadge.getDescription()
        featuredBodyView.text = newBadge.getTextStatus()



    }

    // checks everybadge in the database to set completed badges as true
    private fun initBadgeStatus(){
        println("Loading badges...")
        for (badge in badgeFactory.mutableBadges.value){

            val savedState = repoVM.isBadgeLocked(badge.getId())
            badge.setIsLocked(savedState)
        }
    }
}