package com.project362.sfuhive.Progress.Badges

import androidx.lifecycle.MutableLiveData
import com.project362.sfuhive.Progress.Badges.BadgeUtils.BadgeIds
import com.project362.sfuhive.R

// Describes all instances of the app's badges. Contains all the badge details, icon ids, etc.
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
            // Unlocks when the user achieves their their goal 10 times (i.e for a total of 10 days)
            GOAL1,
            "Goal 1",
            R.drawable.wellness_badge_1,
            R.drawable.badge_locked,
            "Complete your Goal 10 times!"),
        Badge(
            // Unlocks when the user achieves their their goal 10 times (i.e for a total of 10 days)
            GOAL2,
            "Goal 2",
            R.drawable.wellness_badge_2,
            R.drawable.badge_locked,
            "Complete your Goal 10 times!"),
        Badge(
            // Unlocks when the user achieves their their goal 10 times (i.e for a total of 10 days)
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
            "Try spending all your coins!")
    )

    // Easy map to get the right badge instance from the specified badgeId
    val id2BadgeMap = mapOf(
        BadgeIds.GOAL1 to allBadges.get(0),

        BadgeIds.GOAL2 to allBadges.get(1),

        BadgeIds.GOAL3 to allBadges.get(2),

        BadgeIds.BANK_BREAKER to allBadges.get(3)
    )

    // Returns a list of all possible badges the user can unlock
    public fun getAllBadges(): List<Badge>{

        return allBadges
    }

    // returns a particular badge instance given the badge id
    public fun getBageById(id : Long):Badge?{

        return id2BadgeMap.get(id)

    }

}