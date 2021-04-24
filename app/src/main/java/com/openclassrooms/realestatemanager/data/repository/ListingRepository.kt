package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.data.cache.ListingEntity

interface ListingRepository {

    suspend fun getAllListings(): List<ListingEntity>?

    suspend fun saveListing(listing: ListingEntity?)

    suspend fun updateListing(listing: ListingEntity?)

}