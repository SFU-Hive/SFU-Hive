package com.project362.sfuhive.Progress.Badges

import com.project362.sfuhive.R

class BadgeFactory( ){

    companion object{
        //holds all ids for each badge
        val BANK_BREAKER = 4L

    }
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
            // Unlocks when the user spends all their money
            BANK_BREAKER,
            "Bank Breaker",
            R.drawable.bank_breaker_badge,
            R.drawable.badge_locked,
            "Try breaking the bank!")

    )

    public fun getAllBadges(): List<Badge>{

        return allBadges
    }

}