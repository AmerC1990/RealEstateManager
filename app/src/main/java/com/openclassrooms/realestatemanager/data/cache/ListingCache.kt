package com.openclassrooms.realestatemanager.data.cache

interface ListingCache {

    suspend fun getAllListings(): List<ListingEntity>?

    suspend fun saveListing(listing: ListingEntity?)

    suspend fun updateListing(listing: ListingEntity?)

}