package com.project362.sfuhive.Progress.Badges

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BadgeActivityViewModel(
    private var allBadges: MutableLiveData<List<Badge>>
):ViewModel(){

    public lateinit var mutableBadges :  List<MutableLiveData<Badge>>
    init{
        var mutableBadges = mutableListOf<MutableLiveData<Badge>>()
        allBadges.value.forEach { badge ->

            var liveDataBadge = MutableLiveData<Badge>(badge)
            mutableBadges.add(liveDataBadge)
        }
    }
    public var featuredBadge: MutableLiveData<Badge> = MutableLiveData<Badge>(allBadges.value.get(1))


    public fun getFeaturedBadge(): Badge{

        return featuredBadge.value
    }

    public fun setFeaturedBadge(badge :Badge){
        featuredBadge.value= badge

    }

    public fun setBadgeList(theList : List<Badge>){
        allBadges = MutableLiveData<List<Badge>>(theList)

    }

    public fun getBadgeList(): List<Badge>{
        return allBadges.value
    }



}