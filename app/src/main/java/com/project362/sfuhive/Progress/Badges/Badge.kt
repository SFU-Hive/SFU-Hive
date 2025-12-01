package com.project362.sfuhive.Progress.Badges

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.project362.sfuhive.database.Badge.BadgeEntity

// A Badge holds all of the info needed to display a badge
// A Badge has the following properties:
// - id,
// - title,
// - a drawable source to display when it is locked
// - a drawable source to display when it is unlocked
// - a description of how to unlock the badge
data class Badge(
    private val id :  Long,
    private val title :  String,
    private val iconComplete : Int, // this is the id Int of the R.drawable.completedBadgeIcon
    private val iconLocked :  Int, // this is the id Int of the R.drawable.lockedBadgeIcon
    private val description :  String) {

    // The BadgeEntity holds the live "badge state" data.
    // This BadgeEntity linked to the database that holds a "true"/"false" value for if the user has unlocked the badge.
    public var badgeEntity: LiveData<BadgeEntity> = MutableLiveData<BadgeEntity>(null)

    // Return the appropriate icon resource id based on whether or not the badge is locked
    // if badge is locked --> return the locked icon
    // if badge is unlocked --> return the unlocked icon
    public fun getIconId(): Int{
        var theId= iconLocked
        if(badgeEntity.value?.isLocked == false){
            // badge is unlocked
            theId= iconComplete
        }
        // badge is locked
        return theId
    }

    // Return the badge description. Typically used for TextView fields
    public fun getDescription():String{

        return description
    }

    //Return whether or not the user has completed (i.e unlocked) the badge
    public fun isCompleteStatus():Boolean{
        if(badgeEntity.value?.isLocked== false){
            return true // user has completed this badge
        }
        return false // user has not completed this badge
    }

    // return the title of this badge
    public fun getTitle() :String{
        return title
    }

    // Return one word describing the badges current completion status (locked/unlocked)
    // Typically used to populate a TextView field
    public fun getTextStatus():String{
        var status = "Locked"
        if(isCompleteStatus() == true){
            status = "Complete!"
        }
            return status
    }

    // Return the id of this badge
    public fun getId() : Long{
        return id
    }

    // Return the icon to be displayed when this badge is complete
    public fun getUnlockedIcon(): Int{
        return iconComplete

    }
}