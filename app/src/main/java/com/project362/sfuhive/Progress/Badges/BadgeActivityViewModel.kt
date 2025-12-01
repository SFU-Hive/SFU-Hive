package com.project362.sfuhive.Progress.Badges

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.project362.sfuhive.database.DataViewModel

// Holds and maintains the data for the Badge Activity:
// - An instance of all possible badges user can unlock
// - An instance of the ViewModel accessing all database data
// - The details of our currently selected "Featured Badge"
class BadgeActivityViewModel(
    private var allBadges:List<Badge>, // All the possible badges the use can unlock
    private var dataViewModel : DataViewModel // The ViewModel that provides access to our databases via one repo

):ViewModel(){

    init{
        // For every possible badge, establish a BadgeEntity flow (from the BadgesDatabase) to it's corresponding badge in this ViewModel
        allBadges.forEach { badge ->
            val id = badge.getId()
            var flow = dataViewModel.getBadgeFlow(id) // get flow from the database repo
            badge.badgeEntity = flow.asLiveData() // set the flow to our current badge in this viewModel

            if(flow == null){ // Sanity check
                Log.d("BadgeActivityViewModel", "Flow is NULL ")
            }
            Log.d("BadgeActivityViewModel", "Badge entity == ${flow.asLiveData()}")
        }
    }
    public var featuredBadge: MutableLiveData<Badge> = MutableLiveData<Badge>(allBadges.get(1)) // Set the default featured badge to be the badge at index 1


    // get the Badge object of the current featured badge
    public fun getFeaturedBadge(): Badge{

        return featuredBadge.value
    }

    // Set the given Badge object to be the current featured badge
    public fun setFeaturedBadge(badge :Badge){
        featuredBadge.value= badge

    }

    // Get a list of all instantiated badges in this ViewModel
    public fun getBadgeList(): List<Badge>{
        return allBadges
    }



}