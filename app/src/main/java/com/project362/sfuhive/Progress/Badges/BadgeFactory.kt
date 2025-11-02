package com.project362.sfuhive.Progress.Badges

class BadgeFactory(private val rewardIconId: Int, // this is the id Int of the R.drawable.rewardIcon
                   private val cost:  Int,
                   private val description :  String){



    // UPDATE PROGRESS should be overridden in "RewardManager" and used as dependency injection
    public fun purchase(){


    }

}