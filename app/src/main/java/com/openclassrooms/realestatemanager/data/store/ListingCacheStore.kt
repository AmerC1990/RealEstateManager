package com.openclassrooms.realestatemanager.data.store

import com.openclassrooms.realestatemanager.data.cache.ListingCache
import com.openclassrooms.realestatemanager.data.cache.ListingDao
import com.openclassrooms.realestatemanager.data.cache.ListingEntity

class ListingCacheStore constructor(private val dao: ListingDao): ListingCache {
    override suspend fun getAllListings(): List<ListingEntity>? {
        return dao.getAllListingInfo()
    }

    override suspend fun saveListing(listing: ListingEntity?) {
        dao.insertListing(listing)
    }

    override suspend fun updateListing(listing: ListingEntity?) {
        dao.updateListing(listing)
    }

}