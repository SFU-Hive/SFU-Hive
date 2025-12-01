package com.project362.sfuhive.Progress.Rewards

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Holds and maintains the data for the Reward Activity:
// - An instance of all possible rewards user can redeem
// - The details of our currently selected "Featured Reward"
class RewardActivityViewModel(
        private var allRewards: List<Reward> // All the possible rewards the use can redeem
    ):ViewModel(){

        // Set the default featured reward to be the reward at index 1
        public var featuredReward: MutableLiveData<Reward> = MutableLiveData<Reward>(allRewards.get(1))
        public var currencyCount: MutableLiveData<Long> = MutableLiveData<Long>()
        public fun getFeaturedReward(): Reward{

            return featuredReward.value
        }

        public fun setFeaturedReward(reward :Reward){
            featuredReward.value= reward
        }

        public fun getCurrencyCount() : Long{
                if(currencyCount.value != null){
                    return currencyCount.value
                }
                return 0
            }

        public fun setCurrencyCount(newCount:Long?){
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

        public fun subtractCost(): Int{
            val newCurrency=getCurrencyCount() - featuredReward.value.getCost()
            setCurrencyCount(newCurrency)

            return featuredReward.value.getCost()
        }
    }


