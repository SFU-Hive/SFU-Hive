package com.project362.sfuhive.Progress.Badges

import android.graphics.drawable.Drawable

class Badge(private val iconComplete : Int, // this is the id Int of the R.drawable.completedBadgeIcon
            private val iconLocked :  Int, // this is the id Int of the R.drawable.lockedBadgeIcon
            private val description :  String) {

    private var isComplete: Boolean = false


    // UPDATE PROGRESS should be overridden in "BadgeFactory" and used as dependency injection
    private fun updateProgress(){

    }

    public fun getIconId(): Int{

        return iconComplete
    }

    public fun getDescription():String{

        return description
    }


    public fun isCompleteStatus():Boolean{

        return isComplete
    }

    // Call this function to run
    public fun updateBadgeProgress(){
        updateProgress()
    }

}