package com.project362.sfuhive.Progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.Progress.Rewards.Reward
import com.project362.sfuhive.R

// DEPRECATED!
// Progress View model held progress related data in the early stages of demos
class ProgressViewModel : ViewModel(){
    private var pinnedBadges : MutableLiveData<List<Badge>> = MutableLiveData<List<Badge>>();
    private var pinnedRewards : MutableLiveData<List<Reward>> = MutableLiveData<List<Reward>>();


    private var tmpBadgesList = listOf<Badge>(
        Badge(
            1,
            "Badge 1",
            R.drawable.badge2_place_holder,
            R.drawable.badge_locked,
            "This is Pinned Badge 1"),
        Badge(
            2,
            "Badge 2",
            R.drawable.badge2_place_holder,
            R.drawable.badge_locked,
            "This is Pinned Badge 2"),
        Badge(
            3,
            "Badge 3",
            R.drawable.badge2_place_holder,
            R.drawable.badge_locked,
            "This is Pinned Badge 3")
    )

    private var tmpRewardsList = listOf<Reward>(
        Reward(
            R.drawable.bottle_red,
            "Red Potion",
            100,
            "This bottle is red"),
        Reward(
            R.drawable.reward_chest,
            "Chest",
            10,
            "This is a mystery chest"),
        Reward(
            R.drawable.bottle_yellow,
            "Reward 3",
            20,
            "This bottle is yellow"),
    )

    init {
        pinnedBadges.value=tmpBadgesList // temp until a database integration exists?
        pinnedRewards.value=tmpRewardsList // temp until a database integration exists?
    }

    public fun getAllPinnedRewards(): List<Reward>{
        return tmpRewardsList
    }

}