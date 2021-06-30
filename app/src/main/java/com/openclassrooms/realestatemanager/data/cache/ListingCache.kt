package com.openclassrooms.realestatemanager.data.cache

import kotlinx.coroutines.flow.Flow


interface ListingCache {

    suspend fun getAllListings(): Flow<List<ListingEntity>>

    suspend fun saveListing(listing: ListingEntity): Long

    suspend fun updateListing(listing: ListingEntity): Int

    suspend fun getSingleItem(id: Int): Flow<ListingEntity>

}