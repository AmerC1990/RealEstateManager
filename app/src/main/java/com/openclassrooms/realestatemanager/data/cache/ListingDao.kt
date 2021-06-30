package com.openclassrooms.realestatemanager.data.cache

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listing_info ORDER BY id DESC")
    fun getAllListingInfo(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listing_info WHERE id=:id ")
    fun getSingleItem(id: Int): Flow<ListingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertListing(listing: ListingEntity): Long

    @Update()
    fun updateListing(listing: ListingEntity): Int
}