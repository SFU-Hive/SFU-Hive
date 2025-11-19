package com.project362.sfuhive.Progress.Rewards

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project362.sfuhive.Progress.Badges.Badge

class RewardActivityViewModel(
        private var allRewards: List<Reward>
    ):ViewModel(){

        public var featuredReward: MutableLiveData<Reward> = MutableLiveData<Reward>(allRewards.get(1))

        public var currencyCount: MutableLiveData<Int> = MutableLiveData<Int>(100) // temporary start with 100 currency always. Change this later!!
        public fun getFeaturedBadge(): Reward{

            return featuredReward.value
        }

        public fun setFeaturedBadge(reward :Reward){
            featuredReward.value= reward
        }

        public fun getCurrencyCount() : Int{
                if(currencyCount.value != null){
                    return currencyCount.value
                }
                return 0
            }

        public fun setCurrencyCount(newCount:Int?){
            if(newCount!=null){
                currencyCount.value=newCount
            }
        }

        public fun isRedeemable() : Boolean{
            var isRedeemable =false
            if(getCurrencyCount() >= featuredReward.value.getCost() ){
                isRedeemable = true
            }
            return isRedeemable
        }
    }


