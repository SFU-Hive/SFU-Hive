package com.project362.sfuhive.Progress.Badges

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BadgeActivityViewModel(
    private var allBadges: List<Badge>
):ViewModel(){

    public var featuredBadge: MutableLiveData<Badge> = MutableLiveData<Badge>(allBadges.get(1))


    public fun getFeaturedBadge(): Badge{

        return featuredBadge.value
    }

    public fun setFeaturedBadge(badge :Badge){
        featuredBadge.value= badge

    }



}