package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import kotlinx.coroutines.flow.Flow

interface ListingRepository {

    suspend fun getAllListings(): Flow<List<ListingEntity>>

    suspend fun getSingleItem(id: Int): Flow<ListingEntity>

    suspend fun saveListing(listing: ListingEntity): Long

    suspend fun updateListing(listing: ListingEntity): Int

}