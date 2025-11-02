package com.project362.sfuhive.Progress.Badges

import com.project362.sfuhive.R

class BadgeFactory( ){

    private var allBadges = listOf<Badge>(
        Badge(
            "Badge1",
            R.drawable.badge2_place_holder,
            R.drawable.badge_place_holder,
            "This is Pinned Badge 1"),
        Badge(
            "Badge2",
            R.drawable.badge2_place_holder,
            R.drawable.badge_place_holder,
            "This is Pinned Badge 2"),
        Badge(
            "Badge3",
            R.drawable.badge2_place_holder,
            R.drawable.badge_place_holder,
            "This is Pinned Badge 3"),
        Badge(
            "Badge4",
            R.drawable.badge2_place_holder,
            R.drawable.badge_place_holder,
            "This is Pinned Badge 4")

    )

    public fun getAllBadges(): List<Badge>{

        return allBadges
    }
    // UPDATE PROGRESS should be overridden in "RewardManager" and used as dependency injection
    public fun purchase(){


    }

}