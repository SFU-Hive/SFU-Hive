package com.project362.sfuhive.Progress.Rewards

import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.R

class RewardFactory {

    private var allRewards = listOf<Reward>(
        Reward(
            R.drawable.game_reward,
            "Evening Game",
            100,
            "Spend an evening playing your favourite game!"),
        Reward(
            R.drawable.tv_reward,
            "Watch TV",
            20,
            "Watch an episode of TV!"),
        Reward(
            R.drawable.treat_reward,
            "Sweet Treat",
            10,
            "Treat yourself to a sweet treat!"),
        Reward(
            R.drawable.reward_bath,
            "Relaxing Bath",
            20,
            "Spend some time in a relaxing bath. Don't forget your rubber duck!"),
        Reward(
            R.drawable.grass_reward,
            "Touch Grass",
            20,
            "Spend some time outdoors!"),
        Reward(
            R.drawable.nap_reward,
            "20 Min Nap",
            10,
            "Go take a 20 minute nap! (We know you're sleep deprived)")
    )
    public fun getAllRewards(): List<Reward>{

        return allRewards
    }
}