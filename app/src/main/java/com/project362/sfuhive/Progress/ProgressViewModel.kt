package com.project362.sfuhive.Progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.Progress.Rewards.Reward
import com.project362.sfuhive.R

class ProgressViewModel : ViewModel(){
    private var pinnedBadges : MutableLiveData<List<Badge>> = MutableLiveData<List<Badge>>();
    private var pinnedRewards : MutableLiveData<List<Reward>> = MutableLiveData<List<Reward>>();
    private var currency : MutableLiveData<Int> = MutableLiveData();


    private var tmpBadgesList = listOf<Badge>(
        Badge(
            R.drawable.badge2_place_holder,
            R.drawable.badge_place_holder,
            "This is Pinned Badge 1"),
        Badge(
            R.drawable.badge2_place_holder,
            R.drawable.badge_place_holder,
            "This is Pinned Badge 2"),
        Badge(
            R.drawable.badge2_place_holder,
            R.drawable.badge_place_holder,
            "This is Pinned Badge 3")
    )

    private var tmpRewardsList = listOf<Reward>(
        Reward(
            R.drawable.icon_place_holder,
            100,
            "This is Pinned Reward 1"),
        Reward(
            R.drawable.icon_place_holder,
            10,
            "This is Pinned Reward 2"),
        Reward(
            R.drawable.icon_place_holder,
            20,
            "This is Pinned Reward 3"),
    )

    init {
        pinnedBadges.value=tmpBadgesList // temp until a database integration exists?
        pinnedRewards.value=tmpRewardsList // temp until a database integration exists?
    }

    public fun getPinnedBadge(position: Int): Badge{

        return pinnedBadges.value!!.get(position)
    }

    public fun getPinnedReward(position: Int): Reward{

        return pinnedRewards.value!!.get(position)
    }


}