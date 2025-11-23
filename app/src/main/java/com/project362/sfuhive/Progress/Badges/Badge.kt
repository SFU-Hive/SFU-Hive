package com.project362.sfuhive.Progress.Badges

import android.graphics.drawable.Drawable

data class Badge(
    private val id :  Long,
    private val title :  String,
    private val iconComplete : Int, // this is the id Int of the R.drawable.completedBadgeIcon
    private val iconLocked :  Int, // this is the id Int of the R.drawable.lockedBadgeIcon
    private val description :  String) {

    private var isLocked: Boolean = true

    private fun updateProgress(){

    }

    public fun getIconId(): Int{
        if(isLocked == true){
            return iconLocked
        }
        return iconComplete
    }

    public fun getDescription():String{

        return description
    }


    public fun isCompleteStatus():Boolean{

        return !isLocked
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

    public fun setIsLocked(newState: Boolean?){
        if(newState != null){
            isLocked=newState
        }else{
            println("ERROR: new state is null!")
        }
    }
}