package com.project362.sfuhive.Progress.Badges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R

class BadgeActivity : AppCompatActivity(){

    private lateinit var badgeActivityVM : BadgeActivityViewModel
    private var badgeFactory = BadgeFactory()
    private var tmpBadgesList = badgeFactory.getAllBadges()

    private lateinit var featuredBadgeView : CardView

    private lateinit var featuredImageView : ImageView
    private lateinit var featuredTitleView : TextView
    private lateinit var featuredSubheadView : TextView
    private lateinit var featuredBodyView : TextView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_badges)
        badgeActivityVM=BadgeActivityViewModel(tmpBadgesList)
        val badgeSelectView = findViewById<RecyclerView>(R.id.badge_selection)
        val badgeAdapter = BadgeAdapter(this, tmpBadgesList,badgeActivityVM )
        badgeSelectView.adapter= badgeAdapter
        badgeSelectView.layoutManager = GridLayoutManager(this, 3)

        featuredBadgeView =findViewById<CardView>(R.id.featured_badge)

        featuredImageView= featuredBadgeView.findViewById<ImageView>(R.id.featured_image)
        featuredTitleView=featuredBadgeView.findViewById<TextView>(R.id.title)
        featuredSubheadView=featuredBadgeView.findViewById<TextView>(R.id.subhead)
        featuredBodyView=featuredBadgeView.findViewById<TextView>(R.id.body)


    }

    override fun onResume() {
        super.onResume()

        badgeActivityVM.featuredBadge.observe(
            this,
            Observer {
                // update featured badge card
                println("observer triggered")
                updateFeaturedBadgeView(badgeActivityVM.getFeaturedBadge())

            }
        )
    }
    private fun updateFeaturedBadgeView(newBadge: Badge){

        featuredImageView.setImageResource(newBadge.getIconId())
        featuredTitleView.text = newBadge.getTitle()
        featuredSubheadView.text = newBadge.getDescription()



    }
}