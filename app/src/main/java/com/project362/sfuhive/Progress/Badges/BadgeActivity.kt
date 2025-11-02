package com.project362.sfuhive.Progress.Badges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R

class BadgeActivity : AppCompatActivity(){

    private var badgeActivityVM = BadgeActivityViewModel()
    private var badgeFactory = BadgeFactory()
    private var tmpBadgesList = badgeFactory.getAllBadges()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_badges)

        val badgeSelectView = findViewById<RecyclerView>(R.id.badge_selection)
        val badgeAdapter = BadgeAdapter(this, tmpBadgesList)
        badgeSelectView.adapter= badgeAdapter
        badgeSelectView.layoutManager = GridLayoutManager(this, 3)

    }




}