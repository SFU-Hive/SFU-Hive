package com.project362.sfuhive.Progress.Rewards

class Reward(
    private val rewardIconId : Int,
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


}