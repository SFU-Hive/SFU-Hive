package com.project362.sfuhive.Progress.Rewards

import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.R

class RewardFactory {

    private var allRewards = listOf<Reward>(
        Reward(
            R.drawable.icon_place_holder,
            "Reward 1",
            100,
            "This is Pinned Reward 1"),
        Reward(
            R.drawable.icon_place_holder,
            "Reward 2",
            10,
            "This is Pinned Reward 2"),
        Reward(
            R.drawable.icon_place_holder,
            "Reward 3",
            20,
            "This is Pinned Reward 3"),
        Reward(
            R.drawable.icon_place_holder,
            "Reward 4",
            25,
            "This is Pinned Reward 4"),
        Reward(
            R.drawable.icon_place_holder,
            "Reward 5",
            30,
            "This is Pinned Reward 5"),
        Reward(
            R.drawable.icon_place_holder,
            "Reward 6",
            60,
            "This is Pinned Reward 6")
    )
    public fun getAllRewards(): List<Reward>{

        return allRewards
    }
}