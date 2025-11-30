package com.project362.sfuhive.Progress.Badges

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.project362.sfuhive.database.Badge.BadgeEntity

data class Badge(
    private val id :  Long,
    private val title :  String,
    private val iconComplete : Int, // this is the id Int of the R.drawable.completedBadgeIcon
    private val iconLocked :  Int, // this is the id Int of the R.drawable.lockedBadgeIcon
    private val description :  String) {

    public var badgeEntity: LiveData<BadgeEntity> = MutableLiveData<BadgeEntity>(null)

    public var iconID :MutableLiveData<Int> = MutableLiveData<Int>(iconLocked)
    private fun updateProgress(){

    }

    public fun getIconId(): Int{
        var theId= iconLocked
        if(badgeEntity.value?.isLocked == false){
            theId= iconComplete
        }
        return theId
    }

    public fun getDescription():String{

        return description
    }


    public fun isCompleteStatus():Boolean{
        if(badgeEntity.value?.isLocked== false){
            return true
        }
        return false
    }

    // Call this function to run
    public fun updateBadgeProgress(){
        updateProgress()
    }

    public fun getTitle() :String{
        return title
    }

    public fun getTextStatus():String{
        var status = "Locked"
        if(isCompleteStatus() == true){
            status = "Complete!"
        }
            return status
    }

    public fun getId() : Long{

        return id
    }

//    public fun setIsLocked(newState: Boolean?){
//        if(newState != null){
//
//            //isLocked.value=newState
//        }else{
//            println("ERROR: new state is null!")
//        }
//    }

    public fun getUnlockedIcon(): Int{
        return iconComplete

    }
}