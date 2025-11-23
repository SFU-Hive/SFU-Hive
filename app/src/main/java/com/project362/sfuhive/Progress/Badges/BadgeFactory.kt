package com.project362.sfuhive.Progress.Badges

import com.project362.sfuhive.R

class BadgeFactory( ){

    private var allBadges = listOf<Badge>(
        Badge(
            1,
            "Badge 1",
            R.drawable.badge2_place_holder,
            R.drawable.badge_locked,
            "This is Pinned Badge 1"),
        Badge(
            2,
            "Badge2",
            R.drawable.badge2_place_holder,
            R.drawable.badge_locked,
            "This is Pinned Badge 2"),
        Badge(
            3,
            "Badge3",
            R.drawable.badge2_place_holder,
            R.drawable.badge_locked,
            "This is Pinned Badge 3"),
        Badge(
            4,
            "Badge4",
            R.drawable.badge2_place_holder,
            R.drawable.badge_locked,
            "This is Pinned Badge 4")

    )

    public fun getAllBadges(): List<Badge>{

        return allBadges
    }

}