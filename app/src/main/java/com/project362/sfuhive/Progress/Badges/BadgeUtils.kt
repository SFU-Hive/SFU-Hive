package com.project362.sfuhive.Progress.Badges

import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.BANK_BREAKER
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL1
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL2
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL3
import com.project362.sfuhive.R

object BadgeUtils {

    object BadgeIds{
        //holds all ids for each badge
        val GOAL1 = 1L
        val GOAL2 = 2L
        val GOAL3 = 3L
        val BANK_BREAKER = 4L
    }

    val id2BadgeMap = mapOf(
        BadgeIds.GOAL1 to Badge(
            GOAL1,
            "Goal 1",
            R.drawable.wellness_badge_1,
            R.drawable.badge_locked,
            "Complete your Goal 10 times!"),

        BadgeIds.GOAL2 to Badge(
            GOAL2,
            "Goal 2",
            R.drawable.wellness_badge_2,
            R.drawable.badge_locked,
            "Complete your Goal 10 times!"),

        BadgeIds.GOAL3 to Badge(
            GOAL3,
            "Goal 3",
            R.drawable.wellness_badge_3,
            R.drawable.badge_locked,
            "Complete your Goal 10 times!"),

        BadgeIds.BANK_BREAKER to Badge(
            // Unlocks when the user spends all their money
            BANK_BREAKER,
            "Bank Breaker",
            R.drawable.bank_breaker_badge,
            R.drawable.badge_locked,
            "Try breaking the bank!")
    )




    public fun getBadge(theId : Long): Badge?{

        return id2BadgeMap.get(theId)

    }


}