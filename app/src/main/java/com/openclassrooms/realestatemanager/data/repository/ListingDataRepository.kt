package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.data.cache.ListingCache
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.data.repository.ListingRepository

class ListingDataRepository(private val cacheStore: ListingCache): ListingRepository {
    override suspend fun getAllListings(): List<ListingEntity>? {
        return cacheStore.getAllListings()
    }

    override suspend fun saveListing(listing: ListingEntity?) {
        cacheStore.saveListing(listing)
    }

    override suspend fun updateListing(listing: ListingEntity?) {
        cacheStore.updateListing(listing)
    }

}