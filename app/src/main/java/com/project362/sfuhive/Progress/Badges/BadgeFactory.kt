package com.project362.sfuhive.Progress.Badges

import androidx.lifecycle.MutableLiveData
import com.project362.sfuhive.Progress.Badges.BadgeUtils.BadgeIds
import com.project362.sfuhive.R

class BadgeFactory( ){

    companion object{
        //holds all ids for each badge
        val GOAL1 = 1L
        val GOAL2 = 2L
        val GOAL3 = 3L
        val BANK_BREAKER = 4L

    }
    private var allBadges = listOf<Badge>(
        Badge(
            GOAL1,
            "Goal 1",
            R.drawable.wellness_badge_1,
            R.drawable.badge_locked,
            "Complete your Goal 10 times!"),
        Badge(
            GOAL2,
            "Goal 2",
            R.drawable.wellness_badge_2,
            R.drawable.badge_locked,
            "Complete your Goal 10 times!"),
        Badge(
            GOAL3,
            "Goal 3",
            R.drawable.wellness_badge_3,
            R.drawable.badge_locked,
            "Complete your Goal 10 times!"),
        Badge(
            // Unlocks when the user spends all their money
            BANK_BREAKER,
            "Bank Breaker",
            R.drawable.bank_breaker_badge,
            R.drawable.badge_locked,
            "Try breaking the bank!")

    )

    val id2BadgeMap = mapOf(
        BadgeIds.GOAL1 to allBadges.get(0),

        BadgeIds.GOAL2 to allBadges.get(1),

        BadgeIds.GOAL3 to allBadges.get(2),

        BadgeIds.BANK_BREAKER to allBadges.get(3)
    )

    public var mutableBadges = MutableLiveData<List<Badge>>(allBadges)

    public fun getAllBadges(): List<Badge>{

        return allBadges
    }

    public fun getBageById(id : Long):Badge?{

        return id2BadgeMap.get(id)


    }

}