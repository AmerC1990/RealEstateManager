package com.openclassrooms.realestatemanager.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.realestatemanager.App
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.data.repository.ListingRepository
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch

class ListingsViewModel (): ViewModel() {
    var allListings : MutableLiveData<List<ListingEntity>> = MutableLiveData()

    init{
        allListings = MutableLiveData()
        getAllListings()
    }

    fun getAllUsersObservers(): MutableLiveData<List<ListingEntity>> {
        return allListings
    }

    fun getAllListings() {
        viewModelScope.launch(Default) {
            val listingDao = App.database?.listingDao()
            val list = listingDao?.getAllListingInfo()
            allListings.postValue(list)
        }
    }

    fun insertListingInfo(entity: ListingEntity){
        viewModelScope.launch(Default) {
            val listingDao = App.database?.listingDao()
            listingDao?.insertListing(entity)
            getAllListings()
        }

    }

    fun updateListingInfo(entity: ListingEntity){
        val listingDao = App.database?.listingDao()
        listingDao?.updateListing(entity)
        getAllListings()
    }

}