package com.project362.sfuhive.Progress.Rewards

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project362.sfuhive.Progress.Badges.Badge

class RewardActivityViewModel(
        private var allRewards: List<Reward>
    ):ViewModel(){

        public var featuredReward: MutableLiveData<Reward> = MutableLiveData<Reward>(allRewards.get(1))

        public fun getFeaturedBadge(): Reward{

            return featuredReward.value
        }

        public fun setFeaturedBadge(reward :Reward){
            featuredReward.value= reward
        }



    }


