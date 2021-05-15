package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.data.cache.ListingCache
import com.openclassrooms.realestatemanager.data.cache.ListingEntity

class ListingRepositoryImplementation(private val listingCache: ListingCache): ListingRepository {
    override suspend fun getAllListings(): List<ListingEntity> {
        return listingCache.getAllListings()
    }

    override suspend fun saveListing(listing: ListingEntity) {
        listingCache.saveListing(listing)
    }

    override suspend fun updateListing(listing: ListingEntity) {
        listingCache.updateListing(listing)
    }

}