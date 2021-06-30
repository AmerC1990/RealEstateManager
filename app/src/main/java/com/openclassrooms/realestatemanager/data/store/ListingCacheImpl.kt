package com.openclassrooms.realestatemanager.data.store

import com.openclassrooms.realestatemanager.data.cache.ListingCache
import com.openclassrooms.realestatemanager.data.cache.ListingDao
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import kotlinx.coroutines.flow.Flow

class ListingCacheImpl constructor(private val dao: ListingDao): ListingCache {
    override suspend fun getAllListings(): Flow<List<ListingEntity>> {
        return dao.getAllListingInfo()
    }

    override suspend fun saveListing(listing: ListingEntity): Long {
        return dao.insertListing(listing)
    }

    override suspend fun updateListing(listing: ListingEntity): Int {
        return dao.updateListing(listing)
    }

    override suspend fun getSingleItem(id: Int): Flow<ListingEntity> {
        return dao.getSingleItem(id)
    }

}