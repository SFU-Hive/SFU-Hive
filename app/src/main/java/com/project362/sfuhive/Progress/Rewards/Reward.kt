package com.project362.sfuhive.Progress.Rewards

// A Reward holds all of the info needed to display a reward
// A Reward has the following properties:
// - title,
// - reward icon: a drawable source
// - cost to redeem
// - description of what to do when reward is redeemed
class Reward(
    private val rewardIconId : Int,
    private val rewardTitle : String,
    private val cost : Int,
    private val description : String
){
    public fun getIconId(): Int{
        return rewardIconId
    }

    public fun getCost(): Int{
        return cost
    }

    public fun getDescription(): String{
        return description
    }

    public fun getTitle(): String{
        return rewardTitle
    }
}