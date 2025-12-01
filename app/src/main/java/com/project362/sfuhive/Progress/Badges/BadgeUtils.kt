package com.project362.sfuhive.Progress.Badges

import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.BANK_BREAKER
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL1
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL2
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL3
import com.project362.sfuhive.R

// Easy Access to badgeIds.
// Note: this is not best practice as there is a duplicate in BadgeFactory.
// Not removing -- I am worried about breaking the project last minute -Miro
object BadgeUtils {

    //holds all ids for each badge
    object BadgeIds{
        //holds all ids for each badge
        val GOAL1 = 1L
        val GOAL2 = 2L
        val GOAL3 = 3L
        val BANK_BREAKER = 4L
    }

}