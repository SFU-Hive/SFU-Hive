package com.project362.sfuhive.Progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.R

class ProgressViewModel : ViewModel(){
    private var pinnedBadges : MutableLiveData<List<Badge>> = MutableLiveData<List<Badge>>();

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

    init {
        pinnedBadges.value=tmpBadgesList
    }

    public fun getPinnedBadge(position: Int): Badge{

        return pinnedBadges.value!!.get(position)
    }


}