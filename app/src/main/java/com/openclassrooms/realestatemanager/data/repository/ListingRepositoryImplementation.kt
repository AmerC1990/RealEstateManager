package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.data.cache.ListingCache
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ListingRepositoryImplementation(private val listingCache: ListingCache): ListingRepository {

    override suspend fun getAllListings(): Flow<List<ListingEntity>> {
        return listingCache.getAllListings()
    }

    override suspend fun saveListing(listing: ListingEntity): Long {
        return listingCache.saveListing(listing)
    }

    override suspend fun updateListing(listing: ListingEntity): Int {
        return listingCache.updateListing(listing)
    }

    override suspend fun getSingleItem(id: Int): Flow<ListingEntity> {
        return listingCache.getSingleItem(id)
    }

}