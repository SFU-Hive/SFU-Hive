package com.project362.sfuhive.Progress.Rewards

import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.R

class RewardFactory {

    private var allRewards = listOf<Reward>(
        Reward(
            R.drawable.bottle_red,
            "Red Bottle",
            100,
            "This bottle is red"),
        Reward(
            R.drawable.bottle_yellow,
            "Yellow Bottle",
            10,
            "This bottle is yellow"),
        Reward(
            R.drawable.bottle_purple,
            "Purple Bottle",
            20,
            "This bottle is purple"),
        Reward(
            R.drawable.bottle_green,
            "Green Bottle",
            25,
            "This bottle is green"),
        Reward(
            R.drawable.bottle,
            "Blue Bottle",
            30,
            "This bottle is blue"),
        Reward(
            R.drawable.reward_chest,
            "Chest",
            60,
            "This is a mystery chest")
    )
    public fun getAllRewards(): List<Reward>{

        return allRewards
    }
}