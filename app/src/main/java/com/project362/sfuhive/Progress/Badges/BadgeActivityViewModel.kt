package com.project362.sfuhive.Progress.Badges

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.DataViewModel
import kotlinx.coroutines.flow.Flow
import kotlin.collections.mutableListOf

class BadgeActivityViewModel(
    private var allBadges:List<Badge>,
    private var dataViewModel : DataViewModel

):ViewModel(){
    //public var badgeStates : MutableLiveData<List<BadgeEntity>> =

    init{
        allBadges.forEach { badge ->
            val id = badge.getId()
            var flow = dataViewModel.getBadgeFlow(id)
            badge.badgeEntity = flow.asLiveData()

            if(flow == null){
                Log.d("BadgeActivityViewModel", "Flow is NULL ")
            }

            Log.d("BadgeActivityViewModel", "Badge entity == ${flow.asLiveData()}")

        }
    }
    public var featuredBadge: MutableLiveData<Badge> = MutableLiveData<Badge>(allBadges.get(1))


    public fun getFeaturedBadge(): Badge{

        return featuredBadge.value
    }

    public fun setFeaturedBadge(badge :Badge){
        featuredBadge.value= badge

    }


    public fun getBadgeList(): List<Badge>{
        return allBadges
    }



}